package com.cineticket.controller;

import com.cineticket.dto.request.MovieRequest;
import com.cineticket.dto.response.MessageResponse;
import com.cineticket.dto.response.MovieResponse;
import com.cineticket.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    //GET /api/movies
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<MovieResponse> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    // GET /api/movies/active
    @GetMapping("/active")
    public ResponseEntity<List<MovieResponse>> getActiveMovies() {
        List<MovieResponse> movies = movieService.getActiveMovies();
        return ResponseEntity.ok(movies);
    }

    // GET /api/movies/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    // GET /api/movies/search?keyword=
    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<MovieResponse> movies = movieService.searchMovies(keyword, pageable);
        return ResponseEntity.ok(movies);
    }

    //POST /api/movies
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    // PUT /api/movies/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(movie);
    }

    // DELETE /api/movies/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(MessageResponse.success("Movie deleted successfully"));
    }

    @SuppressWarnings("null")
    private Pageable buildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        Set<String> allowedSortFields = Set.of(
                "id",
                "title",
                "genre",
                "duration",
                "releaseDate",
                "director",
                "language",
                "active",
                "createdAt",
                "updatedAt"
        );

        String resolvedSortBy = allowedSortFields.contains(sortBy) ? sortBy : "createdAt";
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = Math.max(size, 1);

        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, resolvedSortBy));
    }
}
