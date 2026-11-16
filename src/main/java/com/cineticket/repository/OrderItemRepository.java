package com.cineticket.repository;

import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Seat;
import com.cineticket.entity.Order;
import com.cineticket.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder(Order order);

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findAllByScreening(Screening screening);

    Optional<OrderItem> findByOrderAndSeat(Order order, Seat seat);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi WHERE oi.screening.id = :screeningId " +
           "AND oi.seat.id = :seatId " +
           "AND oi.order.status IN ('PENDING', 'CONFIRMED')")
    boolean isSeatReservedForScreening(
        @Param("screeningId") Long screeningId,
        @Param("seatId") Long seatId);

    @Query("SELECT oi.seat FROM OrderItem oi WHERE oi.screening.id = :screeningId " +
           "AND oi.order.status IN ('PENDING', 'CONFIRMED')")
    List<Seat> findReservedSeatsForScreening(@Param("screeningId") Long screeningId);


    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.screening.movie.id = :movieId " +
           "AND oi.order.status = 'CONFIRMED'")
    Long countSoldTicketsForMovie(@Param("movieId") Long movieId);

    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.screening.id = :screeningId " +
           "AND oi.order.status = 'CONFIRMED'")
    Long countSoldTicketsForScreening(@Param("screeningId") Long screeningId);

    @Query("SELECT oi.seat.id AS seatId, COUNT(oi) AS count FROM OrderItem oi " +
           "WHERE oi.seat.hall.id = :hallId AND oi.order.status = 'CONFIRMED' " +
           "GROUP BY oi.seat.id")
    List<SeatPopularityProjection> countConfirmedBySeatForHall(@Param("hallId") Long hallId);

    interface SeatPopularityProjection {
        Long getSeatId();
        Long getCount();
    }
}
