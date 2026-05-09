package com.cineticket.repository;

import com.cineticket.entity.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByActiveTrue();

    Optional<Movie> findByTitle(String title);

    List<Movie> findAllByGenreContainingIgnoreCase(String genre);

    List<Movie> findAllByReleaseDateAfter(LocalDate date);

    List<Movie> findAllByDirectorContainingIgnoreCase(String director);

    Page<Movie> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.screenings WHERE m.active=true")
    List<Movie> findActiveMoviesWithScreenings();

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT m FROM Movie m WHERE " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.genre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Movie> searchMovies(@Param("keyword") String keyword, Pageable pageable);
}
