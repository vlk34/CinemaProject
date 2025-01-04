package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.CashierController;
import com.group18.dao.PriceDAO;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CashierCartController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountsLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private VBox cartDetailsContainer;
    @FXML private Button toggleDetailsButton;
    private CashierController mainController;
    private PriceDAO priceDAO;
    private double subtotal = 0.0;
    private double discounts = 0.0;
    private double tax = 0.0;
    private Map<String, HBox> cartItems = new HashMap<>();

    // Movie and session context
    private Movie currentMovie;
    private MovieSession currentSession;
    private LocalDate currentDate;

    @FXML
    private void initialize() {
        priceDAO = new PriceDAO();
        updateSummary();
    }

    @FXML
    private void toggleCartDetails() {
        boolean isVisible = !cartDetailsContainer.isVisible();
        cartDetailsContainer.setVisible(isVisible);
        cartDetailsContainer.setManaged(isVisible);

        // Update button icon based on visibility
        ImageView icon = (ImageView) toggleDetailsButton.getGraphic();
        if (isVisible) {
            // When details are shown, show minimize icon
            icon.setImage(new Image(getClass().getResourceAsStream("/images/minimize.png")));
        } else {
            // When details are hidden, show maximize icon
            icon.setImage(new Image(getClass().getResourceAsStream("/images/maximize.png")));
        }
    }

    public void setMainController(CashierController controller) {
        this.mainController = controller;
    }

    public VBox getCartItemsContainer() {
        return cartItemsContainer;
    }

    public Map<String, HBox> getCartItems() {
        return cartItems;
    }

    // New method to set movie and session context
    public void setSessionContext(Movie movie, MovieSession session, LocalDate date) {
        this.currentMovie = movie;
        this.currentSession = session;
        this.currentDate = date;
    }

    // Modified method to add seats to cart
    public void addSeatsToCart(Set<String> selectedSeats) {
        // Clear existing cart
        clearCart();

        // Get ticket price for the current hall
        double ticketPrice = priceDAO.getTicketPrice(currentSession.getHall());

        // Add each seat as a cart item
        for (String seatId : selectedSeats) {
            String itemName = String.format("Seat %s - %s", seatId, currentMovie.getTitle());
            addCartItem(itemName, ticketPrice, 1, "ticket");
        }
    }

    public void addCartItem(String name, double price, int quantity, String type, double discountAmount) {
        // Remove existing item if it exists
        removeCartItem(name);

        // Create cart item UI
        HBox itemContainer = createCartItemUI(name, price, quantity, discountAmount);
        cartItemsContainer.getChildren().add(itemContainer);
        cartItems.put(name, itemContainer);  // Store reference to the item

        // Update totals
        subtotal += price * quantity;
        if (discountAmount > 0) {
            discounts += discountAmount * quantity;
        }
        tax = calculateTax(subtotal - discounts);
        updateSummary();
    }

    // Overloaded method for backward compatibility (no discount)
    public void addCartItem(String name, double price, int quantity, String type) {
        addCartItem(name, price, quantity, type, 0.0);
    }

    public void updateDiscount(double totalDiscount) {
        this.discounts = totalDiscount;
        updateSummary();
    }

    public void removeCartItem(String name) {
        HBox itemContainer = cartItems.get(name);
        if (itemContainer != null) {
            // Get the price label and extract the amount
            Label priceLabel = (Label) ((VBox) itemContainer.getChildren().get(0)).getChildren().get(1);
            String priceText = priceLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
            double price = Double.parseDouble(priceText);

            // Get quantity label to calculate total price to remove
            Label quantityLabel = (Label) itemContainer.getChildren().get(1);
            int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

            // Remove item from UI and data structure first
            cartItemsContainer.getChildren().remove(itemContainer);
            cartItems.remove(name);

            // Now update all the totals
            subtotal -= (price * quantity);

            // Recalculate full tax based on remaining items
            tax = calculateTax(subtotal - discounts);

            updateSummary();

            // Update action bar
            if (mainController != null && mainController.getActionBarController() != null) {
                mainController.getActionBarController().updateButtonStates(
                        mainController.getCurrentStageIndex()
                );
            }
        }
    }

    private HBox createCartItemUI(String name, double price, int quantity, double discountAmount) {
        HBox container = new HBox();
        container.setSpacing(10);
        container.getStyleClass().add("cart-item");

        // Item details
        VBox details = new VBox();
        details.setSpacing(5);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("item-name");

        Label priceLabel = new Label(formatCurrency(price));
        priceLabel.getStyleClass().add("item-price");

        details.getChildren().addAll(nameLabel, priceLabel);

        // If there's a discount, add it
        if (discountAmount > 0) {
            Label discountLabel = new Label("-" + formatCurrency(discountAmount));
            discountLabel.getStyleClass().addAll("item-discount", "discount-text");
            details.getChildren().add(discountLabel);
        }

        // Quantity
        Label quantityLabel = new Label("x" + quantity);
        quantityLabel.getStyleClass().add("item-quantity");

        // Total
        double itemTotal = (price - discountAmount) * quantity;
        Label totalLabel = new Label(formatCurrency(itemTotal));
        totalLabel.getStyleClass().add("item-total");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(details, quantityLabel, totalLabel);
        return container;
    }

    // Modify the updateSummary method to be more compact
    private void updateSummary() {
        double total = subtotal - discounts + tax;

        // Update compact summary
        itemCountLabel.setText(cartItems.size() + " items");
        totalLabel.setText(formatCurrency(total));

        // Update detailed summary
        subtotalLabel.setText(formatCurrency(subtotal));
        discountsLabel.setText("-" + formatCurrency(discounts));
        taxLabel.setText(formatCurrency(tax));

        if (mainController != null && mainController.getActionBarController() != null) {
            mainController.getActionBarController().updateButtonStates(
                    mainController.getCurrentStageIndex()
            );
        }
    }

    public void refreshCart() {
        // Preserve existing product items
        Map<String, HBox> productItems = new HashMap<>();
        cartItems.forEach((name, item) -> {
            // Check if item is a product (not a ticket)
            if (!name.contains("Seat")) {
                productItems.put(name, item);
            }
        });

        // Clear cart
        cartItemsContainer.getChildren().clear();
        cartItems.clear();

        // Reset totals
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;

        // Re-add product items
        productItems.forEach((name, item) -> {
            cartItemsContainer.getChildren().add(item);
            cartItems.put(name, item);

            // Recalculate totals for products
            VBox details = (VBox) item.getChildren().get(0);
            Label priceLabel = (Label) details.getChildren().get(1);
            String priceText = priceLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
            double price = Double.parseDouble(priceText);

            Label quantityLabel = (Label) item.getChildren().get(1);
            int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

            subtotal += price * quantity;
        });

        updateSummary();
    }

    private double calculateTax(double baseAmount) {
        return cartItems.entrySet().stream()
                .mapToDouble(entry -> {
                    String name = entry.getKey();
                    HBox itemContainer = entry.getValue();
                    VBox details = (VBox) itemContainer.getChildren().get(0);
                    Label priceLabel = (Label) details.getChildren().get(1);
                    String priceText = priceLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
                    double price = Double.parseDouble(priceText);

                    Label quantityLabel = (Label) itemContainer.getChildren().get(1);
                    int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

                    if (name.contains("Seat")) {
                        // 20% tax for tickets
                        double itemSubtotal = price * quantity;
                        // If there's a discount label, subtract it
                        if (details.getChildren().size() > 2) {
                            Label discountLabel = (Label) details.getChildren().get(2);
                            String discountText = discountLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
                            double discountAmount = Double.parseDouble(discountText);
                            itemSubtotal -= (discountAmount * quantity);
                        }
                        return itemSubtotal * 0.20; // 20% tax for tickets
                    } else {
                        return price * quantity * 0.10; // 10% tax for products
                    }
                })
                .sum();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
        return formatter.format(amount);
    }

    public void clearCart() {
        cartItemsContainer.getChildren().clear();
        cartItems.clear();
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;
        updateSummary();

        // Update action bar
        if (mainController != null && mainController.getActionBarController() != null) {
            mainController.getActionBarController().updateButtonStates(
                    mainController.getCurrentStageIndex()
            );
        }
    }

    // Getters for cart details
    public double getSubtotal() {
        return subtotal;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return subtotal - discounts + tax;
    }
}