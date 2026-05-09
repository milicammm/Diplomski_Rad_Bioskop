package com.cineticket.service;

import com.cineticket.dto.request.ScreeningRequest;
import com.cineticket.dto.response.ScreeningResponse;
import com.cineticket.entity.Screening;

import java.time.LocalDate;
import java.util.List;

public interface ScreeningService {

    ScreeningResponse createScreening(ScreeningRequest request);
    
    ScreeningResponse getScreeningById(Long id);
    
    List<ScreeningResponse> getAllScreenings();
    
    List<ScreeningResponse> getScreeningsByMovieId(Long movieId);
    
    List<ScreeningResponse> getScreeningsByDate(LocalDate date);
    
    List<ScreeningResponse> getAvailableScreenings(Long movieId);
    
    ScreeningResponse updateScreening(Long id, ScreeningRequest request);
    
    void deleteScreening(Long id);
    
    Long getSoldTicketsForScreening(Long screeningId);
    
    Screening findScreeningEntityById(Long id);
}
