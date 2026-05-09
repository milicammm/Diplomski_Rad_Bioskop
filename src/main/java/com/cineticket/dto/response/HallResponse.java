package com.cineticket.dto.response;

public class HallResponse {

    private Long id;
    private String name;
    private Integer capacity;
    private String description;

    public HallResponse() {
    }

    public HallResponse(Long id, String name, Integer capacity, String description) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
