package com.cineticket.service.impl;

import com.cineticket.dto.request.CreateOrderItemRequest;
import com.cineticket.dto.request.OrderRequest;
import com.cineticket.dto.response.OrderResponse;
import com.cineticket.entity.Hall;
import com.cineticket.entity.Movie;
import com.cineticket.entity.Order;
import com.cineticket.entity.Screening;
import com.cineticket.entity.Seat;
import com.cineticket.entity.User;
import com.cineticket.exception.InsufficientSeatsException;
import com.cineticket.exception.SeatAlreadyReservedException;
import com.cineticket.repository.OrderItemRepository;
import com.cineticket.repository.OrderRepository;
import com.cineticket.repository.ScreeningRepository;
import com.cineticket.repository.SeatRepository;
import com.cineticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private UserRepository userRepository;
    private ScreeningRepository screeningRepository;
    private SeatRepository seatRepository;
    private OrderServiceImpl orderService;

    private User user;
    private Screening screening;
    private Seat seat;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        userRepository = mock(UserRepository.class);
        screeningRepository = mock(ScreeningRepository.class);
        seatRepository = mock(SeatRepository.class);

        orderService = new OrderServiceImpl(
            orderRepository, orderItemRepository, userRepository,
            screeningRepository, seatRepository, new ModelMapper(), new DealCalculator()
        );

        user = new User();
        user.setId(1L);
        user.setUsername("milica");

        Hall hall = new Hall();
        hall.setId(1L);
        hall.setName("Hall A");

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Dune");

        // Monday matinee, regular seat, single ticket: matches no "Exclusive Deals" rule.
        screening = new Screening();
        screening.setId(10L);
        screening.setHall(hall);
        screening.setMovie(movie);
        screening.setPrice(new BigDecimal("12.50"));
        screening.setAvailableSeats(5);
        screening.setScreeningDate(LocalDate.of(2026, 1, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)));
        screening.setScreeningTime(LocalTime.of(14, 0));

        seat = new Seat();
        seat.setId(100L);
        seat.setRowNumber("A");
        seat.setSeatNumber(1);
        seat.setType(Seat.SeatType.REGULAR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningRepository.findById(10L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createOrderRejectsAlreadyReservedSeat() {
        when(orderItemRepository.isSeatReservedForScreening(10L, 100L)).thenReturn(true);

        OrderRequest request = new OrderRequest(1L, List.of(new CreateOrderItemRequest(10L, 100L, 1)));

        assertThrows(SeatAlreadyReservedException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrderRejectsWhenNotEnoughSeatsAvailable() {
        when(orderItemRepository.isSeatReservedForScreening(anyLong(), anyLong())).thenReturn(false);
        screening.setAvailableSeats(0);

        OrderRequest request = new OrderRequest(1L, List.of(new CreateOrderItemRequest(10L, 100L, 1)));

        assertThrows(InsufficientSeatsException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrderDecrementsAvailableSeatsAndChargesFullScreeningPrice() {
        when(orderItemRepository.isSeatReservedForScreening(anyLong(), anyLong())).thenReturn(false);

        OrderRequest request = new OrderRequest(1L, List.of(new CreateOrderItemRequest(10L, 100L, 1)));
        OrderResponse response = orderService.createOrder(request);

        assertEquals(4, screening.getAvailableSeats());
        assertEquals(1, response.getOrderItems().size());
        assertEquals(0, new BigDecimal("12.50").compareTo(response.getOrderItems().get(0).getPrice()));
        assertEquals(Order.OrderStatus.PENDING, response.getStatus());
    }
}
