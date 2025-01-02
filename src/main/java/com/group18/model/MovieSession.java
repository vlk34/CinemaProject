package com.group18.model;

import java.time.LocalTime;

public class MovieSession {
    private int scheduleId;  // Added field
    private String hall;
    private LocalTime time;
    private int availableSeats;

    public MovieSession(int scheduleId, String hall, LocalTime time, int availableSeats) {
        this.scheduleId = scheduleId;
        this.hall = hall;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    // Added getter
    public int getScheduleId() { return scheduleId; }

    // Existing getters
    public String getHall() { return hall; }
    public LocalTime getTime() { return time; }
    public int getAvailableSeats() { return availableSeats; }
}