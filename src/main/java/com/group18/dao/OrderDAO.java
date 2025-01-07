package com.group18.dao;

import com.group18.model.Order;
import com.group18.model.OrderItem;
import com.group18.model.Product;

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

        ProductDAO productDAO = new ProductDAO();

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

            // Create each order item and update stock
            try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                for (OrderItem item : order.getOrderItems()) {
                    System.out.println("Processing item type: " + item.getItemType());

                    itemStmt.setInt(1, order.getOrderId());
                    itemStmt.setString(2, item.getItemType());

                    // Handle schedule_id based on item type
                    if ("ticket".equals(item.getItemType())) {
                        itemStmt.setInt(3, item.getScheduleId());  // For tickets, use actual schedule_id
                        itemStmt.setInt(4, item.getSeatNumber());  // For tickets, set seat number
                    } else {
                        itemStmt.setNull(3, Types.INTEGER);  // For products, set NULL
                        itemStmt.setNull(4, Types.INTEGER);  // For products, set NULL
                    }

                    itemStmt.setBoolean(5, item.getDiscountApplied());

                    // Handle customer names based on item type
                    if ("ticket".equals(item.getItemType())) {
                        itemStmt.setString(6, item.getOccupantFirstName());
                        itemStmt.setString(7, item.getOccupantLastName());
                    } else {
                        itemStmt.setNull(6, Types.VARCHAR);
                        itemStmt.setNull(7, Types.VARCHAR);
                    }

                    // Handle product_id based on item type
                    if ("product".equals(item.getItemType())) {
                        itemStmt.setInt(8, item.getProductId());
                        // Process product stock reduction
                        if (!productDAO.decreaseStock(item.getProductId(), item.getQuantity())) {
                            throw new SQLException("Failed to decrease stock for product: " + item.getProductId());
                        }
                    } else {
                        itemStmt.setNull(8, Types.INTEGER);
                    }

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

    // Method to restore product stock when a cancellation is processed
    public boolean restoreProductStock(int orderId) {
        String query = """
            SELECT product_id, quantity 
            FROM order_items 
            WHERE order_id = ? AND item_type = 'product' 
            AND product_id IS NOT NULL
        """;

        ProductDAO productDAO = new ProductDAO();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");

                // Restore stock for each product
                if (!productDAO.increaseStock(productId, quantity)) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to restore seats when a cancellation is processed
    public boolean restoreSeats(int orderId) {
        String query = """
            UPDATE schedules s
            SET s.available_seats = s.available_seats + 
                (SELECT COUNT(*) 
                 FROM order_items oi 
                 WHERE oi.order_id = ? 
                 AND oi.item_type = 'ticket' 
                 AND oi.schedule_id = s.schedule_id)
            WHERE EXISTS (
                SELECT 1 
                FROM order_items oi 
                WHERE oi.order_id = ? 
                AND oi.item_type = 'ticket' 
                AND oi.schedule_id = s.schedule_id
            )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    // Modified processCancellation to handle both product stock and seats
    public boolean processCancellation(int orderId) {
        try {
            connection.setAutoCommit(false);

            // Restore product stock
            if (!restoreProductStock(orderId)) {
                connection.rollback();
                return false;
            }

            // Restore seat availability
            if (!restoreSeats(orderId)) {
                connection.rollback();
                return false;
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

    public boolean storeReceipt(int orderId, byte[] receiptPdf) {
        String query = "UPDATE orders SET receipt_pdf = ? WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBytes(1, receiptPdf);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] retrieveReceipt(int orderId) {
        String query = "SELECT receipt_pdf FROM orders WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("receipt_pdf");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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