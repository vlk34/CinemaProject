package com.group18.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private static ShoppingCart instance;
    private List<OrderItem> items = new ArrayList<>();
    private int cashierId;

    private ShoppingCart() {}

    public static ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Order createOrder() {
        Order order = new Order();
        order.setCashierId(cashierId);
        order.setOrderItems(new ArrayList<>(items));
        order.setTotalPrice(getTotal());
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
}