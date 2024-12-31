package com.group18.model;

import com.group18.dao.ProductDAO;
import com.group18.model.Product;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;

public class EditProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private Product originalProduct;

    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private ComboBox<String> categoryComboBox;

    public EditProductDialog(ProductDAO productDAO, Product productToEdit) {
        this.productDAO = productDAO;
        this.originalProduct = productToEdit;

        setupDialog();
        initializeFields();
    }

    private void setupDialog() {
        setTitle("Edit Product");
        setHeaderText("Modify Product Details");

        // Create form fields
        nameField = new TextField();
        priceField = new TextField();
        stockField = new TextField();
        categoryComboBox = new ComboBox<>();

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

        // Set content
        getDialogPane().setContent(grid);

        // Add buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validate and process input
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return updateProduct();
            }
            return null;
        });
    }

    private void initializeFields() {
        // Populate fields with existing product data
        nameField.setText(originalProduct.getProductName());
        priceField.setText(originalProduct.getPrice().toString());
        stockField.setText(String.valueOf(originalProduct.getStock()));

        // Set category, converting to title case
        String category = originalProduct.getProductType();
        categoryComboBox.setValue(
                category.substring(0, 1).toUpperCase() +
                        category.substring(1)
        );

        // Add validation
        setupValidation();
    }

    private void setupValidation() {
        // Price validation
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                new BigDecimal(newValue);
            } catch (NumberFormatException e) {
                priceField.setText(oldValue);
            }
        });

        // Stock validation (only numbers)
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Name validation (prevent empty)
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nameField.setText(oldValue);
            }
        });
    }

    private Product updateProduct() {
        try {
            // Create a copy of the original product with updated values
            Product updatedProduct = new Product(
                    originalProduct.getProductId(),
                    nameField.getText().trim(),
                    categoryComboBox.getValue().toLowerCase(),
                    new BigDecimal(priceField.getText()),
                    Integer.parseInt(stockField.getText())
            );

            // Attempt to update in database
            Product result = productDAO.updateProduct(updatedProduct);

            if (result == null) {
                // Show error if update failed
                showErrorDialog("Update Failed", "Could not update product in database.");
                return null;
            }

            return result;
        } catch (Exception e) {
            // Show error for any unexpected exceptions
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