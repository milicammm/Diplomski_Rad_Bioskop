package com.cineticket.service.impl;

import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Screening;
import com.cineticket.entity.Seat;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DealCalculatorTest {

    private final DealCalculator dealCalculator = new DealCalculator();

    @Test
    void noDiscountForARegularWeekdayMatineeWithFewSeats() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0))
        );

        assertNull(dealCalculator.evaluate(items));
    }

    @Test
    void fourOrMoreSeatsTriggersGroupBookingDiscount() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.MONDAY, LocalTime.of(14, 0))
        );

        DealCalculator.DiscountResult result = dealCalculator.evaluate(items);
        assertEquals(20, result.percentage());
        assertEquals("Group Booking discount", result.label());
    }

    @Test
    void vipSeatTriggersMovieNightComboDiscount() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.VIP, DayOfWeek.MONDAY, LocalTime.of(14, 0))
        );

        DealCalculator.DiscountResult result = dealCalculator.evaluate(items);
        assertEquals(15, result.percentage());
        assertEquals("Movie Night Combo discount", result.label());
    }

    @Test
    void fridayEveningTriggersWeekendSpecialDiscount() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.REGULAR, DayOfWeek.FRIDAY, LocalTime.of(19, 0))
        );

        DealCalculator.DiscountResult result = dealCalculator.evaluate(items);
        assertEquals(10, result.percentage());
        assertEquals("Weekend Special discount", result.label());
    }

    @Test
    void saturdayAfternoonDoesNotQualifyAsWeekendEvening() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.REGULAR, DayOfWeek.SATURDAY, LocalTime.of(15, 0))
        );

        assertNull(dealCalculator.evaluate(items));
    }

    @Test
    void discountsDoNotStackGroupBookingWinsOverVip() {
        List<OrderItem> items = List.of(
            item(Seat.SeatType.VIP, DayOfWeek.FRIDAY, LocalTime.of(19, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.FRIDAY, LocalTime.of(19, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.FRIDAY, LocalTime.of(19, 0)),
            item(Seat.SeatType.REGULAR, DayOfWeek.FRIDAY, LocalTime.of(19, 0))
        );

        DealCalculator.DiscountResult result = dealCalculator.evaluate(items);
        assertEquals(20, result.percentage());
        assertEquals("Group Booking discount", result.label());
    }

    @Test
    void emptyItemListReturnsNoDiscount() {
        assertNull(dealCalculator.evaluate(new ArrayList<>()));
    }

    private OrderItem item(Seat.SeatType seatType, DayOfWeek dayOfWeek, LocalTime time) {
        Seat seat = new Seat();
        seat.setType(seatType);

        LocalDate date = LocalDate.of(2026, 1, 1)
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek));

        Screening screening = new Screening();
        screening.setScreeningDate(date);
        screening.setScreeningTime(time);

        OrderItem orderItem = new OrderItem();
        orderItem.setSeat(seat);
        orderItem.setScreening(screening);
        return orderItem;
    }
}
