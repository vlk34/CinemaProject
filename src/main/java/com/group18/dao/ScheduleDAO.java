// ScheduleDAO.java
package com.group18.dao;

import com.group18.model.Schedule;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for interacting with the "schedules" table in the database.
 * Provides methods for CRUD operations related to movie schedules.
 */
public class ScheduleDAO {
    private Connection connection;

    /**
     * Constructs a new ScheduleDAO object with a database connection.
     */
    public ScheduleDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Retrieves all schedules from the "schedules" table.
     *
     * @return A list of all schedules in the database.
     */
    public List<Schedule> getAllSchedules() {
        String query = "SELECT * FROM schedules";
        List<Schedule> schedules = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * Retrieves schedules for a given month and year.
     *
     * @param monthDate The month and year to filter schedules by.
     * @return A list of schedules that occur in the specified month.
     */
    public List<Schedule> getSchedulesByMonth(LocalDate monthDate) {
        String query = "SELECT * FROM schedules WHERE MONTH(session_date) = ? AND YEAR(session_date) = ?";
        List<Schedule> schedules = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, monthDate.getMonthValue());
            stmt.setInt(2, monthDate.getYear());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * Creates a new schedule in the database.
     *
     * @param schedule The schedule object to be inserted.
     * @return True if the schedule was created successfully, false otherwise.
     */
    public boolean createSchedule(Schedule schedule) {
        String query = "INSERT INTO schedules (movie_id, hall_id, session_date, session_time) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getMovieId());
            stmt.setInt(2, schedule.getHallId());
            stmt.setDate(3, Date.valueOf(schedule.getSessionDate()));
            stmt.setTime(4, Time.valueOf(schedule.getSessionTime()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    schedule.setScheduleId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the count of available seats for a specific schedule.
     *
     * @param scheduleId The ID of the schedule.
     * @return The number of available seats for the given schedule.
     */
    public int getAvailableSeatsCount(int scheduleId) {
        String query = """
        SELECT h.capacity - COUNT(DISTINCT oi.seat_number) as available_seats
        FROM schedules s
        JOIN halls h ON s.hall_id = h.hall_id
        LEFT JOIN order_items oi ON s.schedule_id = oi.schedule_id 
            AND oi.item_type = 'ticket'
            AND oi.order_id IN (
                SELECT order_id 
                FROM orders 
                WHERE status NOT IN ('PROCESSED_FULL', 'PROCESSED_TICKETS')
            )
        WHERE s.schedule_id = ?
        GROUP BY s.schedule_id, h.capacity
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("available_seats");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean hasSchedules(int movieId) {
        String query = "SELECT COUNT(*) FROM schedules WHERE movie_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a schedule from the database.
     * A schedule can only be deleted if no tickets have been sold for it.
     *
     * @param scheduleId The ID of the schedule to be deleted.
     * @return True if the schedule was successfully deleted, false otherwise.
     */
    public boolean deleteSchedule(int scheduleId) {
        try {
            connection.setAutoCommit(false);

            // First, update order_items to remove reference to this schedule for processed/cancelled orders
            String updateOrderItemsQuery = """
            UPDATE order_items 
            SET schedule_id = NULL 
            WHERE schedule_id = ? 
            AND order_id IN (
                SELECT order_id 
                FROM orders 
                WHERE status IN ('PROCESSED_FULL', 'PROCESSED_TICKETS', 'REJECTED')
            )
        """;

            try (PreparedStatement updateStmt = connection.prepareStatement(updateOrderItemsQuery)) {
                updateStmt.setInt(1, scheduleId);
                updateStmt.executeUpdate();
            }

            // Then delete the schedule
            String deleteQuery = """
            DELETE FROM schedules 
            WHERE schedule_id = ? 
            AND NOT EXISTS (
                SELECT 1 FROM order_items oi
                WHERE oi.schedule_id = ? 
                AND oi.item_type = 'ticket'
                AND oi.order_id IN (
                    SELECT order_id 
                    FROM orders 
                    WHERE status NOT IN ('PROCESSED_FULL', 'PROCESSED_TICKETS', 'REJECTED')
                )
            )
        """;

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, scheduleId);
                deleteStmt.setInt(2, scheduleId);

                int affectedRows = deleteStmt.executeUpdate();

                connection.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if a schedule already exists for a given hall, session date, and time.
     *
     * @param hallId      The ID of the hall.
     * @param sessionDate The session date.
     * @param sessionTime The session time.
     * @return True if the schedule exists, false otherwise.
     */
    public boolean isScheduleExists(int hallId, LocalDate sessionDate, LocalTime sessionTime) {
        String query = "SELECT COUNT(*) FROM schedules " +
                "WHERE hall_id = ? " +
                "AND session_date = ? " +
                "AND session_time = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, hallId);
            stmt.setDate(2, Date.valueOf(sessionDate));
            stmt.setTime(3, Time.valueOf(sessionTime));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves schedules for a specific movie between two dates.
     *
     * @param movieId   The ID of the movie.
     * @param startDate The start date of the range.
     * @param endDate   The end date of the range.
     * @return A list of schedules for the specified movie within the given date range.
     */
    public List<Schedule> getSchedulesBetweenDates(int movieId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT * FROM schedules WHERE movie_id = ? AND session_date BETWEEN ? AND ? ORDER BY session_date";
        List<Schedule> schedules = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * Extracts a schedule object from the current row of a ResultSet.
     *
     * @param rs The ResultSet object containing the schedule data.
     * @return A Schedule object populated with the data from the ResultSet.
     * @throws SQLException If an error occurs while accessing the ResultSet.
     */
    private Schedule extractScheduleFromResultSet(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setMovieId(rs.getInt("movie_id"));
        schedule.setHallId(rs.getInt("hall_id"));
        schedule.setSessionDate(rs.getDate("session_date").toLocalDate());
        schedule.setSessionTime(rs.getTime("session_time").toLocalTime());
        return schedule;
    }
}