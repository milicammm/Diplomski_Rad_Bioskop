package com.cineticket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Min(value = 0, message = "Total price cannot be negative")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "original_total_price", precision = 10, scale = 2)
    private BigDecimal originalTotalPrice;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "discount_label", length = 100)
    private String discountLabel;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_intent_id", length = 255)
    private String paymentIntentId;

    @Column(name = "stripe_session_id", length = 255)
    private String stripeSessionId;

    @Column(name = "stripe_session_url", length = 1024)
    private String stripeSessionUrl;

    @Column(name = "stripe_session_expires_at")
    private LocalDateTime stripeSessionExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }

    public Order() {
    }

    public Order(Long id, User user, BigDecimal totalPrice, LocalDateTime orderDate, OrderStatus status,
                 String paymentIntentId, LocalDateTime createdAt, LocalDateTime updatedAt,
                 List<OrderItem> orderItems, Payment payment) {
        this.id = id;
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.paymentIntentId = paymentIntentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderItems = orderItems;
        this.payment = payment;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        orderDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalPrice();
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getOriginalTotalPrice() { return originalTotalPrice; }
    public void setOriginalTotalPrice(BigDecimal originalTotalPrice) { this.originalTotalPrice = originalTotalPrice; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getDiscountLabel() { return discountLabel; }
    public void setDiscountLabel(String discountLabel) { this.discountLabel = discountLabel; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public String getStripeSessionUrl() { return stripeSessionUrl; }
    public void setStripeSessionUrl(String stripeSessionUrl) { this.stripeSessionUrl = stripeSessionUrl; }

    public LocalDateTime getStripeSessionExpiresAt() { return stripeSessionExpiresAt; }
    public void setStripeSessionExpiresAt(LocalDateTime stripeSessionExpiresAt) { this.stripeSessionExpiresAt = stripeSessionExpiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}
