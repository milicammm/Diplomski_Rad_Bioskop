package com.cineticket.controller;

import com.cineticket.dto.response.MovieResponse;
import com.cineticket.dto.response.SeatResponse;
import com.cineticket.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // GET /api/recommendations/movies?userId=&limit=
    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> recommendMovies(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(recommendationService.recommendMovies(userId, limit));
    }

    // GET /api/recommendations/seats?screeningId=&userId=&limit=
    @GetMapping("/seats")
    public ResponseEntity<List<SeatResponse>> recommendSeats(
            @RequestParam Long screeningId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(recommendationService.recommendSeats(screeningId, userId, limit));
    }
}
