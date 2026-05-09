package com.cineticket.repository;

import com.cineticket.entity.Screening;
import com.cineticket.entity.Movie;
import com.cineticket.entity.Hall;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findAllByMovieId(Long movieId);

    List<Screening> findAllByActiveTrue();

    List<Screening> findAllByMovie(Movie movie);

    List<Screening> findAllByHall(Hall hall);
    
    List<Screening> findAllByMovieAndScreeningDate(Movie movie, LocalDate date);

    List<Screening> findAllByMovieAndActiveTrue(Movie movie);

    List<Screening> findAllByScreeningDateBetween(LocalDate startDate, LocalDate endDate);
   
    List<Screening> findAllByAvailableSeatsGreaterThan(int availableSeats);
   
    List<Screening> findAllByHallAndScreeningDate(Hall hall, LocalDate date);

    List<Screening> findAllByScreeningDate(LocalDate date);

    @Query("SELECT s FROM Screening s WHERE s.hall.id = :hallId " +
           "AND s.screeningDate = :date " +
           "AND s.screeningTime = :time")
    Optional<Screening> findByHallAndDateTime(
        @Param("hallId") Long hallId,
        @Param("date") LocalDate date,
        @Param("time") LocalTime time);

    Page<Screening> findAllByScreeningDateAndActiveTrue(LocalDate date, Pageable pageable);
 
 @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.screeningDate >= :currentDate AND s.active = true ORDER BY s.screeningDate, s.screeningTime")
List<Screening> findUpcomingScreeningsForMovie(
        @Param("movieId") Long movieId,
        @Param("currentDate") LocalDate currentDate
);

    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId " +
           "AND s.availableSeats > 0 AND s.active = true " +
           "AND s.screeningDate >= :currentDate")
    List<Screening> findAvailableScreeningsForMovie(
        @Param("movieId") Long movieId,
        @Param("currentDate") LocalDate currentDate);
}
