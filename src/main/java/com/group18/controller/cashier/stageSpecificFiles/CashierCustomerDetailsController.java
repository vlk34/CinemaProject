package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
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

    private List<ProductItem> beverages = new ArrayList<>();
    private List<ProductItem> biscuits = new ArrayList<>();
    private List<ProductItem> toys = new ArrayList<>();
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
                // Apply age discount
                applyAgeDiscount(firstNameField.getText(), lastNameField.getText(), age);
            } else {
                // Show no discount message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Age Verification");
                alert.setHeaderText(null);
                alert.setContentText("No age-based discount applicable.");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            // Show error for invalid age
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Age");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid age.");
            alert.showAndWait();
        }
    }

    private void loadProducts() {
        // TODO: Load from database
        // For now, adding sample products
        addSampleProducts();

        // Display products
        displayProducts();
    }

    private void addSampleProducts() {
        // Add sample beverages
        beverages.add(new ProductItem("Cola", 15.0, "beverages/cola.png", 100));
        beverages.add(new ProductItem("Water", 5.0, "beverages/water.png", 150));

        // Add sample biscuits
        biscuits.add(new ProductItem("Chocolate Cookie", 10.0, "biscuits/cookie.png", 75));
        biscuits.add(new ProductItem("Crackers", 8.0, "biscuits/cracker.png", 90));

        // Add sample toys
        toys.add(new ProductItem("Movie Figure", 50.0, "toys/figure.png", 20));
        toys.add(new ProductItem("Plush Toy", 45.0, "toys/plush.png", 25));
    }

    private void displayProducts() {
        beverages.forEach(product -> beveragesContainer.getChildren().add(createProductCard(product)));
        biscuits.forEach(product -> biscuitsContainer.getChildren().add(createProductCard(product)));
        toys.forEach(product -> toysContainer.getChildren().add(createProductCard(product)));
    }

    private VBox createProductCard(ProductItem product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);

        // Product image
        ImageView imageView = new ImageView(new Image(product.getImagePath()));
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);

        // Product info
        Label nameLabel = new Label(product.getName());
        Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
        Label stockLabel = new Label(String.format("In Stock: %d", product.getStock()));

        // Add to cart button
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private void addToCart(ProductItem product) {
        // TODO: Implement add to cart functionality
    }

    private void applyAgeDiscount(String firstName, String lastName, int age) {
        // TODO: Apply age-based discount to tickets in cart
    }

    // Product data class
    private static class ProductItem {
        private String name;
        private double price;
        private String imagePath;
        private int stock;

        public ProductItem(String name, double price, String imagePath, int stock) {
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
            this.stock = stock;
        }

        // Getters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getImagePath() { return imagePath; }
        public int getStock() { return stock; }
    }
}