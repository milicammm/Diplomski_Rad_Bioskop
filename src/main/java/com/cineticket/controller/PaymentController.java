package com.cineticket.controller;

import com.cineticket.dto.response.AdminPaymentTransactionResponse;
import com.cineticket.dto.response.StripeCheckoutResponse;
import com.cineticket.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //POST /api/payments/checkout-session/{orderId}
    @PostMapping("/api/payments/checkout-session/{orderId}")
    public ResponseEntity<StripeCheckoutResponse> createCheckoutSession(@PathVariable Long orderId) {
        log.warn("=== CHECKOUT SESSION REQUEST ===");
        log.warn("Order ID: {}", orderId);
        StripeCheckoutResponse response = paymentService.createCheckoutSession(orderId);
        log.warn("Checkout session created: {}", response.getSessionId());
        return ResponseEntity.ok(response);
    }

    // POST /stripe/webhook
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature) {
        log.warn("=== WEBHOOK ENDPOINT CALLED ===");
        log.warn("Payload length: {}", payload.length());
        log.warn("Stripe-Signature header: {}", stripeSignature);
        paymentService.handleStripeWebhook(payload, stripeSignature);
        return ResponseEntity.ok("Webhook processed");
    }

    // GET /api/admin/payments/transactions
    @GetMapping("/api/admin/payments/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminPaymentTransactionResponse>> getLatestTransactions(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Admin requested latest payment transactions with limit={}", limit);
        List<AdminPaymentTransactionResponse> transactions = paymentService.getLatestTransactions(limit);
        log.info("Returning {} payment transactions to admin", transactions == null ? 0 : transactions.size());
        return ResponseEntity.ok(transactions);
    }
}
