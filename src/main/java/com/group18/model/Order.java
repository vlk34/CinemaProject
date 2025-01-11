package com.group18.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class Order {
    private int orderId;
    private int cashierId;
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    private List<OrderItem> orderItems;
    private String status = "PENDING";
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.totalPrice = BigDecimal.ZERO;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCashierId() {
        return cashierId;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        updateTotalPrice();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .map(OrderItem::getItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}