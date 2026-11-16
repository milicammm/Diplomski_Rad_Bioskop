package com.cineticket.controller;

import com.cineticket.dto.request.OrderRequest;
import com.cineticket.dto.response.MessageResponse;
import com.cineticket.dto.response.OrderResponse;
import com.cineticket.entity.Order;
import com.cineticket.entity.User;
import com.cineticket.exception.UnauthorizedException;
import com.cineticket.service.OrderService;
import com.cineticket.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    //POST /api/orders
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        requireOwnerOrAdmin(authentication, request.getUserId());
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id, Authentication authentication) {
        OrderResponse order = orderService.getOrderById(id);
        requireOwnerOrAdmin(authentication, order.getUserId());
        return ResponseEntity.ok(order);
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId, Authentication authentication) {
        requireOwnerOrAdmin(authentication, userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    //GET /api/orders
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // PUT /api/orders/{id}/status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> cancelOrder(@PathVariable Long id, Authentication authentication) {
        OrderResponse order = orderService.getOrderById(id);
        requireOwnerOrAdmin(authentication, order.getUserId());
        orderService.cancelOrder(id);
        return ResponseEntity.ok(MessageResponse.success("Order cancelled successfully"));
    }

    /**
     * Orders are personal data: a non-admin caller may only act on their own orders.
     * Without this check any authenticated user could read or cancel another user's
     * bookings simply by guessing an id (IDOR).
     */
    private void requireOwnerOrAdmin(Authentication authentication, Long resourceUserId) {
        User currentUser = userService.findUserEntityByUsername(authentication.getName());
        boolean isAdmin = currentUser.getRole() == com.cineticket.entity.Role.ADMIN;
        if (!isAdmin && !currentUser.getId().equals(resourceUserId)) {
            throw new UnauthorizedException("You are not allowed to access this order");
        }
    }
}
