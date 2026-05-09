package com.cineticket.repository;

import com.cineticket.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    Optional<Hall> findByName(String name);

    boolean existsByName(String name);

    List<Hall> findAllByOrderByCapacityDesc();

    List<Hall> findAllByCapacityGreaterThanEqual(Integer capacity);

    @Query("SELECT DISTINCT h from Hall h LEFT JOIN FETCH h.seats")
    List<Hall> findAllWithSeats();

}
