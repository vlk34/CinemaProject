package com.group18.model;

import java.time.LocalTime;

/**
 * Represents a movie session for a specific movie in a cinema hall.
 * This class provides details such as the session's schedule ID, hall, time,
 * and the number of available seats.
 */
public class MovieSession {
    /**
     * The unique identifier for the schedule of the movie session.
     * It is used to distinguish different movie session schedules.
     */
    private int scheduleId;  // Added field
    /**
     * The name or identifier of the cinema hall where the movie session takes place.
     */
    private String hall;
    /**
     * The time at which the movie session is scheduled to start.
     */
    private LocalTime time;
    /**
     * Represents the number of seats currently available for this movie session.
     * It indicates how many more attendees can be accommodated in the session's hall.
     */
    private int availableSeats;

    /**
     * Constructs a new {@code MovieSession} object with the specified schedule ID, hall name, time,
     * and the number of available seats.
     *
     * @param scheduleId     the unique identifier for the movie session schedule
     * @param hall           the name of the hall where the movie session will take place
     * @param time           the time at which the movie session is scheduled to start
     * @param availableSeats the number of seats available for the movie session
     */
    public MovieSession(int scheduleId, String hall, LocalTime time, int availableSeats) {
        this.scheduleId = scheduleId;
        this.hall = hall;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    /**
     * Retrieves the schedule ID of the movie session.
     *
     * @return the schedule ID of this movie session.
     */
    // Added getter
    public int getScheduleId() { return scheduleId; }

    /**
     * Retrieves the name of the cinema hall where the movie session is scheduled.
     *
     * @return the name of the cinema hall as a String
     */
    // Existing getters
    public String getHall() { return hall; }
    /**
     * Retrieves the time of the movie session.
     *
     * @return the time of the movie session as a {@link LocalTime} object.
     */
    public LocalTime getTime() { return time; }
    /**
     * Retrieves the number of available seats for the movie session.
     *
     * @return the number of available seats as an integer.
     */
    public int getAvailableSeats() { return availableSeats; }
}