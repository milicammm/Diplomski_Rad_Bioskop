package com.cineticket.dto.response;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long screeningId;
    private String movieTitle;
    private String hallName;
    private String screeningDateTime;
    private Long seatId;
    private String seatInfo;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(Long id, Long screeningId, String movieTitle, String hallName, String screeningDateTime,
                             Long seatId, String seatInfo, Integer quantity, BigDecimal price, BigDecimal subtotal) {
        this.id = id;
        this.screeningId = screeningId;
        this.movieTitle = movieTitle;
        this.hallName = hallName;
        this.screeningDateTime = screeningDateTime;
        this.seatId = seatId;
        this.seatInfo = seatInfo;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScreeningId() { return screeningId; }
    public void setScreeningId(Long screeningId) { this.screeningId = screeningId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getScreeningDateTime() { return screeningDateTime; }
    public void setScreeningDateTime(String screeningDateTime) { this.screeningDateTime = screeningDateTime; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public String getSeatInfo() { return seatInfo; }
    public void setSeatInfo(String seatInfo) { this.seatInfo = seatInfo; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
