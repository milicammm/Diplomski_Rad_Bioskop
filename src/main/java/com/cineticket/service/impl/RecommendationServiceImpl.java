package com.cineticket.service.impl;

import com.cineticket.dto.response.MovieResponse;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.entity.Movie;
import com.cineticket.entity.Order;
import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Screening;
import com.cineticket.entity.Seat;
import com.cineticket.repository.MovieRepository;
import com.cineticket.repository.OrderItemRepository;
import com.cineticket.repository.OrderItemRepository.SeatPopularityProjection;
import com.cineticket.repository.OrderRepository;
import com.cineticket.repository.SeatRepository;
import com.cineticket.service.MovieService;
import com.cineticket.service.RecommendationService;
import com.cineticket.service.ScreeningService;
import com.cineticket.service.SeatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    // Content-based feature weights for the movie vector space model.
    private static final double GENRE_WEIGHT = 3.0;
    private static final double DIRECTOR_WEIGHT = 2.0;
    private static final double LANGUAGE_WEIGHT = 1.5;
    private static final double CAST_WEIGHT = 1.0;

    // Weighted utility formula weights for seat scoring.
    private static final double ROW_DISTANCE_WEIGHT = 3.0;
    private static final double SEAT_DISTANCE_WEIGHT = 1.0;
    private static final double POPULARITY_WEIGHT = 4.0;

    private final MovieRepository movieRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SeatRepository seatRepository;
    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final SeatService seatService;

    public RecommendationServiceImpl(MovieRepository movieRepository,
                                      OrderRepository orderRepository,
                                      OrderItemRepository orderItemRepository,
                                      SeatRepository seatRepository,
                                      MovieService movieService,
                                      ScreeningService screeningService,
                                      SeatService seatService) {
        this.movieRepository = movieRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.seatRepository = seatRepository;
        this.movieService = movieService;
        this.screeningService = screeningService;
        this.seatService = seatService;
    }

    // ---------------------------------------------------------------------
    // Movies: content-based filtering (weighted feature vector + cosine similarity)
    // ---------------------------------------------------------------------

    @Override
    public List<MovieResponse> recommendMovies(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        Set<Long> watchedMovieIds = new HashSet<>();
        Map<String, Double> profileVector = new HashMap<>();

        for (Order order : orderRepository.findByUserIdWithItems(userId)) {
            if (order.getStatus() != Order.OrderStatus.CONFIRMED && order.getStatus() != Order.OrderStatus.PENDING) {
                continue;
            }

            for (OrderItem item : order.getOrderItems()) {
                Screening screening = item.getScreening();
                Movie movie = screening != null ? screening.getMovie() : null;
                if (movie == null) {
                    continue;
                }

                watchedMovieIds.add(movie.getId());
                mergeInto(profileVector, buildMovieVector(movie));
            }
        }

        if (profileVector.isEmpty()) {
            return List.of();
        }

        return movieRepository.findAllByActiveTrue().stream()
            .filter(movie -> !watchedMovieIds.contains(movie.getId()))
            .map(movie -> Map.entry(movie, cosineSimilarity(profileVector, buildMovieVector(movie))))
            .filter(entry -> entry.getValue() > 0)
            .sorted((left, right) -> Double.compare(right.getValue(), left.getValue()))
            .limit(limit)
            .map(entry -> movieService.getMovieById(entry.getKey().getId()))
            .collect(Collectors.toList());
    }

    private Map<String, Double> buildMovieVector(Movie movie) {
        Map<String, Double> vector = new HashMap<>();

        for (String genre : splitTags(movie.getGenre(), "[/,&]")) {
            vector.merge("genre:" + genre.toLowerCase(), GENRE_WEIGHT, Double::sum);
        }

        if (movie.getDirector() != null && !movie.getDirector().isBlank()) {
            vector.merge("director:" + movie.getDirector().trim().toLowerCase(), DIRECTOR_WEIGHT, Double::sum);
        }

        if (movie.getLanguage() != null && !movie.getLanguage().isBlank()) {
            vector.merge("language:" + movie.getLanguage().trim().toLowerCase(), LANGUAGE_WEIGHT, Double::sum);
        }

        for (String actor : splitTags(movie.getCast(), ",")) {
            vector.merge("cast:" + actor.toLowerCase(), CAST_WEIGHT, Double::sum);
        }

        return vector;
    }

    private List<String> splitTags(String value, String delimiterRegex) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(delimiterRegex))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.toList());
    }

    private void mergeInto(Map<String, Double> target, Map<String, Double> source) {
        source.forEach((key, value) -> target.merge(key, value, Double::sum));
    }

    private double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        double dotProduct = 0;
        for (Map.Entry<String, Double> entry : a.entrySet()) {
            Double other = b.get(entry.getKey());
            if (other != null) {
                dotProduct += entry.getValue() * other;
            }
        }

        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());
        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (normA * normB);
    }

    // ---------------------------------------------------------------------
    // Seats: weighted multi-attribute utility scoring
    // ---------------------------------------------------------------------

    @Override
    public List<SeatResponse> recommendSeats(Long screeningId, Long userId, int limit) {
        if (screeningId == null || limit <= 0) {
            return List.of();
        }

        Screening screening = screeningService.findScreeningEntityById(screeningId);
        Long hallId = screening.getHall().getId();

        List<SeatResponse> availableSeats = seatService.getAvailableSeatsForScreening(screeningId).stream()
            .filter(seat -> !Boolean.TRUE.equals(seat.getReserved()))
            .collect(Collectors.toList());

        if (availableSeats.isEmpty()) {
            return List.of();
        }

        List<Seat> hallSeats = seatRepository.findAllByHallId(hallId);
        int centerRowRank = medianOf(hallSeats.stream().map(seat -> rowLabelToRank(seat.getRowNumber())).distinct());
        int centerSeatNumber = medianOf(hallSeats.stream().map(Seat::getSeatNumber).distinct());

        Map<Long, Long> popularityBySeatId = orderItemRepository.countConfirmedBySeatForHall(hallId).stream()
            .collect(Collectors.toMap(SeatPopularityProjection::getSeatId, SeatPopularityProjection::getCount));
        long maxPopularity = popularityBySeatId.values().stream().mapToLong(Long::longValue).max().orElse(0);

        SeatPreference preference = userId != null ? buildSeatPreference(userId) : null;

        return availableSeats.stream()
            .sorted((left, right) -> Double.compare(
                scoreSeat(right, preference, centerRowRank, centerSeatNumber, popularityBySeatId, maxPopularity),
                scoreSeat(left, preference, centerRowRank, centerSeatNumber, popularityBySeatId, maxPopularity)))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private SeatPreference buildSeatPreference(Long userId) {
        Map<Integer, Long> rowCounts = new HashMap<>();
        Map<Integer, Long> seatCounts = new HashMap<>();

        for (Order order : orderRepository.findByUserIdWithItems(userId)) {
            if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                continue;
            }

            for (OrderItem item : order.getOrderItems()) {
                Seat seat = item.getSeat();
                if (seat == null) {
                    continue;
                }

                rowCounts.merge(rowLabelToRank(seat.getRowNumber()), 1L, Long::sum);
                seatCounts.merge(seat.getSeatNumber(), 1L, Long::sum);
            }
        }

        if (rowCounts.isEmpty()) {
            return null;
        }

        return new SeatPreference(mostCommonKey(rowCounts), mostCommonKey(seatCounts));
    }

    private double scoreSeat(SeatResponse seat, SeatPreference preference, int centerRowRank, int centerSeatNumber,
                              Map<Long, Long> popularityBySeatId, long maxPopularity) {
        int rowRank = rowLabelToRank(seat.getRowNumber());

        double distanceScore;
        if (preference != null) {
            distanceScore = -(Math.abs(rowRank - preference.rowRank()) * ROW_DISTANCE_WEIGHT
                + Math.abs(seat.getSeatNumber() - preference.seatNumber()) * SEAT_DISTANCE_WEIGHT);
        } else {
            distanceScore = -(Math.abs(rowRank - centerRowRank) * ROW_DISTANCE_WEIGHT
                + Math.abs(seat.getSeatNumber() - centerSeatNumber) * SEAT_DISTANCE_WEIGHT);
        }

        double popularityScore = 0;
        if (maxPopularity > 0) {
            long count = popularityBySeatId.getOrDefault(seat.getId(), 0L);
            popularityScore = POPULARITY_WEIGHT * ((double) count / maxPopularity);
        }

        return distanceScore + popularityScore;
    }

    private int rowLabelToRank(String rowLabel) {
        if (rowLabel == null) {
            return 0;
        }

        int rank = 0;
        for (char character : rowLabel.trim().toUpperCase().toCharArray()) {
            if (character < 'A' || character > 'Z') {
                continue;
            }
            rank = rank * 26 + (character - 'A' + 1);
        }

        return rank;
    }

    private int medianOf(java.util.stream.Stream<Integer> values) {
        List<Integer> sorted = values.sorted().collect(Collectors.toList());
        return sorted.isEmpty() ? 0 : sorted.get(sorted.size() / 2);
    }

    private int mostCommonKey(Map<Integer, Long> counts) {
        return counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(0);
    }

    private record SeatPreference(int rowRank, int seatNumber) {
    }
}
