package com.cineticket.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(String message) {
        super(message);
    }

    public InsufficientSeatsException(Integer requested, Integer available) {
        super(String.format("Insufficient seats available. Requested: %d, Available: %d", requested, available));
    }
}
