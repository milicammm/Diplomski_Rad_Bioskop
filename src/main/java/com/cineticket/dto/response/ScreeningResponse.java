package com.cineticket.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;


public class ScreeningResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private String posterUrl;
    private Long hallId;
    private String hallName;
    private LocalDate screeningDate;
    private LocalTime screeningTime;
    private BigDecimal price;
    private Integer availableSeats;
    private Integer totalSeats;
    private Boolean active;
    private Long soldTickets;
    private LocalDateTime createdAt;

    public ScreeningResponse() {
    }

    public ScreeningResponse(Long id, Long movieId, String movieTitle, String posterUrl, Long hallId,
                             String hallName, LocalDate screeningDate, LocalTime screeningTime, BigDecimal price,
                             Integer availableSeats, Integer totalSeats, Boolean active, Long soldTickets, LocalDateTime createdAt) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.hallId = hallId;
        this.hallName = hallName;
        this.screeningDate = screeningDate;
        this.screeningTime = screeningTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.active = active;
        this.soldTickets = soldTickets;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public LocalDate getScreeningDate() { return screeningDate; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }

    public LocalTime getScreeningTime() { return screeningTime; }
    public void setScreeningTime(LocalTime screeningTime) { this.screeningTime = screeningTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getSoldTickets() { return soldTickets; }
    public void setSoldTickets(Long soldTickets) { this.soldTickets = soldTickets; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
