package com.cineticket.service;

import com.cineticket.dto.request.OrderRequest;
import com.cineticket.dto.response.OrderResponse;
import com.cineticket.entity.Order;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);
    
    OrderResponse getOrderById(Long id);
    
    List<OrderResponse> getAllOrders();
    
    List<OrderResponse> getOrdersByUserId(Long userId);
    
    OrderResponse updateOrderStatus(Long id, Order.OrderStatus status);
    
    void cancelOrder(Long id);

    void expireStalePendingOrders();

    Order findOrderEntityById(Long id);
    
    Order findOrderByPaymentIntentId(String paymentIntentId);
}
