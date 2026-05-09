package com.cineticket.service.impl;

import com.cineticket.dto.request.HallRequest;
import com.cineticket.dto.response.HallResponse;
import com.cineticket.entity.Hall;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.repository.HallRepository;
import com.cineticket.service.HallService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final ModelMapper modelMapper;

    public HallServiceImpl(HallRepository hallRepository, ModelMapper modelMapper) {
        this.hallRepository = hallRepository;
        this.modelMapper = modelMapper;
    }

    @SuppressWarnings("null")
    @Override
    public HallResponse createHall(HallRequest request) {
        Hall hall = modelMapper.map(request, Hall.class);
        Hall savedHall = hallRepository.save(hall);
        return modelMapper.map(savedHall, HallResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public HallResponse getHallById(Long id) {
        Hall hall = findHallEntityById(id);
        return modelMapper.map(hall, HallResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getAllHalls() {
        return hallRepository.findAll().stream()
            .map(hall -> modelMapper.map(hall, HallResponse.class))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public HallResponse updateHall(Long id, HallRequest request) {
        Hall hall = findHallEntityById(id);
        modelMapper.map(request, hall);
        Hall updatedHall = hallRepository.save(hall);
        return modelMapper.map(updatedHall, HallResponse.class);
    }

    @SuppressWarnings("null")
    @Override
    public void deleteHall(Long id) {
        Hall hall = findHallEntityById(id);
        hallRepository.delete(hall);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Hall findHallEntityById(Long id) {
        return hallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
    }
}
