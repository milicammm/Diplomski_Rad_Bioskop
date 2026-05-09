package com.cineticket.service.impl;

import com.cineticket.dto.response.AdminPaymentItemResponse;
import com.cineticket.dto.response.AdminPaymentTransactionResponse;
import com.cineticket.dto.response.PaymentResponse;
import com.cineticket.dto.response.StripeCheckoutResponse;
import com.cineticket.entity.Order;
import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Payment;
import com.cineticket.exception.PaymentException;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.repository.OrderRepository;
import com.cineticket.repository.PaymentRepository;
import com.cineticket.repository.ScreeningRepository;
import com.cineticket.service.OrderService;
import com.cineticket.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ScreeningRepository screeningRepository;
    private final ModelMapper modelMapper;

    @Value("${app.stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${app.stripe.success.url}")
    private String stripeSuccessUrl;

    @Value("${app.stripe.cancel.url}")
    private String stripeCancelUrl;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderService orderService,
                              OrderRepository orderRepository,
                              ScreeningRepository screeningRepository,
                              ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.screeningRepository = screeningRepository;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeApiKey;
        String maskedWebhookSecret = stripeWebhookSecret == null || stripeWebhookSecret.isBlank()
            ? "(empty)"
            : stripeWebhookSecret.substring(0, Math.min(12, stripeWebhookSecret.length())) + "...";
        log.info("Stripe initialized. Webhook secret prefix: {}", maskedWebhookSecret);
    }

    @Override
    public StripeCheckoutResponse createCheckoutSession(Long orderId) {
        Order order = orderService.findOrderEntityById(orderId);

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new PaymentException("Order has no items for payment");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new PaymentException("Cancelled order cannot be paid");
        }

        if (order.getPayment() != null && order.getPayment().getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            throw new PaymentException("Order is already paid");
        }

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(stripeSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}&orderId=" + order.getId())
            .setCancelUrl(stripeCancelUrl + "?orderId=" + order.getId())
            .setClientReferenceId(String.valueOf(order.getId()))
            .putMetadata("orderId", String.valueOf(order.getId()))
            .putMetadata("userId", String.valueOf(order.getUser().getId()))
            .putMetadata("username", order.getUser().getUsername())
            .setCustomerEmail(order.getUser().getEmail())
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);

        for (OrderItem item : order.getOrderItems()) {
            long unitAmount = item.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(item.getQuantity().longValue())
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(unitAmount)
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.getScreening().getMovie().getTitle() + " - Seat " + item.getSeat().getRowNumber() + item.getSeat().getSeatNumber())
                                .build()
                        )
                        .build()
                )
                .build();

            paramsBuilder.addLineItem(lineItem);
        }

        try {
            Session session = Session.create(paramsBuilder.build());
            return new StripeCheckoutResponse(session.getId(), session.getUrl());
        } catch (StripeException ex) {
            log.error("Stripe error while creating checkout session: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
            String msg = "Unable to create Stripe checkout session";
            if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                msg = msg + ": " + ex.getMessage();
            }
            throw new PaymentException(msg, ex);
        }
    }

    @Override
    public void handleStripeWebhook(String payload, String stripeSignature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, stripeWebhookSecret);
            log.info("Received Stripe webhook event: type={}, id={}", event.getType(), event.getId());
        } catch (SignatureVerificationException ex) {
            throw new PaymentException("Invalid Stripe webhook signature", ex);
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            case "checkout.session.expired" -> handleCheckoutSessionExpired(event);
            case "payment_intent.succeeded" -> log.info("Payment intent succeeded received");
            default -> {
               
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPaymentTransactionResponse> getLatestTransactions(int limit) {
        int effectiveLimit = Math.max(1, Math.min(limit, 100));

        List<AdminPaymentTransactionResponse> result = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paidAt", "createdAt")).stream()
            .limit(effectiveLimit)
            .map(this::mapToAdminTransaction)
            .collect(Collectors.toList());

        log.info("getLatestTransactions called with limit={}, returning {} entries", effectiveLimit, result == null ? 0 : result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
            .map(p -> modelMapper.map(p, PaymentResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse createPayment(Order order, String paymentIntentId) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentIntentId(paymentIntentId);
        payment.setAmount(order.getTotalPrice());
        payment.setCurrency("USD");
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);
        return modelMapper.map(savedPayment, PaymentResponse.class);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return modelMapper.map(payment, PaymentResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return modelMapper.map(payment, PaymentResponse.class);
    }

    @SuppressWarnings("null")
    @Override
    public PaymentResponse updatePaymentStatus(String paymentIntentId, Payment.PaymentStatus status) {
        Payment payment = findPaymentEntityByPaymentIntentId(paymentIntentId);

        Order order = payment.getOrder();
        boolean shouldReleaseSeats = (status == Payment.PaymentStatus.FAILED || status == Payment.PaymentStatus.CANCELLED)
            && order.getStatus() != Order.OrderStatus.CANCELLED;

        payment.setStatus(status);

        if (status == Payment.PaymentStatus.SUCCEEDED) {
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.CONFIRMED);
        } else if (status == Payment.PaymentStatus.FAILED || status == Payment.PaymentStatus.CANCELLED) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            if (shouldReleaseSeats) {
                order.getOrderItems().forEach(item -> {
                    item.getScreening().setAvailableSeats(item.getScreening().getAvailableSeats() + item.getQuantity());
                    screeningRepository.save(item.getScreening());
                });
            }
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return modelMapper.map(updatedPayment, PaymentResponse.class);
    }

    @SuppressWarnings("null")
    @Override
    public PaymentResponse updatePayment(Long id, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        Order order = payment.getOrder();
        boolean shouldReleaseSeats = (status == Payment.PaymentStatus.FAILED || status == Payment.PaymentStatus.CANCELLED)
            && order.getStatus() != Order.OrderStatus.CANCELLED;

        payment.setStatus(status);

        if (status == Payment.PaymentStatus.SUCCEEDED) {
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.CONFIRMED);
        } else if (status == Payment.PaymentStatus.FAILED || status == Payment.PaymentStatus.CANCELLED) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            if (shouldReleaseSeats) {
                order.getOrderItems().forEach(item -> {
                    item.getScreening().setAvailableSeats(item.getScreening().getAvailableSeats() + item.getQuantity());
                    screeningRepository.save(item.getScreening());
                });
            }
        }

        Payment updated = paymentRepository.save(payment);
        return modelMapper.map(updated, PaymentResponse.class);
    }

    @SuppressWarnings("null")
    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        paymentRepository.delete(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findPaymentEntityByPaymentIntentId(String paymentIntentId) {
        return paymentRepository.findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentIntentId", paymentIntentId));
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = deserializeStripeObject(event, Session.class);
        String paymentIntentId = session.getPaymentIntent();

        log.info("Handling checkout.session.completed for session.id={}, paymentIntent={}", session.getId(), paymentIntentId);

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new PaymentException("Stripe checkout session has no payment intent");
        }

        Optional<Long> maybeOrderId = extractOrderId(session);
        if (maybeOrderId.isEmpty()) {
            log.warn("checkout.session.completed: no orderId metadata found for session={}", session.getId());
            return;
        }

        Order order;
        try {
            order = orderRepository.findByIdForUpdate(maybeOrderId.get())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", maybeOrderId.get()));
        } catch (ResourceNotFoundException ex) {
            log.warn("checkout.session.completed: order not found id={}", maybeOrderId.get());
            return;
        }
        order.setPaymentIntentId(paymentIntentId);

        Optional<Payment> existingPayment = paymentRepository.findByPaymentIntentId(paymentIntentId);

        Payment payment = existingPayment
            .or(() -> paymentRepository.findByOrder(order))
            .or(() -> paymentRepository.findByOrderId(order.getId()))
            .orElseGet(() -> {
                if (order.getPayment() != null) {
                    return order.getPayment();
                }

                Payment created = new Payment();
                created.setOrder(order);
                created.setPaymentIntentId(paymentIntentId);
                created.setAmount(order.getTotalPrice());
                created.setCurrency("USD");
                log.info("Creating new Payment entity for orderId={} paymentIntent={}", order.getId(), paymentIntentId);
                return created;
            });

        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentMethod("CARD");
        payment.setAmount(order.getTotalPrice());
        payment.setCurrency("USD");
        order.setStatus(Order.OrderStatus.CONFIRMED);

        if (!paymentIntentId.equals(payment.getPaymentIntentId())) {
            payment.setPaymentIntentId(paymentIntentId);
        }

        paymentRepository.save(payment);
        log.info("Payment saved: id={}, orderId={}, status={}", payment.getId(), order.getId(), payment.getStatus());
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = deserializeStripeObject(event, PaymentIntent.class);
        if (paymentIntent.getId() == null) {
            return;
        }

        if (paymentRepository.existsByPaymentIntentId(paymentIntent.getId())) {
            updatePaymentStatus(paymentIntent.getId(), Payment.PaymentStatus.FAILED);
        }
    }

    @SuppressWarnings("null")
    private void handleCheckoutSessionExpired(Event event) {
        Session session = deserializeStripeObject(event, Session.class);
        String paymentIntentId = session.getPaymentIntent();

        if (paymentIntentId != null && paymentRepository.existsByPaymentIntentId(paymentIntentId)) {
            updatePaymentStatus(paymentIntentId, Payment.PaymentStatus.CANCELLED);
            return;
        }

        Map<String, String> metadata = session.getMetadata();
        String orderIdRaw = metadata == null ? null : metadata.get("orderId");
        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            return;
        }

        Long orderId;
        try {
            orderId = Long.parseLong(orderIdRaw);
        } catch (NumberFormatException ex) {
            return;
        }

        Order order = orderService.findOrderEntityById(orderId);
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            return;
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.getOrderItems().forEach(item -> {
            item.getScreening().setAvailableSeats(item.getScreening().getAvailableSeats() + item.getQuantity());
            screeningRepository.save(item.getScreening());
        });
    }

    private Optional<Long> extractOrderId(Session session) {
        Map<String, String> metadata = session.getMetadata();
        String orderIdRaw = metadata == null ? null : metadata.get("orderId");

        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            if (session.getClientReferenceId() != null && !session.getClientReferenceId().isBlank()) {
                orderIdRaw = session.getClientReferenceId();
            }
        }

        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(orderIdRaw));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private <T> T deserializeStripeObject(Event event, Class<T> clazz) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return deserializer.getObject()
            .map(clazz::cast)
            .orElseThrow(() -> new PaymentException("Unable to deserialize Stripe event: " + event.getType()));
    }

    private AdminPaymentTransactionResponse mapToAdminTransaction(Payment payment) {
        Order order = payment.getOrder();

        AdminPaymentTransactionResponse response = new AdminPaymentTransactionResponse();
        response.setPaymentId(payment.getId());
        response.setPaymentIntentId(payment.getPaymentIntentId());
        response.setPaymentStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaidAt(payment.getPaidAt());
        response.setCurrency(payment.getCurrency());
        response.setTotalAmount(payment.getAmount());

        response.setOrderId(order.getId());
        response.setOrderStatus(order.getStatus());

        response.setCustomerId(order.getUser().getId());
        response.setCustomerUsername(order.getUser().getUsername());
        response.setCustomerEmail(order.getUser().getEmail());

        List<AdminPaymentItemResponse> items = order.getOrderItems().stream()
            .map(item -> new AdminPaymentItemResponse(
                item.getScreening().getMovie().getTitle() + " (Seat " + item.getSeat().getRowNumber() + item.getSeat().getSeatNumber() + ")",
                item.getQuantity(),
                item.getPrice(),
                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ))
            .collect(Collectors.toList());

        response.setPurchasedItems(items);
        return response;
    }
}
