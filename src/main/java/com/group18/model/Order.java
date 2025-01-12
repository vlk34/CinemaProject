package com.group18.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Represents an order in a transactional system. This class contains information
 * about the order's identification, associated cashier, order date, total price,
 * status, and the list of items included in the order. It also provides methods
 * to manage the items in the order and calculate the total price dynamically.
 */
public class Order {
    /**
     * A unique identifier for the order.
     * This ID serves as a primary key to distinguish each order within the system.
     */
    private int orderId;
    /**
     * Represents the unique identifier of the cashier associated with the order.
     * This identifier is used to track which cashier executed or handled the order in the system.
     */
    private int cashierId;
    /**
     * Represents the date and time when the order was created.
     * This field is used to track when the order was placed
     * in the transactional system.
     */
    private LocalDateTime orderDate;
    /**
     * Represents the total price of all items in the order.
     * This variable is dynamically updated based on the current list of items in the order.
     * It is calculated as the sum of the prices of all items included in the order.
     */
    private BigDecimal totalPrice;
    /**
     * Represents the list of items included in the order.
     * Each item in the list is represented by an instance of the {@code OrderItem} class.
     * This list is used to manage all the order's items dynamically and calculate
     * the total price of the order based on the associated items.
     */
    private List<OrderItem> orderItems;
    /**
     * Represents the current status of the order.
     * Possible values include "PENDING", "COMPLETED", "CANCELLED", or other
     * custom-defined statuses depending on the business logic.
     *
     * Default value is "PENDING".
     */
    private String status = "PENDING";
    /**
     * Represents the total amount that has been refunded for the order.
     * This value is initialized to zero and is updated whenever a refund is processed.
     */
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    /**
     * Retrieves the total refunded amount for the order.
     *
     * @return the refunded amount as a BigDecimal
     */
    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    /**
     * Sets the refunded amount for the order.
     *
     * @param refundedAmount the amount to set as refunded, represented as a BigDecimal
     */
    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    /**
     * Constructs a new Order instance with default values.
     * Initializes an empty list of order items, sets the order date
     * to the current date and time, and initializes the total price to zero.
     */
    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.totalPrice = BigDecimal.ZERO;
    }

    /**
     * Retrieves the unique identifier of the order.
     *
     * @return the order ID as an integer.
     */
    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the unique identifier for this order.
     *
     * @param orderId The unique identifier to assign to the order.
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Retrieves the identifier of the cashier associated with this order.
     *
     * @return the cashier ID as an integer.
     */
    public int getCashierId() {
        return cashierId;
    }

    /**
     * Sets the identifier of the cashier associated with the order.
     *
     * @param cashierId the unique identifier of the cashier to be assigned to the order
     */
    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    /**
     * Retrieves the date and time when the order was created.
     *
     * @return the order creation date and time as a {@code LocalDateTime}.
     */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the date and time when the order was placed.
     * This value is used to record the specific moment an order is created or updated.
     *
     * @param orderDate the date and time of the order as a {@code LocalDateTime} object
     */
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Retrieves the total price of the order.
     *
     * @return The total price as a BigDecimal.
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets the total price of the order.
     *
     * @param totalPrice the total price to be set for the order as a BigDecimal
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Retrieves the list of items associated with the order.
     *
     * @return a list of OrderItem objects representing the items in the order.
     */
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    /**
     * Sets the list of items included in the order.
     *
     * @param orderItems the list of {@link OrderItem} representing the items in the order
     */
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    /**
     * Adds an OrderItem to the list of order items and updates the total price of the order.
     *
     * @param item the OrderItem to be added to the order
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        updateTotalPrice();
    }

    /**
     * Removes the specified order item from the list of items in the order
     * and updates the total price of the order.
     *
     * @param item the order item to be removed from the order
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        updateTotalPrice();
    }

    /**
     * Recalculates and updates the total price of the order based on the prices
     * of all the items currently in the order. The total price is computed by
     * summing up the prices of all `OrderItem` objects in the `orderItems` list.
     * If the list is empty, the total price is set to zero.
     */
    private void updateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .map(OrderItem::getItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retrieves the current status of the order.
     *
     * @return the status of the order as a String. The status indicates the current
     *         state of the order (e.g., "PENDING", "REJECTED", "PROCESSED_FULL").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the order.
     *
     * @param status The new status to be assigned to the order.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}