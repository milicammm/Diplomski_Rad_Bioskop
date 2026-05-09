package com.cineticket.repository;

import com.cineticket.entity.Seat;
import com.cineticket.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByHallId(Long hallId);

    List<Seat> findAllByHall(Hall hall);

    Optional <Seat> findByHallAndRowNumberAndSeatNumber(Hall hall, String rowNumber, Integer seatNumber);
   
    List<Seat> findAllByHallAndType(Hall hall, String type);
    
    List <Seat> findAllByType(String type);

    boolean existsByHallAndRowNumberAndSeatNumber(Hall hall, String rowNumber, Integer seatNumber);

       @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByHallIdOrderedByRowAndNumber(@Param("hallId") Long hallId);
}
