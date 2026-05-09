package com.cineticket.service;

import com.cineticket.dto.request.HallRequest;
import com.cineticket.dto.response.HallResponse;
import com.cineticket.entity.Hall;

import java.util.List;

public interface HallService {

    HallResponse createHall(HallRequest request);
    
    HallResponse getHallById(Long id);
    
    List<HallResponse> getAllHalls();
    
    HallResponse updateHall(Long id, HallRequest request);
    
    void deleteHall(Long id);
    
    Hall findHallEntityById(Long id);
}
