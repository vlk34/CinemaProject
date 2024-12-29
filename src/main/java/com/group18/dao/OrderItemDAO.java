package com.group18.dao;

import com.group18.model.OrderItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class OrderItemDAO {
    private Connection connection;

    public OrderItemDAO() {
        this.connection = DBConnection.getConnection();
    }

    public boolean addOrderItem(OrderItem item) {
        String query = "INSERT INTO order_items (order_id, item_type, schedule_id, seat_number, " +
                "discount_applied, occupant_first_name, occupant_last_name, product_id, " +
                "quantity, item_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setString(2, item.getItemType());
            stmt.setObject(3, item.getScheduleId());
            stmt.setObject(4, item.getSeatNumber());
            stmt.setBoolean(5, item.getDiscountApplied());
            stmt.setString(6, item.getOccupantFirstName());
            stmt.setString(7, item.getOccupantLastName());
            stmt.setObject(8, item.getProductId());
            stmt.setInt(9, item.getQuantity());
            stmt.setBigDecimal(10, item.getItemPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    item.setOrderItemId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<OrderItem> getItemsByOrderId(int orderId) {
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(extractOrderItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean deleteOrderItem(int orderItemId) {
        String query = "DELETE FROM order_items WHERE order_item_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderItem> getTicketsBySchedule(int scheduleId) {
        String query = "SELECT * FROM order_items WHERE schedule_id = ? AND item_type = 'ticket'";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(extractOrderItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean checkSeatAvailability(int scheduleId, int seatNumber) {
        String query = "SELECT COUNT(*) FROM order_items WHERE schedule_id = ? AND seat_number = ? AND item_type = 'ticket'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            stmt.setInt(2, seatNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // If count is 0, seat is available
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private OrderItem extractOrderItemFromResultSet(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setItemType(rs.getString("item_type"));
        item.setScheduleId((Integer) rs.getObject("schedule_id"));
        item.setSeatNumber((Integer) rs.getObject("seat_number"));
        item.setDiscountApplied(rs.getBoolean("discount_applied"));
        item.setOccupantFirstName(rs.getString("occupant_first_name"));
        item.setOccupantLastName(rs.getString("occupant_last_name"));
        item.setProductId((Integer) rs.getObject("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setItemPrice(rs.getBigDecimal("item_price"));
        return item;
    }
}