package com.cineticket.service.impl;

import com.cineticket.dto.request.MovieRequest;
import com.cineticket.dto.response.MovieResponse;
import com.cineticket.entity.Movie;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.repository.MovieRepository;
import com.cineticket.repository.OrderItemRepository;
import com.cineticket.service.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;

    public MovieServiceImpl(MovieRepository movieRepository, OrderItemRepository orderItemRepository, ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.orderItemRepository = orderItemRepository;
        this.modelMapper = modelMapper;
    }

    @SuppressWarnings("null")
    @Override
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = modelMapper.map(request, Movie.class);
        Movie savedMovie = movieRepository.save(movie);
        return mapToResponse(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        Movie movie = findMovieEntityById(id);
        return mapToResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getActiveMovies() {
        return movieRepository.findAllByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingIgnoreCase(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, Pageable pageable) {
        return movieRepository.searchMovies(keyword, pageable)
            .map(this::mapToResponse);
    }

    @SuppressWarnings("null")
    @Override
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = findMovieEntityById(id);
        
        modelMapper.map(request, movie);
        
        Movie updatedMovie = movieRepository.save(movie);
        return mapToResponse(updatedMovie);
    }

    @SuppressWarnings("null")
    @Override
    public void deleteMovie(Long id) {
        Movie movie = findMovieEntityById(id);
        movieRepository.delete(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getSoldTicketsForMovie(Long movieId) {
        Long soldTickets = orderItemRepository.countSoldTicketsForMovie(movieId);
        return soldTickets != null ? soldTickets : 0L;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Movie findMovieEntityById(Long id) {
        return movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
    }

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = modelMapper.map(movie, MovieResponse.class);
        response.setSoldTickets(getSoldTicketsForMovie(movie.getId()));
        return response;
    }
}
