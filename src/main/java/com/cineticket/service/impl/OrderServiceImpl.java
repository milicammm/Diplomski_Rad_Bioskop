package com.cineticket.service.impl;

import com.cineticket.dto.request.CreateOrderItemRequest;
import com.cineticket.dto.request.OrderRequest;
import com.cineticket.dto.response.OrderItemResponse;
import com.cineticket.dto.response.OrderResponse;
import com.cineticket.entity.*;
import com.cineticket.exception.InsufficientSeatsException;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.exception.SeatAlreadyReservedException;
import com.cineticket.repository.*;
import com.cineticket.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            UserRepository userRepository, ScreeningRepository screeningRepository,
                            SeatRepository seatRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.modelMapper = modelMapper;
    }

    @SuppressWarnings("null")
    @Override
    public OrderResponse createOrder(OrderRequest request) {

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

     
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

    
        for (CreateOrderItemRequest itemRequest : request.getOrderItems()) {
 
            Screening screening = screeningRepository.findById(itemRequest.getScreeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", itemRequest.getScreeningId()));

            Seat seat = seatRepository.findById(itemRequest.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", itemRequest.getSeatId()));

            boolean isReserved = orderItemRepository.isSeatReservedForScreening(
                screening.getId(), 
                seat.getId()
            );
            if (isReserved) {
                throw new SeatAlreadyReservedException(
                    seat.getRowNumber(), 
                    seat.getSeatNumber()
                );
            }

            if (screening.getAvailableSeats() < itemRequest.getQuantity()) {
                throw new InsufficientSeatsException(
                    itemRequest.getQuantity(), 
                    screening.getAvailableSeats()
                );
            }

            screening.setAvailableSeats(screening.getAvailableSeats() - itemRequest.getQuantity());
            screeningRepository.save(screening);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setScreening(screening);
            orderItem.setSeat(seat);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(screening.getPrice());

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderEntityById(id);
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = findOrderEntityById(id);
        
        if (status == Order.OrderStatus.CANCELLED && 
            order.getStatus() != Order.OrderStatus.CANCELLED) {
            
            for (OrderItem item : order.getOrderItems()) {
                Screening screening = item.getScreening();
                screening.setAvailableSeats(screening.getAvailableSeats() + item.getQuantity());
                screeningRepository.save(screening);
            }
        }
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        return mapToResponse(updatedOrder);
    }

    @Override
    public void cancelOrder(Long id) {
        updateOrderStatus(id, Order.OrderStatus.CANCELLED);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Order findOrderEntityById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderByPaymentIntentId(String paymentIntentId) {
        return orderRepository.findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "paymentIntentId", paymentIntentId));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = modelMapper.map(order, OrderResponse.class);
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        
        List<OrderItemResponse> items = order.getOrderItems().stream()
            .map(this::mapOrderItemToResponse)
            .collect(Collectors.toList());
        response.setOrderItems(items);
        
        return response;
    }

    private OrderItemResponse mapOrderItemToResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setScreeningId(item.getScreening().getId());
        response.setMovieTitle(item.getScreening().getMovie().getTitle());
        response.setHallName(item.getScreening().getHall().getName());
        
        String dateTime = item.getScreening().getScreeningDate().toString() + " " + 
                         item.getScreening().getScreeningTime().toString();
        response.setScreeningDateTime(dateTime);
        
        response.setSeatId(item.getSeat().getId());
        response.setSeatInfo(item.getSeat().getRowNumber() + "-" + item.getSeat().getSeatNumber());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        
        return response;
    }
}
