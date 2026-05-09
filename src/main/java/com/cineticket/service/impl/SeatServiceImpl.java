package com.cineticket.service.impl;

import com.cineticket.dto.request.SeatRequest;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.entity.Hall;
import com.cineticket.entity.Seat;
import com.cineticket.entity.Screening;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.exception.SeatAlreadyReservedException;
import com.cineticket.repository.HallRepository;
import com.cineticket.repository.OrderItemRepository;
import com.cineticket.repository.SeatRepository;
import com.cineticket.repository.ScreeningRepository;
import com.cineticket.service.SeatService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;
    private final OrderItemRepository orderItemRepository;
    private final ScreeningRepository screeningRepository;
    private final ModelMapper modelMapper;

    public SeatServiceImpl(SeatRepository seatRepository, HallRepository hallRepository,
                           OrderItemRepository orderItemRepository, ScreeningRepository screeningRepository, ModelMapper modelMapper) {
        this.seatRepository = seatRepository;
        this.hallRepository = hallRepository;
        this.orderItemRepository = orderItemRepository;
        this.screeningRepository = screeningRepository;
        this.modelMapper = modelMapper;
    }

    @SuppressWarnings("null")
    @Override
    public SeatResponse createSeat(SeatRequest request) {
        Hall hall = hallRepository.findById(request.getHallId())
            .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", request.getHallId()));

        if (seatRepository.existsByHallAndRowNumberAndSeatNumber(
                hall, request.getRowNumber(), request.getSeatNumber())) {
            throw new SeatAlreadyReservedException(
                String.format("Seat already exists in hall '%s': %s-%d", 
                    hall.getName(), request.getRowNumber(), request.getSeatNumber())
            );
        }

        Seat seat = new Seat();
        seat.setHall(hall);
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setType(request.getType());

        Seat savedSeat = seatRepository.save(seat);
        return mapToResponse(savedSeat, false);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse getSeatById(Long id) {
        Seat seat = findSeatEntityById(id);
        return mapToResponse(seat, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAllSeats() {
        return seatRepository.findAll().stream()
            .map(seat -> mapToResponse(seat, false))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByHallId(Long hallId) {
        return seatRepository.findAllByHallId(hallId).stream()
            .map(seat -> mapToResponse(seat, false))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeatsForScreening(Long screeningId) {

        Screening screening = screeningRepository.findById(screeningId)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));

        List<Seat> reservedSeats = orderItemRepository.findReservedSeatsForScreening(screeningId);
        List<Long> reservedSeatIds = reservedSeats.stream()
            .map(Seat::getId)
            .collect(Collectors.toList());

        return seatRepository.findAllByHallId(screening.getHall().getId()).stream()
            .map(seat -> {
                boolean isReserved = reservedSeatIds.contains(seat.getId());
                return mapToResponse(seat, isReserved);
            })
            .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void deleteSeat(Long id) {
        Seat seat = findSeatEntityById(id);
        seatRepository.delete(seat);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Seat findSeatEntityById(Long id) {
        return seatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", id));
    }

    private SeatResponse mapToResponse(Seat seat, boolean reserved) {
        SeatResponse response = modelMapper.map(seat, SeatResponse.class);
        response.setHallId(seat.getHall().getId());
        response.setHallName(seat.getHall().getName());
        response.setReserved(reserved);
        return response;
    }
}
