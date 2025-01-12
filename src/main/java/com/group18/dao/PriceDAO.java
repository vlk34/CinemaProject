package com.group18.dao;

import com.group18.model.PriceHistory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing price-related operations, including ticket prices,
 * age discounts, and logging price changes.
 */
public class PriceDAO {
    private Connection connection;

    /**
     * Constructs a new PriceDAO object and initializes the database connection.
     */
    public PriceDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Retrieves the price of tickets for a specific hall.
     *
     * @param hall the name of the hall
     * @return the ticket price for the specified hall
     */
    public double getTicketPrice(String hall) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT price FROM ticket_prices WHERE hall = ?")) {

            pstmt.setString(1, hall);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Updates the ticket price for a specific hall.
     *
     * @param hall     the name of the hall
     * @param newPrice the new ticket price to set
     * @return true if the price update was successful, false otherwise
     */
    public boolean updateTicketPrice(String hall, double newPrice) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE ticket_prices SET price = ? WHERE hall = ?")) {

            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, hall);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the current age discount rate.
     *
     * @return the discount rate for age-based discounts
     */
    public double getAgeDiscount() {
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT discount_rate FROM age_discounts WHERE discount_type = 'age'")) {

            if (rs.next()) {
                return rs.getDouble("discount_rate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Updates the age discount rate.
     *
     * @param newDiscount the new age discount rate
     * @return true if the discount update was successful, false otherwise
     */
    public boolean updateAgeDiscount(double newDiscount) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE age_discounts SET discount_rate = ? WHERE discount_type = 'age'")) {

            pstmt.setDouble(1, newDiscount);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Logs a price change to the price history.
     *
     * @param log the PriceHistory object containing details of the price change
     */
    public void logPriceChange(PriceHistory log) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO price_history (change_timestamp, item, old_price, new_price, updated_by) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setTimestamp(1, Timestamp.valueOf(log.getChangeTimestamp()));
            pstmt.setString(2, log.getItem());
            pstmt.setDouble(3, log.getOldPrice());
            pstmt.setDouble(4, log.getNewPrice());
            pstmt.setString(5, log.getUpdatedBy());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the price change history, ordered by the most recent changes.
     *
     * @return a list of PriceHistory objects representing the price change history
     */
    public List<PriceHistory> getPriceUpdateHistory() {
        String query = "SELECT * FROM price_history ORDER BY change_timestamp DESC";
        List<PriceHistory> history = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                PriceHistory log = new PriceHistory(
                        rs.getTimestamp("change_timestamp").toLocalDateTime(),
                        rs.getString("item"),
                        rs.getDouble("old_price"),
                        rs.getDouble("new_price"),
                        rs.getString("updated_by")
                );
                history.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}