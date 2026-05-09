package com.cineticket.service;

import com.cineticket.dto.request.MovieRequest;
import com.cineticket.dto.response.MovieResponse;
import com.cineticket.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {

    MovieResponse createMovie(MovieRequest request);
    
    MovieResponse getMovieById(Long id);
    
    List<MovieResponse> getAllMovies();
    
    Page<MovieResponse> getAllMovies(Pageable pageable);
    
    List<MovieResponse> getActiveMovies();
    
    List<MovieResponse> searchMovies(String keyword);
    
    Page<MovieResponse> searchMovies(String keyword, Pageable pageable);
    
    MovieResponse updateMovie(Long id, MovieRequest request);
    
    void deleteMovie(Long id);
    
    Long getSoldTicketsForMovie(Long movieId);
    
    Movie findMovieEntityById(Long id);
}
