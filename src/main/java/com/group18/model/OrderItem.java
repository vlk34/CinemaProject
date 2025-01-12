package com.group18.model;

import java.math.BigDecimal;

/**
 * The OrderItem class represents an individual item within an order.
 * This class can be used to represent both product items and ticket items
 * in an order system.
 *
 * Each OrderItem includes details about the order it belongs to,
 * the type of item, relevant pricing, and optional fields
 * for specific attributes like schedule ID, seat number, occupant names,
 * and product ID.
 *
 * It provides functionality to store and retrieve details about
 * the item, such as its quantity, price, and discount status.
 */
public class OrderItem {
    /**
     * Represents the unique identifier for a specific item within an order.
     * This field is used to distinguish individual items in an order.
     */
    private int orderItemId;
    /**
     * Represents the unique identifier for an order associated with the order item.
     */
    private int orderId;
    /**
     * Represents the type of item associated with an order.
     * The value can either be "ticket" or "product".
     */
    private String itemType; // "ticket" or "product"
    /**
     * Represents the identifier for the schedule associated with the order item.
     * It is used to link the order item to a specific scheduled event or activity.
     */
    private Integer scheduleId;
    /**
     * Represents the seat number associated with this order item.
     * This value indicates the specific seat assigned to the order,
     * and it may be null if no seat is applicable or assigned.
     */
    private Integer seatNumber;
    /**
     * Indicates whether a discount has been applied to the current order item.
     * This field is used to track the discounted status of the item and plays a role
     * in the calculation of the final price for the order.
     */
    private Boolean discountApplied;
    /**
     * Represents the first name of the occupant associated with the order item.
     * Typically used to identify or record the individual who owns or is assigned to this order item.
     */
    private String occupantFirstName;
    /**
     * Represents the last name of the occupant associated with the order item.
     * This field is used to store the surname of the individual occupying a seat
     * or associated with the order item, for identification or documentation purposes.
     */
    private String occupantLastName;
    /**
     * Represents the unique identifier of a product associated with this order item.
     * This field is used to link the order item to a specific product in the system.
     */
    private Integer productId;
    /**
     * Represents the amount or number of a specific item in the order.
     * This value indicates the quantity of the product associated with this {@code OrderItem}.
     */
    private int quantity;
    /**
     * Represents the price of an individual item in the order.
     * The price is expressed as a monetary value with arbitrary precision.
     */
    private BigDecimal itemPrice;

    /**
     * Default constructor for the OrderItem class.
     * Initializes the quantity to 1 and sets discountApplied to false by default.
     */
    public OrderItem() {
        this.quantity = 1;
        this.discountApplied = false;
    }

    /**
     * Retrieves the unique identifier for the order item.
     *
     * @return the order item ID as an integer
     */
    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId;
    }

    /**
     * Sets the identifier for the order item.
     *
     * @param orderItemId the identifier of the order item to set
     */
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    /**
     * Retrieves the order ID associated with this OrderItem.
     *
     * @return the order ID as an integer
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID for this order item.
     *
     * @param orderId The unique identifier of the order to which this order item belongs.
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Retrieves the type of the item.
     *
     * @return a string representing the type of the item, such as "product" or "ticket".
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * Sets the type of the item for the order.
     *
     * @param itemType The type of the item, such as "product" or "ticket".
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Retrieves the schedule ID associated with the order item.
     *
     * @return the schedule ID as an Integer, or null if no schedule ID is set for the order item.
     */
    public Integer getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the schedule ID associated with this order item.
     *
     * @param scheduleId The ID of the schedule to associate with this order item.
     */
    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * Retrieves the seat number associated with the order item.
     *
     * @return the seat number of the order item, or null if not set.
     */
    public Integer getSeatNumber() {
        return seatNumber;
    }

    /**
     * Sets the seat number associated with this order item.
     *
     * @param seatNumber The seat number to be assigned to this order item.
     */
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    /**
     * Retrieves the discount applied status for the order item.
     *
     * @return true if a discount has been applied to the order item; false otherwise.
     */
    public Boolean getDiscountApplied() {
        return discountApplied;
    }

    /**
     * Sets whether a discount has been applied to the order item.
     *
     * @param discountApplied A Boolean value indicating if a discount is applied
     *                        (true if a discount is applied, false otherwise).
     */
    public void setDiscountApplied(Boolean discountApplied) {
        this.discountApplied = discountApplied;
    }

    /**
     * Retrieves the first name of the occupant associated with the order item.
     *
     * @return The occupant's first name as a String.
     */
    public String getOccupantFirstName() {
        return occupantFirstName;
    }

    /**
     * Sets the first name of the occupant associated with the order item.
     *
     * @param occupantFirstName The first name of the occupant to set.
     */
    public void setOccupantFirstName(String occupantFirstName) {
        this.occupantFirstName = occupantFirstName;
    }

    /**
     * Retrieves the last name of the occupant associated with the order item.
     *
     * @return the last name of the occupant as a String
     */
    public String getOccupantLastName() {
        return occupantLastName;
    }

    /**
     * Sets the last name of the occupant for the order item.
     *
     * @param occupantLastName The last name of the occupant.
     */
    public void setOccupantLastName(String occupantLastName) {
        this.occupantLastName = occupantLastName;
    }

    /**
     * Retrieves the product ID associated with this order item.
     *
     * @return the product ID as an Integer, or null if no product ID is set.
     */
    public Integer getProductId() {
        return productId;
    }

    /**
     * Sets the product identifier for the order item.
     *
     * @param productId The unique identifier of the product to associate with the order item.
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    /**
     * Retrieves the quantity of the order item.
     *
     * @return the quantity of the item as an integer
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity for the order item.
     *
     * @param quantity The quantity to set for the order item.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Retrieves the price of the item.
     *
     * @return the price of the item as a {@code BigDecimal}
     */
    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    /**
     * Sets the price of the item for the order.
     *
     * @param itemPrice The price of the item as a BigDecimal.
     */
    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }
}