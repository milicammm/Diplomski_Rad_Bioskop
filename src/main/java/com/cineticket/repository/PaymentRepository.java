package com.cineticket.repository;

import com.cineticket.entity.Payment;
import com.cineticket.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByStatus(String status);

    List<Payment> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findAllByStatusAndPaidAtBetween(Payment.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT sum(p.amount) FROM Payment p WHERE p.status='SUCCEEDED'")
    Double getTotalSuccessfulPayments();

    @Query("SELECT count(p) FROM Payment p WHERE p.status= :status")
    Long countByStatus(@Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status IN ('FAILED','CANCELLED') " +  " ORDER BY p.createdAt DESC")
    List<Payment> findFailedPayments();

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING'" + " AND p.createdAt < :expirationTime")
    List<Payment> findExpiredPendingPayments(@Param("expirationTime") LocalDateTime expirationTime);
    
    boolean existsByPaymentIntentId(String paymentIntentId);
}
