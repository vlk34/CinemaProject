package com.group18.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private static ShoppingCart instance;
    private List<OrderItem> items = new ArrayList<>();
    private int cashierId;
    private BigDecimal tax = BigDecimal.ZERO;
    private ShoppingCart() {}

    public static ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTax();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        recalculateTax();
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
        tax = BigDecimal.ZERO;
    }

    private void recalculateTax() {
        tax = calculateTax();
    }

    private BigDecimal calculateTax() {
        BigDecimal ticketTaxRate = BigDecimal.valueOf(0.20); // 20% for tickets
        BigDecimal productTaxRate = BigDecimal.valueOf(0.10); // 10% for products

        BigDecimal totalTax = items.stream()
                .map(item -> {
                    BigDecimal itemPrice = item.getItemPrice();
                    int quantity = item.getQuantity();
                    BigDecimal taxRate = "ticket".equals(item.getItemType())
                            ? ticketTaxRate
                            : productTaxRate;

                    return itemPrice
                            .multiply(BigDecimal.valueOf(quantity))
                            .multiply(taxRate);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalTax;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal calculatedTax = calculateTax(); // Always calculate fresh tax

        System.out.println("Subtotal: " + subtotal);
        System.out.println("Tax: " + calculatedTax);

        return subtotal.add(calculatedTax);
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