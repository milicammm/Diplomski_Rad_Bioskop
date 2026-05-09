package com.cineticket.dto.response;

import com.cineticket.entity.Seat;

public class SeatResponse {

    private Long id;
    private Long hallId;
    private String hallName;
    private String rowNumber;
    private Integer seatNumber;
    private Seat.SeatType type;
    private Boolean reserved;

    public SeatResponse() {
    }

    public SeatResponse(Long id, Long hallId, String hallName, String rowNumber, Integer seatNumber, Seat.SeatType type, Boolean reserved) {
        this.id = id;
        this.hallId = hallId;
        this.hallName = hallName;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.type = type;
        this.reserved = reserved;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getRowNumber() { return rowNumber; }
    public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Seat.SeatType getType() { return type; }
    public void setType(Seat.SeatType type) { this.type = type; }

    public Boolean getReserved() { return reserved; }
    public void setReserved(Boolean reserved) { this.reserved = reserved; }
}
