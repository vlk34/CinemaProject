// OrderItemTableModel.java
package com.group18.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a table model for an order item in a point-of-sale or inventory system.
 * This class is primarily used for displaying order details in a table view.
 *
 * Each instance of this class encapsulates the details of an item within an order,
 * including the item's name, quantity, unit price, and total price.
 */
public class OrderItemTable {
    /**
     * Represents the name of the item in the order.
     * This property is used to store and retrieve the name of the item in the context of
     * an order or inventory system. It is typically displayed within a table view as part
     * of order details.
     */
    private SimpleStringProperty itemName;
    /**
     * Represents the quantity of an order item.
     * This property is used to hold the number of units purchased or involved in a transaction.
     */
    private SimpleIntegerProperty quantity;
    /**
     * Represents the unit price of an individual item in an order.
     * This value is used to calculate the total cost of the item
     * based on the quantity purchased.
     */
    private SimpleDoubleProperty price;
    /**
     * Represents the total price for the order item, calculated as the product of
     * quantity and unit price. It is a property that supports JavaFX bindings for
     * dynamic updates in the user interface.
     */
    private SimpleDoubleProperty total;

    /**
     * Constructs an OrderItemTable instance with the specified item details.
     *
     * @param itemName the name of the item in the order
     * @param quantity the quantity of the item in the order
     * @param price the unit price of the item
     * @param total the total price of the item (calculated as quantity * price)
     */
    public OrderItemTable(String itemName, int quantity, double price, double total) {
        this.itemName = new SimpleStringProperty(itemName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.total = new SimpleDoubleProperty(total);
    }

    /**
     * Retrieves the name of the item associated with this order entry.
     *
     * @return the name of the item as a String
     */
    public String getItemName() {
        return itemName.get();
    }

    /**
     * Retrieves the quantity of the order item.
     *
     * @return the quantity of the order item as an integer
     */
    public int getQuantity() {
        return quantity.get();
    }

    /**
     * Retrieves the unit price of the order item.
     *
     * @return the unit price of the item as a double
     */
    public double getPrice() {
        return price.get();
    }

    /**
     * Retrieves the total price for the order item.
     *
     * @return the total price of the order item as a double
     */
    public double getTotal() {
        return total.get();
    }
}