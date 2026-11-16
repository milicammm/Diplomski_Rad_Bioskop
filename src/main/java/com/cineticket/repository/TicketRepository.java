package com.cineticket.repository;

import com.cineticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByOrderId(Long orderId);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
