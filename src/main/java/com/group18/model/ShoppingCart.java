package com.group18.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The ShoppingCart class manages a collection of OrderItem objects and provides
 * methods for adding, removing, and retrieving items. It also handles the calculation
 * of tax, subtotal, and total prices. This class follows the Singleton design pattern,
 * ensuring only one instance of the ShoppingCart exists throughout the application lifecycle.
 */
public class ShoppingCart {
    /**
     * Singleton instance of the ShoppingCart class.
     * This ensures that only one instance of the ShoppingCart is created
     * and shared across the application.
     */
    private static ShoppingCart instance;
    /**
     * A collection of OrderItem objects representing the items currently in the shopping cart.
     * It maintains all items added to the shopping cart and is used to calculate totals, taxes,
     * and other properties of the cart.
     */
    private List<OrderItem> items = new ArrayList<>();
    /**
     * Represents the identifier of the cashier performing actions
     * related to the shopping cart or associated orders.
     * This field is used to track which cashier is responsible for
     * managing the shopping cart operations.
     */
    private int cashierId;
    /**
     * Represents the total tax amount for the items in the shopping cart.
     *
     * This value is computed based on the individual tax rates for tickets
     * and products included in the cart. The calculation is performed by
     * applying the corresponding tax rate to the item price multiplied by
     * its quantity, then aggregating the tax values for all items.
     *
     * The tax is updated whenever items are added, removed, or modified
     * in the cart by invoking the appropriate calculation methods.
     *
     * Default value is BigDecimal. ZERO, indicating no tax when the cart
     * is empty or contains no taxable items.
     */
    private BigDecimal tax = BigDecimal.ZERO;
    /**
     * Private constructor for the ShoppingCart class.
     * This constructor ensures that the ShoppingCart cannot be instantiated
     * directly from outside the class and enforces the use of a singleton instance.
     */
    private ShoppingCart() {}

    /**
     * Provides a singleton instance of the ShoppingCart class.
     * This ensures that only one instance of the ShoppingCart exists throughout the application,
     * promoting centralized management of cart-related operations.
     *
     * @return the singleton instance of the ShoppingCart class
     */
    public static ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }

    /**
     * Checks whether the shopping cart is empty.
     *
     * @return true if the cart contains no items, false otherwise.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Sets the cashier ID for this shopping cart.
     *
     * @param cashierId the identifier of the cashier to be associated with the cart
     */
    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    /**
     * Adds an order item to the shopping cart and recalculates the tax.
     *
     * @param item the order item to be added to the shopping cart
     */
    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTax();
    }

    /**
     * Removes a specified item from the shopping cart. The removal criteria differ depending on the type of the item:
     * for "product" type, items with matching product IDs are removed, and for "ticket" type, items with matching seat numbers are removed.
     * After removing the item(s), the tax for the cart is recalculated.
     *
     * @param itemToRemove The item to be removed from the cart. The item's type, product ID, or seat number
     *                     will determine the removal logic.
     */
    public void removeItem(OrderItem itemToRemove) {
        // For products, remove by product ID
        if ("product".equals(itemToRemove.getItemType())) {
            items.removeIf(item ->
                    "product".equals(item.getItemType()) &&
                            item.getProductId() != null &&
                            item.getProductId().equals(itemToRemove.getProductId())
            );
        }
        // For tickets, remove by seat number
        else if ("ticket".equals(itemToRemove.getItemType())) {
            items.removeIf(item ->
                    "ticket".equals(item.getItemType()) &&
                            item.getSeatNumber() != null &&
                            item.getSeatNumber().equals(itemToRemove.getSeatNumber())
            );
        }
        recalculateTax();
    }

    /**
     * Retrieves a list of order items currently in the shopping cart.
     *
     * @return a new list containing all {@code OrderItem} objects in the cart.
     */
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Clears all items and resets the tax in the shopping cart.
     * This method removes all items from the cart and sets the tax amount to zero.
     */
    public void clear() {
        items.clear();
        tax = BigDecimal.ZERO;
    }

    /**
     * Recalculates the total tax for all items in the shopping cart.
     *
     * The method retrieves the current tax value by invoking the calculateTax() method,
     * which computes the tax based on item prices, quantities, and applicable tax rates
     * for different item types (e.g., tickets and products).
     *
     * This method ensures that the tax value is updated whenever items are
     * added to or removed from the cart, maintaining consistency in the cart's total calculations.
     */
    private void recalculateTax() {
        tax = calculateTax();
    }

    /**
     * Calculates the total tax for the items in the shopping cart.
     * The tax is calculated based on the type of each item:
     * - A 20% tax rate is applied to items of type "ticket".
     * - A 10% tax rate is applied to items of other types (e.g., "product").
     *
     * The calculation considers the price and quantity of each item.
     *
     * @return the total tax for all items as a BigDecimal
     */
    private BigDecimal calculateTax() {
        BigDecimal ticketTaxRate = BigDecimal.valueOf(0.20); // 20% for tickets
        BigDecimal productTaxRate = BigDecimal.valueOf(0.10); // 10% for products

        BigDecimal totalTax = items.stream()
                .map(item -> {
                    BigDecimal itemPrice = item.getItemPrice();
                    System.out.println(itemPrice + "item price");
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

    /**
     * Retrieves the total tax amount calculated based on the items in the shopping cart.
     *
     * @return the total tax amount as a BigDecimal.
     */
    public BigDecimal getTax() {
        return tax;
    }

    /**
     * Calculates the subtotal of all items in the shopping cart.
     * The subtotal is determined by summing the product of the price
     * and quantity of each item in the cart.
     *
     * @return the calculated subtotal as a BigDecimal, representing the total cost of all items before tax.
     */
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates and returns the total amount of the shopping cart by adding the subtotal
     * and the calculated tax. The subtotal is derived from the sum of all item prices
     * multiplied by their respective quantities. Tax is recalculated dynamically
     * during the execution of this method.
     *
     * @return the total amount of the shopping cart, including subtotal and tax, as a BigDecimal.
     */
    public BigDecimal getTotal() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal calculatedTax = calculateTax(); // Always calculate fresh tax

        System.out.println("Subtotal: " + subtotal);
        System.out.println("Tax: " + calculatedTax);

        return subtotal.add(calculatedTax);
    }

    /**
     * Creates a new Order based on the current items in the shopping cart.
     * Filters out items with null values or quantities less than or equal to zero.
     * The order includes the cashier ID, valid items, total price, and the current date and time.
     *
     * @return a newly created Order containing valid items, cashier ID, total price, and order date
     */
    public Order createOrder() {
        Order order = new Order();
        order.setCashierId(cashierId);
        // Only include non-null items with quantity > 0
        List<OrderItem> validItems = items.stream()
                .filter(item -> item != null && item.getQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());
        order.setOrderItems(new ArrayList<>(validItems));
        order.setTotalPrice(getTotal());
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
}