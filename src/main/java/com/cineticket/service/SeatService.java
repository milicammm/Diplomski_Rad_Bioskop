package com.cineticket.service;

import com.cineticket.dto.request.SeatRequest;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.entity.Seat;

import java.util.List;

public interface SeatService {

    SeatResponse createSeat(SeatRequest request);
    
    SeatResponse getSeatById(Long id);
    
    List<SeatResponse> getAllSeats();
    
    List<SeatResponse> getSeatsByHallId(Long hallId);
    
    List<SeatResponse> getAvailableSeatsForScreening(Long screeningId);
    
    void deleteSeat(Long id);
    
    Seat findSeatEntityById(Long id);
}
