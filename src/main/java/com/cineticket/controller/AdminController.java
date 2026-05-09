package com.cineticket.controller;

import com.cineticket.dto.request.HallRequest;
import com.cineticket.dto.request.SeatRequest;
import com.cineticket.dto.response.HallResponse;
import com.cineticket.dto.response.MessageResponse;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.service.HallService;
import com.cineticket.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final HallService hallService;
    private final SeatService seatService;

    public AdminController(HallService hallService, SeatService seatService) {
        this.hallService = hallService;
        this.seatService = seatService;
    }

    // GET /api/admin/halls
    @GetMapping("/halls")
    public ResponseEntity<List<HallResponse>> getAllHalls() {
        List<HallResponse> halls = hallService.getAllHalls();
        return ResponseEntity.ok(halls);
    }

    // GET /api/admin/halls/{id}
     
    @GetMapping("/halls/{id}")
    public ResponseEntity<HallResponse> getHallById(@PathVariable Long id) {
        HallResponse hall = hallService.getHallById(id);
        return ResponseEntity.ok(hall);
    }
    
    //POST /api/admin/halls
   
    @PostMapping("/halls")
    public ResponseEntity<HallResponse> createHall(@Valid @RequestBody HallRequest request) {
        HallResponse hall = hallService.createHall(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(hall);
    }

    // PUT /api/admin/halls/{id}
    @PutMapping("/halls/{id}")
    public ResponseEntity<HallResponse> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallRequest request) {
        HallResponse hall = hallService.updateHall(id, request);
        return ResponseEntity.ok(hall);
    }

    // DELETE /api/admin/halls/{id}
    @DeleteMapping("/halls/{id}")
    public ResponseEntity<MessageResponse> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.ok(MessageResponse.success("Hall deleted successfully"));
    }

    // GET /api/admin/seats
    @GetMapping("/seats")
    public ResponseEntity<List<SeatResponse>> getAllSeats() {
        List<SeatResponse> seats = seatService.getAllSeats();
        return ResponseEntity.ok(seats);
    }

    // GET /api/admin/seats/hall/{hallId}
    @GetMapping("/seats/hall/{hallId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByHallId(@PathVariable Long hallId) {
        List<SeatResponse> seats = seatService.getSeatsByHallId(hallId);
        return ResponseEntity.ok(seats);
    }

    //GET /api/admin/seats/{id}
    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        SeatResponse seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    // POST /api/admin/seats
    @PostMapping("/seats")
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatRequest request) {
        SeatResponse seat = seatService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    // DELETE /api/admin/seats/{id}
    @DeleteMapping("/seats/{id}")
    public ResponseEntity<MessageResponse> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(MessageResponse.success("Seat deleted successfully"));
    }
}
