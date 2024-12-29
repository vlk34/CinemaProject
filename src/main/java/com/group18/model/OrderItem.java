package com.group18.model;

import java.math.BigDecimal;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private String itemType; // "ticket" or "product"
    private Integer scheduleId;
    private Integer seatNumber;
    private Boolean discountApplied;
    private String occupantFirstName;
    private String occupantLastName;
    private Integer productId;
    private int quantity;
    private BigDecimal itemPrice;

    public OrderItem() {
        this.quantity = 1;
        this.discountApplied = false;
    }

    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Boolean getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(Boolean discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getOccupantFirstName() {
        return occupantFirstName;
    }

    public void setOccupantFirstName(String occupantFirstName) {
        this.occupantFirstName = occupantFirstName;
    }

    public String getOccupantLastName() {
        return occupantLastName;
    }

    public void setOccupantLastName(String occupantLastName) {
        this.occupantLastName = occupantLastName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }
}