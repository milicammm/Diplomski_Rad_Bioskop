package com.cineticket.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer duration;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private String director;
    private String cast;
    private String language;
    private Boolean active;
    private Long soldTickets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MovieResponse() {
    }

    public MovieResponse(Long id, String title, String description, String genre, Integer duration,
                         String posterUrl, String trailerUrl, LocalDate releaseDate, String director,
                         String cast, String language, Boolean active, Long soldTickets, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.releaseDate = releaseDate;
        this.director = director;
        this.cast = cast;
        this.language = language;
        this.active = active;
        this.soldTickets = soldTickets;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getSoldTickets() { return soldTickets; }
    public void setSoldTickets(Long soldTickets) { this.soldTickets = soldTickets; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
