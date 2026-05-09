package com.cineticket.service.impl;

import com.cineticket.dto.request.ScreeningRequest;
import com.cineticket.dto.response.ScreeningResponse;
import com.cineticket.entity.Hall;
import com.cineticket.entity.Movie;
import com.cineticket.entity.Screening;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.exception.ScheduleConflictException;
import com.cineticket.repository.HallRepository;
import com.cineticket.repository.MovieRepository;
import com.cineticket.repository.OrderItemRepository;
import com.cineticket.repository.ScreeningRepository;
import com.cineticket.service.ScreeningService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScreeningServiceImpl implements ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;

    public ScreeningServiceImpl(ScreeningRepository screeningRepository, MovieRepository movieRepository,
                                HallRepository hallRepository, OrderItemRepository orderItemRepository, ModelMapper modelMapper) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.hallRepository = hallRepository;
        this.orderItemRepository = orderItemRepository;
        this.modelMapper = modelMapper;
    }

    @SuppressWarnings("null")
    @Override
    public ScreeningResponse createScreening(ScreeningRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));
        
        Hall hall = hallRepository.findById(request.getHallId())
            .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", request.getHallId()));

        screeningRepository.findByHallAndDateTime(
            request.getHallId(), 
            request.getScreeningDate(), 
            request.getScreeningTime()
        ).ifPresent(existing -> {
            throw new ScheduleConflictException(
                String.format("Screening already exists in hall '%s' at %s %s", 
                    hall.getName(), 
                    request.getScreeningDate(), 
                    request.getScreeningTime())
            );
        });

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setScreeningDate(request.getScreeningDate());
        screening.setScreeningTime(request.getScreeningTime());
        screening.setPrice(request.getPrice());
        screening.setAvailableSeats(hall.getCapacity());
        screening.setActive(request.getActive());

        Screening savedScreening = screeningRepository.save(screening);
        return mapToResponse(savedScreening);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreeningResponse getScreeningById(Long id) {
        Screening screening = findScreeningEntityById(id);
        return mapToResponse(screening);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningResponse> getAllScreenings() {
        return screeningRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningResponse> getScreeningsByMovieId(Long movieId) {
        return screeningRepository.findAllByMovieId(movieId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningResponse> getScreeningsByDate(LocalDate date) {
        return screeningRepository.findAllByScreeningDate(date).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningResponse> getAvailableScreenings(Long movieId) {
        LocalDate today = LocalDate.now();
        return screeningRepository.findAvailableScreeningsForMovie(movieId, today).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ScreeningResponse updateScreening(Long id, ScreeningRequest request) {
        Screening screening = findScreeningEntityById(id);

        if (!screening.getHall().getId().equals(request.getHallId()) ||
            !screening.getScreeningDate().equals(request.getScreeningDate()) ||
            !screening.getScreeningTime().equals(request.getScreeningTime())) {
            
            screeningRepository.findByHallAndDateTime(
                request.getHallId(), 
                request.getScreeningDate(), 
                request.getScreeningTime()
            ).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ScheduleConflictException("Another screening exists at this time");
                }
            });
        }

        @SuppressWarnings("null")
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));
        
        @SuppressWarnings("null")
        Hall hall = hallRepository.findById(request.getHallId())
            .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", request.getHallId()));

        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setScreeningDate(request.getScreeningDate());
        screening.setScreeningTime(request.getScreeningTime());
        screening.setPrice(request.getPrice());
        screening.setActive(request.getActive());

        Screening updatedScreening = screeningRepository.save(screening);
        return mapToResponse(updatedScreening);
    }

    @SuppressWarnings("null")
    @Override
    public void deleteScreening(Long id) {
        Screening screening = findScreeningEntityById(id);
        screeningRepository.delete(screening);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getSoldTicketsForScreening(Long screeningId) {
        Long soldTickets = orderItemRepository.countSoldTicketsForScreening(screeningId);
        return soldTickets != null ? soldTickets : 0L;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Screening findScreeningEntityById(Long id) {
        return screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
    }

    private ScreeningResponse mapToResponse(Screening screening) {
        ScreeningResponse response = modelMapper.map(screening, ScreeningResponse.class);
        response.setMovieId(screening.getMovie().getId());
        response.setMovieTitle(screening.getMovie().getTitle());
        response.setPosterUrl(screening.getMovie().getPosterUrl());
        response.setHallId(screening.getHall().getId());
        response.setHallName(screening.getHall().getName());
        response.setTotalSeats(screening.getHall().getCapacity());
        response.setSoldTickets(getSoldTicketsForScreening(screening.getId()));
        return response;
    }
}
