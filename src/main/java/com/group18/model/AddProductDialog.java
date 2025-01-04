package com.group18.model;

import com.group18.dao.ProductDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private TextField imagePathField;
    private ComboBox<String> categoryComboBox;
    private String currentImagePath;

    public AddProductDialog(ProductDAO productDAO) {
        this.productDAO = productDAO;
        setupDialog();
    }

    private void setupDialog() {
        setTitle("Add New Product");
        setHeaderText("Enter product details");

        // Create form fields
        nameField = new TextField();
        priceField = new TextField();
        stockField = new TextField();
        imagePathField = new TextField();
        imagePathField.setEditable(false);
        categoryComboBox = new ComboBox<>();

        // Create select image button
        Button selectImageButton = new Button("Select Image");
        selectImageButton.setOnAction(e -> handleSelectImage());

        // Create image path layout
        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(imagePathField, selectImageButton);

        categoryComboBox.getItems().addAll("Beverage", "Biscuit", "Toy");

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryComboBox, 1, 3);
        grid.add(new Label("Image:"), 0, 4);
        grid.add(imageBox, 1, 4);

        // Set content
        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType addButtonType = ButtonType.OK;
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Add validation
        setupValidation();

        // Validate and process input
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return createProduct();
            }
            return null;
        });
    }

    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create products directory if it doesn't exist
                Path productsDir = Paths.get("src/main/resources/images/products");
                Files.createDirectories(productsDir);

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = productsDir.resolve(fileName);

                // Copy file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Set the relative path for database storage
                currentImagePath = "/images/products/" + fileName;
                imagePathField.setText(fileName);

            } catch (Exception e) {
                showError("Error", "Failed to copy image file: " + e.getMessage());
            }
        }
    }

    private void setupValidation() {
        // Price validation - allow decimal numbers only
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only allow numbers and one decimal point
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });

        // Stock validation (only positive numbers)
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Enable/disable OK button based on valid input
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Enable OK button only when all fields are valid
        nameField.textProperty().addListener((obs, old, newVal) -> validateOkButton(okButton));
        priceField.textProperty().addListener((obs, old, newVal) -> validateOkButton(okButton));
        stockField.textProperty().addListener((obs, old, newVal) -> validateOkButton(okButton));
        categoryComboBox.valueProperty().addListener((obs, old, newVal) -> validateOkButton(okButton));
    }

    private void validateOkButton(Button okButton) {
        boolean isValid = !nameField.getText().trim().isEmpty() &&
                !priceField.getText().isEmpty() &&
                !stockField.getText().isEmpty() &&
                categoryComboBox.getValue() != null;

        okButton.setDisable(!isValid);
    }

    private Product createProduct() {
        try {
            Product newProduct = new Product(
                    nameField.getText().trim(),
                    categoryComboBox.getValue().toLowerCase(),
                    new BigDecimal(priceField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim()),
                    currentImagePath
            );

            // Save to database and get the saved product with ID
            return productDAO.addProduct(newProduct);
        } catch (Exception e) {
            showError("Error", "Failed to create product: " + e.getMessage());
            return null;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}