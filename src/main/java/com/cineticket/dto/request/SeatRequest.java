package com.cineticket.dto.request;

import com.cineticket.entity.Seat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatRequest {

    @NotNull(message = "Hall ID is required")
    private Long hallId;

    @NotBlank(message = "Row number is required")
    private String rowNumber;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    private Seat.SeatType type = Seat.SeatType.REGULAR;

    public SeatRequest() {
    }

    public SeatRequest(Long hallId, String rowNumber, Integer seatNumber, Seat.SeatType type) {
        this.hallId = hallId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.type = type;
    }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public String getRowNumber() { return rowNumber; }
    public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Seat.SeatType getType() { return type; }
    public void setType(Seat.SeatType type) { this.type = type; }
}
