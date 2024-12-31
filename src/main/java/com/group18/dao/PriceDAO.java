package com.group18.dao;

import com.group18.model.PriceHistory;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PriceDAO {
    private Connection connection;

    public PriceDAO() {
        this.connection = DBConnection.getConnection();
    }

    // Ticket Prices
    public double getTicketPrice(String hall) {
        String query = "SELECT price FROM ticket_prices WHERE hall = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, hall);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateTicketPrice(String hall, double newPrice) {
        String query = "UPDATE ticket_prices SET price = ? WHERE hall = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, hall);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Age Discounts
    public double getAgeDiscount() {
        String query = "SELECT discount_rate FROM age_discounts WHERE discount_type = 'age'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble("discount_rate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateAgeDiscount(double newDiscount) {
        String query = "UPDATE age_discounts SET discount_rate = ? WHERE discount_type = 'age'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, newDiscount);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Price History
    public void logPriceChange(PriceHistory log) {
        String query = "INSERT INTO price_history (change_date, item, old_price, new_price, updated_by) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(log.getChangeDate()));
            pstmt.setString(2, log.getItem());
            pstmt.setDouble(3, log.getOldPrice());
            pstmt.setDouble(4, log.getNewPrice());
            pstmt.setString(5, log.getUpdatedBy());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PriceHistory> getPriceUpdateHistory() {
        List<PriceHistory> history = new ArrayList<>();
        String query = "SELECT * FROM price_history ORDER BY change_date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                PriceHistory log = new PriceHistory(
                        rs.getDate("change_date").toLocalDate(),
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