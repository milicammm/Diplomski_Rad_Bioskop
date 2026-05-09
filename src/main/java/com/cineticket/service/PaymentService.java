package com.cineticket.service;

import com.cineticket.dto.response.AdminPaymentTransactionResponse;
import com.cineticket.dto.response.PaymentResponse;
import com.cineticket.dto.response.StripeCheckoutResponse;
import com.cineticket.entity.Order;
import com.cineticket.entity.Payment;

import java.util.List;

public interface PaymentService {

    StripeCheckoutResponse createCheckoutSession(Long orderId);

    void handleStripeWebhook(String payload, String stripeSignature);

    List<AdminPaymentTransactionResponse> getLatestTransactions(int limit);

    PaymentResponse createPayment(Order order, String paymentIntentId);
    
    PaymentResponse getPaymentById(Long id);
    
    PaymentResponse getPaymentByOrderId(Long orderId);
    
    PaymentResponse updatePaymentStatus(String paymentIntentId, Payment.PaymentStatus status);

    // Additional admin/helper operations
    java.util.List<PaymentResponse> getAllPayments();

    PaymentResponse updatePayment(Long id, Payment.PaymentStatus status);

    void deletePayment(Long id);

    Payment findPaymentEntityByPaymentIntentId(String paymentIntentId);
}
