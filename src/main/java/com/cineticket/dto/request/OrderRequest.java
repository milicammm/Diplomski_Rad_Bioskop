package com.cineticket.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<CreateOrderItemRequest> orderItems;

    public OrderRequest() {
    }

    public OrderRequest(Long userId, List<CreateOrderItemRequest> orderItems) {
        this.userId = userId;
        this.orderItems = orderItems;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CreateOrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<CreateOrderItemRequest> orderItems) { this.orderItems = orderItems; }
}
