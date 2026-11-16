package com.cineticket.dto.response;

import com.cineticket.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long userId;
    private String username;
    private BigDecimal totalPrice;
    private BigDecimal originalTotalPrice;
    private Integer discountPercentage;
    private String discountLabel;
    private LocalDateTime orderDate;
    private Order.OrderStatus status;
    private String paymentIntentId;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(Long id, Long userId, String username, BigDecimal totalPrice, LocalDateTime orderDate,
                         Order.OrderStatus status, String paymentIntentId, List<OrderItemResponse> orderItems,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.paymentIntentId = paymentIntentId;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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

    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public List<OrderItemResponse> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResponse> orderItems) { this.orderItems = orderItems; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
