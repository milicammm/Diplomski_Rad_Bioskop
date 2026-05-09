package com.cineticket.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateOrderItemRequest {

    @NotNull(message = "Screening ID is required")
    private Long screeningId;

    @NotNull(message = "Seat ID is required")
    private Long seatId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    public CreateOrderItemRequest() {
    }

    public CreateOrderItemRequest(Long screeningId, Long seatId, Integer quantity) {
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.quantity = quantity;
    }

    public Long getScreeningId() { return screeningId; }
    public void setScreeningId(Long screeningId) { this.screeningId = screeningId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
