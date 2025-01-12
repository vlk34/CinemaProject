package com.group18.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This class represents the scheduling details for a movie session.
 * It includes information such as the movie ID, hall ID, session date, session time,
 * and the number of available seats for the session.
 */
public class Schedule {
    /**
     * Represents the unique identifier for the schedule.
     * This ID distinguishes each schedule entry in the system.
     */
    private int scheduleId;
    /**
     * Represents the unique identifier for a movie associated with a specific schedule.
     * This ID is used to reference a movie in the system.
     */
    private int movieId;
    /**
     * Identifier for the hall where the movie session is scheduled.
     * This ID references the corresponding entry in the halls table.
     */
    private int hallId;  // References halls table
    /**
     * Represents the date of a movie session.
     * This field is used to specify on which date a session is scheduled.
     */
    private LocalDate sessionDate;
    /**
     * Represents the time at which a movie session is scheduled to start.
     * This field is used in conjunction with the session date to define when
     * a specific movie session will take place.
     */
    private LocalTime sessionTime;
    /**
     * Represents the number of available seats for a movie session.
     * This value indicates the count of seats that have not been booked yet.
     * HOWEVER, THIS IS NOT BEING USED, Instead we retrieve them from the current orders.
     */
    private int availableSeats;
    /**
     * Constructs a new, empty Schedule object.
     * This constructor initializes an instance of the Schedule class without setting any properties.
     */
    public Schedule() {}

    /**
     * Constructs a {@code Schedule} instance with the specified movie ID, hall ID, session date,
     * and session time.
     *
     * @param movieId      the unique identifier of the movie being scheduled
     * @param hallId       the unique identifier of the hall where the session will take place
     * @param sessionDate  the date on which the movie session will occur
     * @param sessionTime  the time at which the movie session will start
     */
    public Schedule(int movieId, int hallId, LocalDate sessionDate, LocalTime sessionTime) {
        this.movieId = movieId;
        this.hallId = hallId;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
    }

    /**
     * Retrieves the number of available seats for the movie session.
     * HOWEVER, THIS IS NOT BEING USED, Instead we retrieve them from the current orders.
     *
     * @return the number of available seats
     */
    public int getAvailableSeats() {
        return availableSeats;
    }

    /**
     * Sets the number of available seats for the movie session.
     * HOWEVER, THIS IS NOT BEING USED, Instead we retrieve them from the current orders.
     *
     * @param availableSeats the number of seats available for this session
     */
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    /**
     * Retrieves the unique identifier of the schedule.
     *
     * @return The schedule ID.
     */
    // Getters and Setters
    public int getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the ID for the schedule.
     *
     * @param scheduleId The unique identifier for the schedule.
     */
    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * Retrieves the ID of the movie associated with this schedule.
     *
     * @return The movie ID.
     */
    public int getMovieId() {
        return movieId;
    }

    /**
     * Sets the ID of the movie associated with the schedule.
     *
     * @param movieId The ID of the movie to be set.
     */
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    /**
     * Retrieves the ID of the hall associated with this schedule.
     *
     * @return The hall ID as an integer.
     */
    public int getHallId() {
        return hallId;
    }

    /**
     * Sets the ID of the hall for the schedule.
     *
     * @param hallId The ID of the hall to be associated with the schedule.
     */
    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    /**
     * Retrieves the session date associated with the schedule.
     *
     * @return The session date as a LocalDate object.
     */
    public LocalDate getSessionDate() {
        return sessionDate;
    }

    /**
     * Sets the session date for the schedule.
     *
     * @param sessionDate The date of the session to be scheduled.
     */
    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    /**
     * Retrieves the session time for the schedule.
     *
     * @return The session time as a LocalTime object.
     */
    public LocalTime getSessionTime() {
        return sessionTime;
    }

    /**
     * Sets the session time for the schedule.
     *
     * @param sessionTime The time of the session.
     */
    public void setSessionTime(LocalTime sessionTime) {
        this.sessionTime = sessionTime;
    }
}