package com.cineticket.controller;

import com.cineticket.dto.response.SeatResponse;
import com.cineticket.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    // GET /api/seats/screening/{screeningId}/available
    @GetMapping("/screening/{screeningId}/available")
    public ResponseEntity<List<SeatResponse>> getAvailableSeatsForScreening(@PathVariable Long screeningId) {
        List<SeatResponse> seats = seatService.getAvailableSeatsForScreening(screeningId);
        return ResponseEntity.ok(seats);
    }
}
