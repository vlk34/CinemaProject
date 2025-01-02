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
    @FXML private Button checkoutButton;
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

    public void addCartItem(String name, double price, int quantity, String type) {
        // Remove existing item if it exists
        removeCartItem(name);

        // Create cart item UI
        HBox itemContainer = createCartItemUI(name, price, quantity);
        cartItemsContainer.getChildren().add(itemContainer);
        cartItems.put(name, itemContainer);  // Store reference to the item

        // Update totals
        subtotal += price * quantity;
        tax = calculateTax(subtotal - discounts);
        updateSummary();

        checkoutButton.setDisable(false);
    }

    public void removeCartItem(String name) {
        HBox itemContainer = cartItems.get(name);
        if (itemContainer != null) {
            // Get the price label and extract the amount
            Label priceLabel = (Label) ((VBox) itemContainer.getChildren().get(0)).getChildren().get(1);
            String priceText = priceLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
            double price = Double.parseDouble(priceText);

            // Update totals
            subtotal -= price;
            tax = calculateTax(subtotal - discounts);

            cartItemsContainer.getChildren().remove(itemContainer);
            cartItems.remove(name);
            updateSummary();
        }
    }

    public void addDiscount(String description, double amount) {
        discounts += amount;
        updateSummary();
    }

    private HBox createCartItemUI(String name, double price, int quantity) {
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

        // Quantity
        Label quantityLabel = new Label("x" + quantity);
        quantityLabel.getStyleClass().add("item-quantity");

        // Total
        Label totalLabel = new Label(formatCurrency(price * quantity));
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

        // Disable checkout if no items
        checkoutButton.setDisable(cartItems.isEmpty());
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
        cartItems.clear();
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;
        updateSummary();

        checkoutButton.setDisable(true);
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