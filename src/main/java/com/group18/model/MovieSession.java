package com.group18.model;

import java.time.LocalTime;

public class MovieSession {
    private String hall;
    private LocalTime time;
    private int availableSeats;

    public MovieSession(String hall, LocalTime time, int availableSeats) {
        this.hall = hall;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    // Getters
    public String getHall() { return hall; }
    public LocalTime getTime() { return time; }
    public int getAvailableSeats() { return availableSeats; }
}
