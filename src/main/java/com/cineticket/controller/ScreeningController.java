package com.cineticket.controller;

import com.cineticket.dto.request.ScreeningRequest;
import com.cineticket.dto.response.MessageResponse;
import com.cineticket.dto.response.ScreeningResponse;
import com.cineticket.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    // GET /api/screenings
    @GetMapping
    public ResponseEntity<List<ScreeningResponse>> getAllScreenings() {
        List<ScreeningResponse> screenings = screeningService.getAllScreenings();
        return ResponseEntity.ok(screenings);
    }

    // GET /api/screenings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable Long id) {
        ScreeningResponse screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }

    // GET /api/screenings/movie/{movieId}
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ScreeningResponse>> getScreeningsByMovieId(@PathVariable Long movieId) {
        List<ScreeningResponse> screenings = screeningService.getScreeningsByMovieId(movieId);
        return ResponseEntity.ok(screenings);
    }

    // GET /api/screenings/movie/{movieId}/available
    @GetMapping("/movie/{movieId}/available")
    public ResponseEntity<List<ScreeningResponse>> getAvailableScreenings(@PathVariable Long movieId) {
        List<ScreeningResponse> screenings = screeningService.getAvailableScreenings(movieId);
        return ResponseEntity.ok(screenings);
    }

    // GET /api/screenings/date/{date}
        @GetMapping("/date/{date}")
    public ResponseEntity<List<ScreeningResponse>> getScreeningsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScreeningResponse> screenings = screeningService.getScreeningsByDate(date);
        return ResponseEntity.ok(screenings);
    }

    // POST /api/screenings
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningResponse> createScreening(@Valid @RequestBody ScreeningRequest request) {
        ScreeningResponse screening = screeningService.createScreening(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(screening);
    }

    // PUT /api/screenings/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningResponse> updateScreening(
            @PathVariable Long id,
            @Valid @RequestBody ScreeningRequest request) {
        ScreeningResponse screening = screeningService.updateScreening(id, request);
        return ResponseEntity.ok(screening);
    }

    // DELETE /api/screenings/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.ok(MessageResponse.success("Screening deleted successfully"));
    }
}
