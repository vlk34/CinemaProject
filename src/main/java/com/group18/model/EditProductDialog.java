package com.group18.model;

import com.group18.dao.ProductDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private Product originalProduct;
    private byte[] currentImageData;

    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private ImageView imagePreview;
    private ComboBox<String> categoryComboBox;

    public EditProductDialog(ProductDAO productDAO, Product productToEdit) {
        this.productDAO = productDAO;
        this.originalProduct = productToEdit;
        this.currentImageData = productToEdit.getImageData();

        setupDialog();
        initializeFields();
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? updateProduct() : null);
    }

    private void setupDialog() {
        setTitle("Edit Product");
        setHeaderText("Modify Product Details");

        // Create form fields
        nameField = new TextField();
        priceField = new TextField();
        stockField = new TextField();
        imagePreview = new ImageView();
        imagePreview.setFitHeight(150);
        imagePreview.setFitWidth(150);
        imagePreview.setPreserveRatio(true);
        categoryComboBox = new ComboBox<>();

        // Create select image button
        Button selectImageButton = new Button("Select Image");
        selectImageButton.setOnAction(e -> handleSelectImage());

        // Create image preview layout
        VBox imageBox = new VBox(10);
        imageBox.getChildren().addAll(imagePreview, selectImageButton);

        categoryComboBox.getItems().addAll("Beverage", "Biscuit", "Toy");

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

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

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setupValidation();
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
                // Read file into byte array
                currentImageData = Files.readAllBytes(selectedFile.toPath());

                // Update image preview
                updateImagePreview();
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to read image file: " + e.getMessage());
            }
        }
    }

    private void updateImagePreview() {
        if (currentImageData != null && currentImageData.length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(currentImageData));
                imagePreview.setImage(image);
            } catch (Exception e) {
                showErrorDialog("Error", "Failed to preview image: " + e.getMessage());
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            InputStream defaultImageStream = getClass().getResourceAsStream("/images/no-image.png");
            if (defaultImageStream != null) {
                currentImageData = defaultImageStream.readAllBytes();
                Image defaultImage = new Image(new ByteArrayInputStream(currentImageData));
                imagePreview.setImage(defaultImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            imagePreview.setImage(null);
        }
    }

    private void initializeFields() {
        nameField.setText(originalProduct.getProductName());
        priceField.setText(originalProduct.getPrice().toString());
        stockField.setText(String.valueOf(originalProduct.getStock()));

        String category = originalProduct.getProductType();
        categoryComboBox.setValue(
                category.substring(0, 1).toUpperCase() + category.substring(1)
        );

        updateImagePreview();
    }

    private void setupValidation() {
        // Price validation - allow decimal numbers only
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only allow numbers and one decimal point
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
            validateOkButton();
        });

        // Stock validation (only positive numbers)
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateOkButton();
        });

        // Enable/disable OK button based on valid input
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        // Enable OK button only when all fields are valid
        nameField.textProperty().addListener((obs, old, newVal) -> validateOkButton());
        categoryComboBox.valueProperty().addListener((obs, old, newVal) -> validateOkButton());

        // Initial validation
        validateOkButton();
    }

    private void validateOkButton() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

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
                    currentImageData
            );

            Product result = productDAO.updateProduct(updatedProduct);
            if (result == null) {
                showErrorDialog("Update Failed", "Could not update product in database.");
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