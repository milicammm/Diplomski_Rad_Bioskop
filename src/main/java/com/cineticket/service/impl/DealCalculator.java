package com.cineticket.service.impl;

import com.cineticket.entity.Order;
import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Screening;
import com.cineticket.entity.Seat;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Evaluates the "Exclusive Deals" promotions against a set of order items.
 * Rules are non-stacking: the single highest-percentage rule that applies wins.
 */
@Component
public class DealCalculator {

    private static final int GROUP_BOOKING_MIN_SEATS = 4;
    private static final int GROUP_BOOKING_PERCENTAGE = 20;
    private static final int VIP_COMBO_PERCENTAGE = 15;
    private static final int WEEKEND_SPECIAL_PERCENTAGE = 10;
    private static final LocalTime WEEKEND_EVENING_START = LocalTime.of(18, 0);

    public record DiscountResult(int percentage, String label) {
    }

    public DiscountResult evaluate(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        boolean groupBookingEligible = items.size() >= GROUP_BOOKING_MIN_SEATS;
        boolean vipComboEligible = items.stream()
            .anyMatch(item -> item.getSeat().getType() == Seat.SeatType.VIP);
        boolean weekendSpecialEligible = items.stream()
            .anyMatch(item -> isWeekendEvening(item.getScreening()));

        if (groupBookingEligible) {
            return new DiscountResult(GROUP_BOOKING_PERCENTAGE, "Group Booking discount");
        }
        if (vipComboEligible) {
            return new DiscountResult(VIP_COMBO_PERCENTAGE, "Movie Night Combo discount");
        }
        if (weekendSpecialEligible) {
            return new DiscountResult(WEEKEND_SPECIAL_PERCENTAGE, "Weekend Special discount");
        }
        return null;
    }

    private boolean isWeekendEvening(Screening screening) {
        DayOfWeek day = screening.getScreeningDate().getDayOfWeek();
        boolean isWeekendDay = day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
        boolean isEvening = !screening.getScreeningTime().isBefore(WEEKEND_EVENING_START);
        return isWeekendDay && isEvening;
    }
}
