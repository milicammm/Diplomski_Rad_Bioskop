package com.cineticket.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScreeningRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Hall ID is required")
    private Long hallId;

    @NotNull(message = "Screening date is required")
    private LocalDate screeningDate;

    @NotNull(message = "Screening time is required")
    private LocalTime screeningTime;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    private Boolean active = true;

    public ScreeningRequest() {
    }

    public ScreeningRequest(Long movieId, Long hallId, LocalDate screeningDate, LocalTime screeningTime, BigDecimal price, Boolean active) {
        this.movieId = movieId;
        this.hallId = hallId;
        this.screeningDate = screeningDate;
        this.screeningTime = screeningTime;
        this.price = price;
        this.active = active;
    }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public LocalDate getScreeningDate() { return screeningDate; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }

    public LocalTime getScreeningTime() { return screeningTime; }
    public void setScreeningTime(LocalTime screeningTime) { this.screeningTime = screeningTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
