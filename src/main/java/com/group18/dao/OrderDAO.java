package com.group18.dao;

import com.group18.model.Order;
import com.group18.model.OrderItem;
import com.group18.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object (DAO) for interacting with the 'orders' and 'order_items' tables in the database.
 * Provides methods for creating, retrieving, updating, and deleting orders and order items.
 */
public class OrderDAO {
    private Connection connection;

    /**
     * Constructs an OrderDAO object and establishes a connection to the database.
     */
    public OrderDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Creates a new order and associated order items in the database.
     *
     * @param order The Order object containing the order details.
     * @return true if the order is successfully created; false otherwise.
     */
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


    /**
     * Restores the stock of products when an order is canceled.
     *
     * @param orderId The ID of the order to be canceled.
     * @return true if the product stock is successfully restored; false otherwise.
     */
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

    /**
     * Restores the available seats for tickets when an order is canceled.
     *
     * @param orderId The ID of the order to be canceled.
     * @return true if the seats are successfully restored; false otherwise.
     */
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

    /**
     * Retrieves orders from the database that fall within the specified date range.
     *
     * @param startDate The start of the date range.
     * @param endDate The end of the date range.
     * @return A list of orders within the specified date range.
     */
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

    /**
     * Retrieves all orders from the database.
     *
     * @return A list of all orders in the database.
     */
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

    /**
     * Processes the cancellation of an order by updating stock and seat availability.
     * Also calculates any applicable refunds.
     *
     * @param orderId The ID of the order to be canceled.
     * @param cancelProducts Whether to cancel the products in the order.
     * @param cancelTickets Whether to cancel the tickets in the order.
     * @return true if the cancellation is successfully processed; false otherwise.
     */
    public boolean processCancellation(int orderId, boolean cancelProducts, boolean cancelTickets) {
        try {
            connection.setAutoCommit(false);
            boolean productRestored = true;
            boolean seatsRestored = true;

            // Conditional product stock restoration
            if (cancelProducts) {
                productRestored = restoreProductStock(orderId);
                if (!productRestored) {
                    connection.rollback();
                    return false;
                }
            }

            // Conditional seat availability restoration
            if (cancelTickets) {
                seatsRestored = restoreSeats(orderId);
                if (!seatsRestored) {
                    connection.rollback();
                    return false;
                }
            }

            // Determine final order status
            String newStatus;
            if (cancelProducts && cancelTickets) {
                newStatus = "PROCESSED_FULL";
            } else if (cancelProducts) {
                newStatus = "PROCESSED_PRODUCTS";
            } else if (cancelTickets) {
                newStatus = "PROCESSED_TICKETS";
            } else {
                connection.rollback();
                return false;
            }

            // Calculate refund amounts for products and tickets with tax
            BigDecimal refundAmount = BigDecimal.ZERO;

            if (cancelProducts) {
                String productRefundQuery = """
                SELECT COALESCE(SUM(
                    (item_price * quantity) * (1 + 0.10)  -- 10% tax for products
                ), 0) 
                FROM order_items 
                WHERE order_id = ? AND item_type = 'product'
            """;
                try (PreparedStatement stmt = connection.prepareStatement(productRefundQuery)) {
                    stmt.setInt(1, orderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            refundAmount = refundAmount.add(rs.getBigDecimal(1));
                        }
                    }
                }
            }

            if (cancelTickets) {
                String ticketRefundQuery = """
                SELECT COALESCE(SUM(
                    (item_price * quantity) * (1 + 0.20)  -- 20% tax for tickets
                ), 0) 
                FROM order_items 
                WHERE order_id = ? AND item_type = 'ticket'
            """;
                try (PreparedStatement stmt = connection.prepareStatement(ticketRefundQuery)) {
                    stmt.setInt(1, orderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            refundAmount = refundAmount.add(rs.getBigDecimal(1));
                        }
                    }
                }
            }

            // Update order status and refunded amount
            String updateQuery = "UPDATE orders SET status = ?, refunded_amount = refunded_amount + ? WHERE order_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setString(1, newStatus);

                // Add the calculated refund amount (including tax) to refunded_amount
                stmt.setBigDecimal(2, refundAmount);
                stmt.setInt(3, orderId);

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

    /**
     * Rejects the cancellation of an order.
     *
     * @param orderId The ID of the order to be rejected.
     * @return true if the cancellation is successfully rejected; false otherwise.
     */
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

    /**
     * Retrieves statistics about cancellations, including pending cancellations and processed cancellations for today.
     *
     * @return A CancellationStats object containing cancellation statistics.
     */
    public CancellationStats getCancellationStats() {
        String query = """
        SELECT 
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
            COUNT(CASE WHEN status IN ('PROCESSED_FULL', 'PROCESSED_TICKETS', 'PROCESSED_PRODUCTS') AND DATE(order_date) = CURRENT_DATE THEN 1 END) as processed_today,
            SUM(CASE WHEN status IN ('PROCESSED_FULL', 'PROCESSED_TICKETS', 'PROCESSED_PRODUCTS') AND DATE(order_date) = CURRENT_DATE THEN refunded_amount END) as refunded_today
        FROM orders
        WHERE status IN ('PENDING', 'PROCESSED_FULL', 'PROCESSED_TICKETS', 'PROCESSED_PRODUCTS')
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

    /**
     * Retrieves all order items associated with a specific order.
     *
     * @param orderId The ID of the order.
     * @return A list of order items for the specified order.
     */
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

    /**
     * Stores receipt and ticket PDFs for an order.
     *
     * @param orderId The ID of the order.
     * @param receiptPdf The receipt PDF as a byte array.
     * @param ticketsPdf The tickets PDF as a byte array.
     * @return true if the PDFs are successfully stored; false otherwise.
     */
    public boolean storeDocuments(int orderId, byte[] receiptPdf, byte[] ticketsPdf) {
        String query = "UPDATE orders SET receipt_pdf = ?, tickets_pdf = ? WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            // Handle null PDFs gracefully
            if (receiptPdf != null) {
                stmt.setBytes(1, receiptPdf);
            } else {
                stmt.setNull(1, Types.BLOB);
            }

            if (ticketsPdf != null) {
                stmt.setBytes(2, ticketsPdf);
            } else {
                stmt.setNull(2, Types.BLOB);
            }

            stmt.setInt(3, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the receipt PDF for a specific order.
     *
     * @param orderId The ID of the order.
     * @return The receipt PDF as a byte array, or null if not found.
     */
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

    /**
     * Retrieves the tickets PDF for a specific order.
     *
     * @param orderId The ID of the order.
     * @return The tickets PDF as a byte array, or null if not found.
     */
    public byte[] retrieveTickets(int orderId) {
        String query = "SELECT tickets_pdf FROM orders WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("tickets_pdf");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts order details from a ResultSet.
     *
     * @param rs The ResultSet containing the order data.
     * @return An Order object populated with the data from the ResultSet.
     * @throws SQLException If an SQL error occurs while extracting the data.
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setCashierId(rs.getInt("cashier_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getString("status")); // Added status
        return order;
    }

    /**
     * Extracts order item details from a ResultSet.
     *
     * @param rs The ResultSet containing the order item data.
     * @return An OrderItem object populated with the data from the ResultSet.
     * @throws SQLException If an SQL error occurs while extracting the data.
     */
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

    /**
     * Represents statistics related to order cancellations.
     */
    public static class CancellationStats {
        private final int pendingCount;
        private final int processedToday;
        private final BigDecimal refundedToday;

        /**
         * Constructs a CancellationStats object.
         *
         * @param pendingCount The number of pending cancellations.
         * @param processedToday The number of cancellations processed today.
         * @param refundedToday The total amount refunded for cancellations today.
         */
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