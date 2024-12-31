package com.group18.controller.cashier.sharedComponents;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.text.NumberFormat;
import java.util.Locale;

public class CashierCartController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountsLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private double subtotal = 0.0;
    private double discounts = 0.0;
    private double tax = 0.0;

    @FXML
    private void initialize() {
        updateSummary();
    }

    public void addCartItem(String name, double price, int quantity, String type) {
        // Create cart item UI
        HBox itemContainer = createCartItemUI(name, price, quantity);
        cartItemsContainer.getChildren().add(itemContainer);

        // Update totals
        subtotal += price * quantity;
        tax = calculateTax(subtotal - discounts);
        updateSummary();
    }

    public void addDiscount(String description, double amount) {
        discounts += amount;
        updateSummary();
    }

    private HBox createCartItemUI(String name, double price, int quantity) {
        HBox container = new HBox();
        container.setSpacing(10);

        // Item details
        VBox details = new VBox();
        Label nameLabel = new Label(name);
        Label priceLabel = new Label(formatCurrency(price));
        details.getChildren().addAll(nameLabel, priceLabel);

        // Quantity
        Label quantityLabel = new Label("x" + quantity);

        // Total
        Label totalLabel = new Label(formatCurrency(price * quantity));

        container.getChildren().addAll(details, quantityLabel, totalLabel);
        return container;
    }

    private void updateSummary() {
        double total = subtotal - discounts + tax;

        itemCountLabel.setText(cartItemsContainer.getChildren().size() + " items");
        subtotalLabel.setText(formatCurrency(subtotal));
        discountsLabel.setText("-" + formatCurrency(discounts));
        taxLabel.setText(formatCurrency(tax));
        totalLabel.setText(formatCurrency(total));
    }

    private double calculateTax(double amount) {
        // 20% tax rate for tickets, 10% for products
        return amount * 0.20; // We'll need to make this more sophisticated later
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
        return formatter.format(amount);
    }

    public void clearCart() {
        cartItemsContainer.getChildren().clear();
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;
        updateSummary();
    }
}