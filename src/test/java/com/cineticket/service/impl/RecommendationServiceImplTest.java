package com.cineticket.service.impl;

import com.cineticket.dto.response.MovieResponse;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.entity.Hall;
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
import com.cineticket.service.ScreeningService;
import com.cineticket.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationServiceImplTest {

    private MovieRepository movieRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private SeatRepository seatRepository;
    private MovieService movieService;
    private ScreeningService screeningService;
    private SeatService seatService;
    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        seatRepository = mock(SeatRepository.class);
        movieService = mock(MovieService.class);
        screeningService = mock(ScreeningService.class);
        seatService = mock(SeatService.class);

        recommendationService = new RecommendationServiceImpl(
            movieRepository, orderRepository, orderItemRepository, seatRepository,
            movieService, screeningService, seatService
        );
    }

    // --- Movies: content-based cosine similarity -----------------------------------

    @Test
    void recommendMoviesReturnsEmptyWithoutUserId() {
        assertTrue(recommendationService.recommendMovies(null, 6).isEmpty());
    }

    @Test
    void recommendMoviesExcludesWatchedAndRanksSharedDirectorAboveSharedLanguageOnly() {
        Movie oppenheimer = movie(1L, "Oppenheimer", "Drama", "Christopher Nolan", "English");
        Movie interstellar = movie(2L, "Interstellar", "Sci-Fi", "Christopher Nolan", "English");
        Movie barbie = movie(3L, "Barbie", "Comedy", "Greta Gerwig", "English");

        Screening watchedScreening = new Screening();
        watchedScreening.setMovie(oppenheimer);

        OrderItem watchedItem = new OrderItem();
        watchedItem.setScreening(watchedScreening);

        Order confirmedOrder = new Order();
        confirmedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        confirmedOrder.setOrderItems(List.of(watchedItem));

        when(orderRepository.findByUserIdWithItems(1L)).thenReturn(List.of(confirmedOrder));
        when(movieRepository.findAllByActiveTrue()).thenReturn(List.of(oppenheimer, interstellar, barbie));
        when(movieService.getMovieById(2L)).thenReturn(response(2L, "Interstellar"));
        when(movieService.getMovieById(3L)).thenReturn(response(3L, "Barbie"));

        List<MovieResponse> recommended = recommendationService.recommendMovies(1L, 6);

        assertEquals(2, recommended.size());
        assertEquals("Interstellar", recommended.get(0).getTitle());
        assertEquals("Barbie", recommended.get(1).getTitle());
    }

    // --- Seats: weighted utility scoring --------------------------------------------

    @Test
    void recommendSeatsReturnsEmptyWhenNoneAvailable() {
        Screening screening = screeningInHall(1L);
        when(screeningService.findScreeningEntityById(20L)).thenReturn(screening);
        when(seatService.getAvailableSeatsForScreening(20L)).thenReturn(List.of());

        assertTrue(recommendationService.recommendSeats(20L, null, 8).isEmpty());
    }

    @Test
    void recommendSeatsExcludesReservedAndPrefersHallCenterWithoutUserHistory() {
        Screening screening = screeningInHall(1L);
        when(screeningService.findScreeningEntityById(20L)).thenReturn(screening);

        // Hall layout: rows A/B, seats 1-3 -> center is row B, seat 2.
        when(seatRepository.findAllByHallId(1L)).thenReturn(List.of(
            seat(1L, "A", 1), seat(2L, "A", 2), seat(3L, "A", 3),
            seat(4L, "B", 1), seat(5L, "B", 2), seat(6L, "B", 3)
        ));
        when(orderItemRepository.countConfirmedBySeatForHall(1L)).thenReturn(List.of());

        SeatResponse cornerSeat = seatResponse(1L, "A", 1, false);
        SeatResponse centerSeat = seatResponse(5L, "B", 2, false);
        SeatResponse reservedCenterSeat = seatResponse(6L, "B", 3, true);
        when(seatService.getAvailableSeatsForScreening(20L))
            .thenReturn(List.of(cornerSeat, centerSeat, reservedCenterSeat));

        List<SeatResponse> recommended = recommendationService.recommendSeats(20L, null, 8);

        assertEquals(2, recommended.size());
        assertEquals(5L, recommended.get(0).getId());
        assertEquals(1L, recommended.get(1).getId());
    }

    @Test
    void recommendSeatsPrefersUserHistoricalSeatOverHallCenterWhenHistoryExists() {
        Screening screening = screeningInHall(1L);
        when(screeningService.findScreeningEntityById(20L)).thenReturn(screening);

        // Hall layout: rows A/B/C, seats 1-3 -> center is row B, seat 2.
        when(seatRepository.findAllByHallId(1L)).thenReturn(List.of(
            seat(1L, "A", 1), seat(2L, "A", 2), seat(3L, "A", 3),
            seat(4L, "B", 1), seat(5L, "B", 2), seat(6L, "B", 3),
            seat(7L, "C", 1), seat(8L, "C", 2), seat(9L, "C", 3)
        ));
        when(orderItemRepository.countConfirmedBySeatForHall(1L)).thenReturn(List.of());

        // User previously booked row C, seat 3 (far corner) -> that becomes their preference.
        OrderItem historicalItem = new OrderItem();
        historicalItem.setSeat(seat(9L, "C", 3));

        Order confirmedOrder = new Order();
        confirmedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        confirmedOrder.setOrderItems(List.of(historicalItem));
        when(orderRepository.findByUserIdWithItems(1L)).thenReturn(List.of(confirmedOrder));

        SeatResponse nearPreference = seatResponse(10L, "C", 2, false);
        SeatResponse atHallCenter = seatResponse(11L, "B", 2, false);
        when(seatService.getAvailableSeatsForScreening(20L))
            .thenReturn(List.of(nearPreference, atHallCenter));

        List<SeatResponse> recommended = recommendationService.recommendSeats(20L, 1L, 8);

        assertEquals(2, recommended.size());
        assertEquals(10L, recommended.get(0).getId());
        assertEquals(11L, recommended.get(1).getId());
    }

    @Test
    void recommendSeatsRanksMorePopularSeatHigherWhenEquidistantFromCenter() {
        Screening screening = screeningInHall(1L);
        when(screeningService.findScreeningEntityById(20L)).thenReturn(screening);

        // Hall layout: rows A/B, seats 1-3 -> center is row B, seat 2.
        when(seatRepository.findAllByHallId(1L)).thenReturn(List.of(
            seat(1L, "A", 1), seat(2L, "A", 2), seat(3L, "A", 3),
            seat(4L, "B", 1), seat(5L, "B", 2), seat(6L, "B", 3)
        ));

        // Seats B1 and B3 are both one seat away from the center (B2) -> equal distance score.
        SeatPopularityProjection popularProjection = mock(SeatPopularityProjection.class);
        when(popularProjection.getSeatId()).thenReturn(4L);
        when(popularProjection.getCount()).thenReturn(5L);
        when(orderItemRepository.countConfirmedBySeatForHall(1L)).thenReturn(List.of(popularProjection));

        SeatResponse popularSeat = seatResponse(4L, "B", 1, false);
        SeatResponse quietSeat = seatResponse(6L, "B", 3, false);
        when(seatService.getAvailableSeatsForScreening(20L))
            .thenReturn(List.of(popularSeat, quietSeat));

        List<SeatResponse> recommended = recommendationService.recommendSeats(20L, null, 8);

        assertEquals(2, recommended.size());
        assertEquals(4L, recommended.get(0).getId());
        assertEquals(6L, recommended.get(1).getId());
    }

    private Movie movie(Long id, String title, String genre, String director, String language) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setDirector(director);
        movie.setLanguage(language);
        movie.setActive(true);
        return movie;
    }

    private MovieResponse response(Long id, String title) {
        MovieResponse response = new MovieResponse();
        response.setId(id);
        response.setTitle(title);
        return response;
    }

    private Screening screeningInHall(Long hallId) {
        Hall hall = new Hall();
        hall.setId(hallId);

        Screening screening = new Screening();
        screening.setId(20L);
        screening.setHall(hall);
        return screening;
    }

    private Seat seat(Long id, String row, int seatNumber) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setRowNumber(row);
        seat.setSeatNumber(seatNumber);
        seat.setType(Seat.SeatType.REGULAR);
        return seat;
    }

    private SeatResponse seatResponse(Long id, String row, int seatNumber, boolean reserved) {
        return new SeatResponse(id, 1L, "Hall A", row, seatNumber, Seat.SeatType.REGULAR, reserved);
    }
}
