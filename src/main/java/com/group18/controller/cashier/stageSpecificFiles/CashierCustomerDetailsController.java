package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
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

    private List<Product> beverages = new ArrayList<>();
    private List<Product> biscuits = new ArrayList<>();
    private List<Product> toys = new ArrayList<>();
    private CashierController cashierController;
    private Set<String> selectedSeats;

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    public void setSelectedSeats(Set<String> seats) {
        this.selectedSeats = seats;
        // Update UI with selected seats info if needed
    }

    @FXML
    private void initialize() {
        // Setup input validation
        ageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Load products from database
        loadProducts();
    }

    @FXML
    private void handleVerifyAge() {
        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 18 || age > 60) {
                applyAgeDiscount(firstNameField.getText(), lastNameField.getText(), age);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Age Verification");
                alert.setHeaderText(null);
                alert.setContentText("No age-based discount applicable.");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Age");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid age.");
            alert.showAndWait();
        }
    }

    private void loadProducts() {
        // TODO: Load from database
        // Display products
        displayProducts();
    }

    private void displayProducts() {
        beverages.forEach(product -> beveragesContainer.getChildren().add(createProductCard(product)));
        biscuits.forEach(product -> biscuitsContainer.getChildren().add(createProductCard(product)));
        toys.forEach(product -> toysContainer.getChildren().add(createProductCard(product)));
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");

        // Product image - always use default image
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/products/popcorn.jpg")));
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);

        // Product info with improved styling
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #666666;");

        Label stockLabel = new Label(String.format("In Stock: %d", product.getStock()));
        stockLabel.setStyle("-fx-text-fill: #666666;");

        Button addButton = new Button("Add to Cart");
        addButton.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white;");
        addButton.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private void addToCart(Product product) {
        // TODO: Implement add to cart functionality
    }

    private void applyAgeDiscount(String firstName, String lastName, int age) {
        // TODO: Apply age-based discount to tickets in cart
    }
}