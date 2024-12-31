package com.group18.model;

import com.group18.dao.ProductDAO;
import com.group18.model.Product;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;

public class AddProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private ComboBox<String> categoryComboBox;

    public AddProductDialog(ProductDAO productDAO) {
        this.productDAO = productDAO;
        setupDialog();
    }

    private void setupDialog() {
        setTitle("Add New Product");

        // Create form fields
        nameField = new TextField();
        priceField = new TextField();
        stockField = new TextField();
        categoryComboBox = new ComboBox<>();

        categoryComboBox.getItems().addAll("Beverage", "Biscuit", "Toy");

        // Create layout
        GridPane grid = new GridPane();
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
                return createProduct();
            }
            return null;
        });
    }

    private Product createProduct() {
        try {
            Product newProduct = new Product(
                    nameField.getText(),
                    categoryComboBox.getValue().toLowerCase(),
                    new BigDecimal(priceField.getText()),
                    Integer.parseInt(stockField.getText())
            );

            // Save to database and get the saved product with ID
            return productDAO.addProduct(newProduct);
        } catch (Exception e) {
            // Show error alert
            return null;
        }
    }
}