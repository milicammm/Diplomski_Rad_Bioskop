package com.cineticket.service;

import com.cineticket.dto.response.MovieResponse;
import com.cineticket.dto.response.SeatResponse;

import java.util.List;

public interface RecommendationService {

    /**
     * Content-based recommendation using cosine similarity over a movie feature vector
     * (genre, director, language, cast) built from the user's booking history.
     * Returns an empty list when the user has no usable booking history.
     */
    List<MovieResponse> recommendMovies(Long userId, int limit);

    /**
     * Weighted utility scoring over available seats for a screening: distance from the
     * user's historically preferred seat (or the hall center when there is no history),
     * combined with how often that physical seat has been booked by all users.
     */
    List<SeatResponse> recommendSeats(Long screeningId, Long userId, int limit);
}
