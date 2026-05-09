package com.cineticket.repository;

import com.cineticket.entity.Order;
import com.cineticket.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);

    List<Order> findAllByUser(User user);

    List<Order> findAllByUserAndStatus(User user, String status);

    List<Order> findAllByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findAllByUserOrderByOrderDateDesc(User user);
    
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    Page<Order> findAllByStatus(Order.OrderStatus status, Pageable pageable);
    
    Optional<Order> findByPaymentIntentId(String paymentIntentId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'CONFIRMED'")
    Double getTotalRevenueFromConfirmedOrders();

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.status= 'PENDING' AND o.orderDate < :expirationTime")
    List<Order> findExpiredPendingOrders(@Param("expirationTime") LocalDateTime expirationTime);

    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
