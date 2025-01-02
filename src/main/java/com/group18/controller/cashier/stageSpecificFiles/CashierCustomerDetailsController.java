package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.dao.PriceDAO;
import com.group18.dao.ProductDAO;
import com.group18.model.OrderItem;
import com.group18.model.Product;
import com.group18.model.ShoppingCart;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class CashierCustomerDetailsController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField ageField;
    @FXML private Button verifyAgeButton;

    @FXML private FlowPane beveragesContainer;
    @FXML private FlowPane biscuitsContainer;
    @FXML private FlowPane toysContainer;

    private CashierController cashierController;
    private Set<String> selectedSeats;
    private PriceDAO priceDAO;
    private ProductDAO productDAO;
    private ShoppingCart cart;
    private boolean isDiscountApplicable = false;

    @FXML
    private void initialize() {
        priceDAO = new PriceDAO();
        productDAO = new ProductDAO();
        cart = ShoppingCart.getInstance();

        // Set up age field validation for numbers only
        ageField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Load products for each category
        loadProducts();
    }

    private void loadProducts() {
        loadProductCategory("beverage", beveragesContainer);
        loadProductCategory("biscuit", biscuitsContainer);
        loadProductCategory("toy", toysContainer);
    }

    private void loadProductCategory(String category, FlowPane container) {
        List<Product> products = productDAO.getProductsByType(category);
        container.getChildren().clear();

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            container.getChildren().add(productCard);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        // Image container with placeholder
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageContainer.getChildren().add(imageView);

        // Product name with wrapping
        Label nameLabel = new Label(product.getProductName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        // Price with currency
        Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        // Stock status indicator
        Label stockLabel = new Label();
        stockLabel.getStyleClass().addAll("stock-status");
        if (product.getStock() == 0) {
            stockLabel.getStyleClass().add("out-of-stock");
            stockLabel.setText("Out of Stock");
        } else if (product.getStock() < 10) {
            stockLabel.getStyleClass().add("low-stock");
            stockLabel.setText("Low Stock");
        } else {
            stockLabel.getStyleClass().add("in-stock");
            stockLabel.setText("In Stock");
        }

        // Quantity controls with enhanced styling
        HBox quantityBox = new HBox(10);
        quantityBox.getStyleClass().add("quantity-control");
        quantityBox.setAlignment(Pos.CENTER);

        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("quantity-button");
        minusButton.setDisable(product.getStock() == 0);

        Label quantityLabel = new Label("0");
        quantityLabel.getStyleClass().add("quantity-label");

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("quantity-button");
        plusButton.setDisable(product.getStock() == 0);

        minusButton.setOnAction(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            if (quantity > 0) {
                quantityLabel.setText(String.valueOf(quantity - 1));
                updateCartProduct(product, quantity - 1);

                // Enable plus button when decreasing from max stock
                plusButton.setDisable(false);
            }
            // Disable minus button when reaching 0
            if (quantity - 1 == 0) {
                minusButton.setDisable(true);
            }
        });

        plusButton.setOnAction(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            if (quantity < product.getStock()) {
                quantityLabel.setText(String.valueOf(quantity + 1));
                updateCartProduct(product, quantity + 1);

                // Enable minus button when increasing from 0
                minusButton.setDisable(false);
            }
            // Disable plus button when reaching max stock
            if (quantity + 1 == product.getStock()) {
                plusButton.setDisable(true);
            }
        });

        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);

        // Add elements to card
        card.getChildren().addAll(
                imageContainer,
                nameLabel,
                priceLabel,
                stockLabel,
                quantityBox
        );

        // Add hover effect handler
        card.setOnMouseEntered(e -> {
            if (product.getStock() > 0) {
                card.getStyleClass().add("product-card-hover");
            }
        });
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("product-card-hover");
        });

        return card;
    }

    private void updateCartProduct(Product product, int quantity) {
        // Remove existing item if quantity is 0
        cart.getItems().removeIf(item ->
                item.getItemType().equals("product") &&
                        item.getProductId() != null &&
                        item.getProductId().equals(product.getProductId())
        );

        // Add new item if quantity > 0
        if (quantity > 0) {
            OrderItem item = new OrderItem();
            item.setItemType("product");
            item.setProductId(product.getProductId());
            item.setQuantity(quantity);
            item.setItemPrice(product.getPrice());
            cart.addItem(item);
        }
    }

    @FXML
    private void handleVerifyAge() {
        if (ageField.getText().isEmpty()) {
            showError("Invalid Input", "Please enter customer's age.");
            return;
        }

        int age = Integer.parseInt(ageField.getText());
        isDiscountApplicable = age < 18 || age > 60;

        // Show result
        Alert alert = new Alert(
                isDiscountApplicable ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING,
                isDiscountApplicable ? "Age discount will be applied!" : "No age discount applicable."
        );
        alert.setHeaderText(null);
        alert.showAndWait();

        // Update all ticket items in cart with customer details and discount
        updateTicketsInCart();
    }

    private void updateTicketsInCart() {
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()) {
            showError("Invalid Input", "Please fill in customer name details.");
            return;
        }

        double basePrice = priceDAO.getTicketPrice(cashierController.getSelectedSession().getHall());
        double discountRate = isDiscountApplicable ? priceDAO.getAgeDiscount() / 100.0 : 0.0;

        // Remove existing ticket items
        cart.getItems().removeIf(item -> item.getItemType().equals("ticket"));

        // Add updated ticket items
        int seatIndex = 0;
        for (String seatId : selectedSeats) {
            OrderItem ticketItem = new OrderItem();
            ticketItem.setItemType("ticket");
            ticketItem.setScheduleId(cashierController.getSelectedSession().getScheduleId());
            ticketItem.setSeatNumber(convertSeatIdToNumber(seatId));
            ticketItem.setQuantity(1);

            // Apply discount if applicable
            double finalPrice = isDiscountApplicable ? basePrice * (1 - discountRate) : basePrice;
            ticketItem.setItemPrice(BigDecimal.valueOf(finalPrice));
            ticketItem.setDiscountApplied(isDiscountApplicable);

            ticketItem.setOccupantFirstName(firstNameField.getText().trim());
            ticketItem.setOccupantLastName(lastNameField.getText().trim());

            cart.addItem(ticketItem);
        }
    }

    private int convertSeatIdToNumber(String seatId) {
        char row = seatId.charAt(0);
        int col = Integer.parseInt(seatId.substring(1));
        int cols = cashierController.getSelectedSession().getHall().equals("Hall_A") ? 4 : 8;
        return ((row - 'A') * cols) + col;
    }

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    public void setSelectedSeats(Set<String> seats) {
        this.selectedSeats = seats;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}