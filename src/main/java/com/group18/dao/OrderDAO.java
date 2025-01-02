package com.group18.dao;

import com.group18.model.Order;
import com.group18.model.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class OrderDAO {
    private Connection connection;

    public OrderDAO() {
        this.connection = DBConnection.getConnection();
    }

    public boolean createOrder(Order order) {
        String orderQuery = "INSERT INTO orders (cashier_id, order_date, total_price, status) VALUES (?, ?, ?, 'PENDING')";
        String itemQuery = "INSERT INTO order_items (order_id, item_type, schedule_id, seat_number, " +
                "discount_applied, occupant_first_name, occupant_last_name, product_id, " +
                "quantity, item_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            // Create the order first
            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getCashierId());
                orderStmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
                orderStmt.setBigDecimal(3, order.getTotalPrice());

                int affectedRows = orderStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            // Create each order item
            try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                for (OrderItem item : order.getOrderItems()) {
                    itemStmt.setInt(1, order.getOrderId());
                    itemStmt.setString(2, item.getItemType());
                    itemStmt.setObject(3, item.getScheduleId());
                    itemStmt.setObject(4, item.getSeatNumber());
                    itemStmt.setBoolean(5, item.getDiscountApplied());
                    itemStmt.setString(6, item.getOccupantFirstName());
                    itemStmt.setString(7, item.getOccupantLastName());
                    itemStmt.setObject(8, item.getProductId());
                    itemStmt.setInt(9, item.getQuantity());
                    itemStmt.setBigDecimal(10, item.getItemPrice());

                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
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

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ?";
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderItems(getOrderItemsForOrder(order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getAllOrders() {
        String query = "SELECT * FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderItems(getOrderItemsForOrder(order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean processCancellation(int orderId) {
        try {
            connection.setAutoCommit(false);

            // First get all items to restore inventory
            List<OrderItem> items = getOrderItemsForOrder(orderId);
            ProductDAO productDAO = new ProductDAO();

            // Restore product inventory
            for (OrderItem item : items) {
                if ("product".equals(item.getItemType()) && item.getProductId() != null) {
                    productDAO.increaseStock(item.getProductId(), item.getQuantity());
                }
            }

            // Update order status
            String updateQuery = "UPDATE orders SET status = 'PROCESSED' WHERE order_id = ? AND status = 'PENDING'";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, orderId);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
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

    public boolean rejectCancellation(int orderId) {
        String query = "UPDATE orders SET status = 'REJECTED' WHERE order_id = ? AND status = 'PENDING'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getPendingCancellations() {
        String query = "SELECT * FROM orders WHERE status = 'PENDING' ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderItems(getOrderItemsForOrder(order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public CancellationStats getCancellationStats() {
        String query = """
            SELECT 
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
                COUNT(CASE WHEN status = 'PROCESSED' AND DATE(order_date) = CURRENT_DATE THEN 1 END) as processed_today,
                SUM(CASE WHEN status = 'PROCESSED' AND DATE(order_date) = CURRENT_DATE THEN total_price END) as refunded_today
            FROM orders
            WHERE status IN ('PENDING', 'PROCESSED')
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return new CancellationStats(
                        rs.getInt("pending_count"),
                        rs.getInt("processed_today"),
                        rs.getBigDecimal("refunded_today")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new CancellationStats(0, 0, BigDecimal.ZERO);
    }

    private List<OrderItem> getOrderItemsForOrder(int orderId) {
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


    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setCashierId(rs.getInt("cashier_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getString("status")); // Added status
        return order;
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

    public static class CancellationStats {
        private final int pendingCount;
        private final int processedToday;
        private final BigDecimal refundedToday;

        public CancellationStats(int pendingCount, int processedToday, BigDecimal refundedToday) {
            this.pendingCount = pendingCount;
            this.processedToday = processedToday;
            this.refundedToday = refundedToday != null ? refundedToday : BigDecimal.ZERO;
        }

        public int getPendingCount() { return pendingCount; }
        public int getProcessedToday() { return processedToday; }
        public BigDecimal getRefundedToday() { return refundedToday; }
    }
}