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

public class EditProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private Product originalProduct;
    private String currentImagePath;

    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private TextField imagePathField;
    private ComboBox<String> categoryComboBox;

    public EditProductDialog(ProductDAO productDAO, Product productToEdit) {
        this.productDAO = productDAO;
        this.originalProduct = productToEdit;
        this.currentImagePath = productToEdit.getImagePath();

        setupDialog();
        initializeFields();

        // Add result converter
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return updateProduct();
            }
            return null;
        });
    }

    private void setupDialog() {
        setTitle("Edit Product");
        setHeaderText("Modify Product Details");

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

        // Populate category combo box
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
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
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
                // Generate relative path for database
                String fileName = selectedFile.getName();
                currentImagePath = "/images/products/" + fileName;

                // Create target path in resources
                Path targetPath = Paths.get("src/main/resources/images/products", fileName);
                Files.createDirectories(targetPath.getParent());

                // Copy file if it doesn't exist already
                if (!Files.exists(targetPath)) {
                    Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                // Update the display field
                imagePathField.setText(fileName);

            } catch (Exception e) {
                showErrorDialog("Error", "Failed to copy image file: " + e.getMessage());
            }
        }
    }

    private void initializeFields() {
        // Populate fields with existing product data
        nameField.setText(originalProduct.getProductName());
        priceField.setText(originalProduct.getPrice().toString());
        stockField.setText(String.valueOf(originalProduct.getStock()));

        // Handle image path display
        if (originalProduct.getImagePath() != null) {
            String fileName = originalProduct.getImagePath().substring(
                    originalProduct.getImagePath().lastIndexOf('/') + 1
            );
            imagePathField.setText(fileName);
            currentImagePath = originalProduct.getImagePath(); // Keep the full path
        } else {
            imagePathField.setText("No image selected");
            currentImagePath = null;
        }

        // Set category, converting to title case
        String category = originalProduct.getProductType();
        categoryComboBox.setValue(
                category.substring(0, 1).toUpperCase() +
                        category.substring(1)
        );

        setupValidation();
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

    private Product updateProduct() {
        try {
            Product updatedProduct = new Product(
                    originalProduct.getProductId(),
                    nameField.getText().trim(),
                    categoryComboBox.getValue().toLowerCase(),
                    new BigDecimal(priceField.getText()),
                    Integer.parseInt(stockField.getText()),
                    currentImagePath  // Add the image path
            );

            // Attempt to update in database
            Product result = productDAO.updateProduct(updatedProduct);

            if (result == null) {
                showErrorDialog("Update Failed", "Could not update product in database.");
                return null;
            }

            return result;
        } catch (Exception e) {
            showErrorDialog("Error", "An error occurred while updating the product.");
            return null;
        }
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}