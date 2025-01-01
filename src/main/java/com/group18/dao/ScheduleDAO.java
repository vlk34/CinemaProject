// ScheduleDAO.java
package com.group18.dao;

import com.group18.model.Schedule;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    private Connection connection;

    public ScheduleDAO() {
        this.connection = DBConnection.getConnection();
    }

    public Schedule findById(int scheduleId) {
        String query = "SELECT * FROM schedules WHERE schedule_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractScheduleFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Schedule> getSchedulesByMovie(int movieId) {
        String query = "SELECT * FROM schedules WHERE movie_id = ?";
        List<Schedule> schedules = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        String query = "SELECT * FROM schedules WHERE session_date = ?";
        List<Schedule> schedules = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public List<Schedule> getAvailableSchedules(int movieId, LocalDate date) {
        String query = "SELECT * FROM schedules WHERE movie_id = ? AND session_date = ?";
        List<Schedule> schedules = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

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

    public boolean updateSchedule(Schedule schedule) {
        // Only allow updates if no tickets have been sold
        String query = """
            UPDATE schedules 
            SET movie_id = ?, hall_id = ?, session_date = ?, session_time = ?
            WHERE schedule_id = ? 
            AND NOT EXISTS (
                SELECT 1 FROM order_items 
                WHERE schedule_id = ? 
                AND item_type = 'ticket'
            )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, schedule.getMovieId());
            stmt.setInt(2, schedule.getHallId());
            stmt.setDate(3, Date.valueOf(schedule.getSessionDate()));
            stmt.setTime(4, Time.valueOf(schedule.getSessionTime()));
            stmt.setInt(5, schedule.getScheduleId());
            stmt.setInt(6, schedule.getScheduleId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getAvailableSeatsCount(int scheduleId) {
        String query = """
            SELECT h.capacity - COUNT(oi.order_item_id) as available_seats
            FROM schedules s
            JOIN halls h ON s.hall_id = h.hall_id
            LEFT JOIN order_items oi ON s.schedule_id = oi.schedule_id 
                AND oi.item_type = 'ticket'
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

    public boolean deleteSchedule(int scheduleId) {
        // Only allow deletion if no tickets have been sold for this schedule
        String query = """
            DELETE FROM schedules 
            WHERE schedule_id = ? 
            AND NOT EXISTS (
                SELECT 1 FROM order_items 
                WHERE schedule_id = ? 
                AND item_type = 'ticket'
            )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            stmt.setInt(2, scheduleId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isScheduleExists(int movieId, int hallId, LocalDate sessionDate, LocalTime sessionTime) {
        String query = "SELECT COUNT(*) FROM schedules " +
                "WHERE movie_id = ? " +
                "AND hall_id = ? " +
                "AND session_date = ? " +
                "AND session_time = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, hallId);
            stmt.setDate(3, Date.valueOf(sessionDate));
            stmt.setTime(4, Time.valueOf(sessionTime));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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