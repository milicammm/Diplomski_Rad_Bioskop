package com.cineticket.exception;

public class SeatAlreadyReservedException extends RuntimeException {

    public SeatAlreadyReservedException(String message) {
        super(message);
    }

    public SeatAlreadyReservedException(String row, Integer seatNumber) {
        super(String.format("Seat %s-%d is already reserved", row, seatNumber));
    }
}
