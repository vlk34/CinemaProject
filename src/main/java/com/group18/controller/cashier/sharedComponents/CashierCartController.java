package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.CashierController;
import com.group18.dao.PriceDAO;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.text.ParseException;
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
    @FXML private VBox cashierCart;

    private CashierController mainController;
    private PriceDAO priceDAO;
    private double subtotal = 0.0;
    private double discounts = 0.0;
    private double tax = 0.0;
    private Map<String, HBox> cartItems = new HashMap<>();

    private Movie currentMovie;
    private MovieSession currentSession;
    private LocalDate currentDate;

    private static final double TICKET_TAX_RATE = 0.20; // 20% tax for tickets
    private static final double PRODUCT_TAX_RATE = 0.10; // 10% tax for products
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @FXML
    private void initialize() {
        priceDAO = new PriceDAO();
        updateSummary();
    }

    // UI Animation methods remain the same...
    @FXML
    private void toggleCartDetails() {
        boolean isCurrentlyVisible = cartDetailsContainer.isVisible();

        if (isCurrentlyVisible) {
            cartDetailsContainer.setVisible(false);
            cartDetailsContainer.setManaged(false);
            ImageView icon = (ImageView) toggleDetailsButton.getGraphic();
            icon.setImage(new Image(getClass().getResourceAsStream("/images/maximize.png")));
        } else {
            cartDetailsContainer.setOpacity(0);
            cartDetailsContainer.setTranslateY(10);
            cartDetailsContainer.setVisible(true);
            cartDetailsContainer.setManaged(true);

            ImageView icon = (ImageView) toggleDetailsButton.getGraphic();
            icon.setImage(new Image(getClass().getResourceAsStream("/images/minimize.png")));

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), cartDetailsContainer);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), cartDetailsContainer);
            translateTransition.setFromY(10);
            translateTransition.setToY(0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, translateTransition);
            parallelTransition.play();
        }
    }

    public void setMainController(CashierController controller) {
        this.mainController = controller;
    }

    // Getters remain the same...
    public VBox getCartItemsContainer() {
        return cartItemsContainer;
    }

    public Map<String, HBox> getCartItems() {
        return cartItems;
    }

    public void setSessionContext(Movie movie, MovieSession session, LocalDate date) {
        this.currentMovie = movie;
        this.currentSession = session;
        this.currentDate = date;
    }

    public void addSeatsToCart(Set<String> selectedSeats) {
        clearCart();
        double ticketPrice = priceDAO.getTicketPrice(currentSession.getHall());

        for (String seatId : selectedSeats) {
            String itemName = String.format("Seat %s - %s", seatId, currentMovie.getTitle());
            addCartItem(itemName, ticketPrice, 1, "ticket");
        }
    }

    public void addCartItem(String name, double price, int quantity, String type, double discountAmount) {
        removeCartItem(name);

        HBox itemContainer = createCartItemUI(name, price, quantity, discountAmount);
        cartItemsContainer.getChildren().add(itemContainer);
        cartItems.put(name, itemContainer);

        recalculateSubtotalAndTax();
        updateSummary();
    }

    public void addCartItem(String name, double price, int quantity, String type) {
        addCartItem(name, price, quantity, type, 0.0);
    }

    public void updateDiscount(double totalDiscount) {
        this.discounts = totalDiscount;
        recalculateSubtotalAndTax(); // New method to recalculate everything
        updateSummary();
    }

    private void recalculateSubtotalAndTax() {
        subtotal = 0.0;
        tax = 0.0;
        discounts = 0.0;

        for (Map.Entry<String, HBox> entry : cartItems.entrySet()) {
            try {
                String name = entry.getKey();
                HBox itemContainer = entry.getValue();
                VBox details = (VBox) itemContainer.getChildren().get(0);
                Label priceLabel = (Label) details.getChildren().get(1);
                Label quantityLabel = (Label) itemContainer.getChildren().get(1);

                // Parse base price and quantity
                Number parsedPrice = CURRENCY_FORMATTER.parse(priceLabel.getText());
                double price = parsedPrice.doubleValue();
                int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

                double itemSubtotal = price * quantity;
                double itemDiscount = 0.0;

                // Handle discount if present
                if (details.getChildren().size() > 2) {
                    Label discountLabel = (Label) details.getChildren().get(2);
                    Number parsedDiscount = CURRENCY_FORMATTER.parse(discountLabel.getText().replace("-", ""));
                    itemDiscount = parsedDiscount.doubleValue() * quantity;
                    discounts += itemDiscount;
                }

                subtotal += (itemSubtotal - itemDiscount);

                // Apply appropriate tax rate on discounted amount
                double taxableAmount = itemSubtotal - itemDiscount;
                tax += taxableAmount * (name.contains("Seat") ? TICKET_TAX_RATE : PRODUCT_TAX_RATE);

            } catch (ParseException e) {
                System.err.println("Error in recalculateSubtotalAndTax: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void removeCartItem(String name) {
        HBox itemContainer = cartItems.get(name);
        if (itemContainer != null) {
            cartItemsContainer.getChildren().remove(itemContainer);
            cartItems.remove(name);

            recalculateSubtotalAndTax();
            updateSummary();

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

        VBox details = new VBox();
        details.setSpacing(5);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("item-name");

        Label priceLabel = new Label(formatCurrency(price));
        priceLabel.getStyleClass().add("item-price");

        details.getChildren().addAll(nameLabel, priceLabel);

        if (discountAmount > 0) {
            Label discountLabel = new Label("-" + formatCurrency(discountAmount));
            discountLabel.getStyleClass().addAll("item-discount", "discount-text");
            details.getChildren().add(discountLabel);
        }

        Label quantityLabel = new Label("x" + quantity);
        quantityLabel.getStyleClass().add("item-quantity");

        double itemTotal = (price - discountAmount) * quantity;
        Label totalLabel = new Label(formatCurrency(itemTotal));
        totalLabel.getStyleClass().add("item-total");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(details, quantityLabel, totalLabel);
        return container;
    }

    private void recalculateTax() {
        tax = 0.0;
        for (Map.Entry<String, HBox> entry : cartItems.entrySet()) {
            try {
                String name = entry.getKey();
                HBox itemContainer = entry.getValue();
                VBox details = (VBox) itemContainer.getChildren().get(0);
                Label priceLabel = (Label) details.getChildren().get(1);
                Number parsedPrice = CURRENCY_FORMATTER.parse(priceLabel.getText());
                double price = parsedPrice.doubleValue();

                Label quantityLabel = (Label) itemContainer.getChildren().get(1);
                int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

                double itemSubtotal = price * quantity;

                // Apply discount if present
                if (details.getChildren().size() > 2) {
                    Label discountLabel = (Label) details.getChildren().get(2);
                    Number parsedDiscount = CURRENCY_FORMATTER.parse(discountLabel.getText().replace("-", ""));
                    double discountAmount = parsedDiscount.doubleValue() * quantity;
                    itemSubtotal -= discountAmount;
                }

                // Apply appropriate tax rate
                tax += itemSubtotal * (name.contains("Seat") ? TICKET_TAX_RATE : PRODUCT_TAX_RATE);
            } catch (ParseException e) {
                System.err.println("Error calculating tax: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void updateSummary() {
        itemCountLabel.setText(cartItems.size() + " items");
        subtotalLabel.setText(formatCurrency(subtotal));
        discountsLabel.setText("-" + formatCurrency(discounts));
        taxLabel.setText(formatCurrency(tax));

        double total = subtotal + tax; // subtotal is already discounted
        totalLabel.setText(formatCurrency(total));

        if (mainController != null && mainController.getActionBarController() != null) {
            mainController.getActionBarController().updateButtonStates(
                    mainController.getCurrentStageIndex()
            );
        }
    }

    public void refreshCart() {
        Map<String, HBox> productItems = new HashMap<>();
        cartItems.forEach((name, item) -> {
            if (!name.contains("Seat")) {
                productItems.put(name, item);
            }
        });

        cartItemsContainer.getChildren().clear();
        cartItems.clear();
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;

        productItems.forEach((name, item) -> {
            cartItemsContainer.getChildren().add(item);
            cartItems.put(name, item);
        });

        recalculateSubtotalAndTax();
        updateSummary();
    }

    private String formatCurrency(double amount) {
        return CURRENCY_FORMATTER.format(amount);
    }

    public void clearCart() {
        cartItemsContainer.getChildren().clear();
        cartItems.clear();
        subtotal = 0.0;
        discounts = 0.0;
        tax = 0.0;
        updateSummary();

        if (mainController != null && mainController.getActionBarController() != null) {
            mainController.getActionBarController().updateButtonStates(
                    mainController.getCurrentStageIndex()
            );
        }
    }

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