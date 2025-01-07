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

public class AddProductDialog extends Dialog<Product> {
    private ProductDAO productDAO;
    private TextField nameField;
    private TextField priceField;
    private TextField stockField;
    private ImageView imagePreview;
    private ComboBox<String> categoryComboBox;
    private byte[] currentImageData;

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
        ButtonType addButtonType = ButtonType.OK;
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        setupValidation();
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? createProduct() : null);

        // Set default image
        setDefaultImage();
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
                Image image = new Image(new ByteArrayInputStream(currentImageData));
                imagePreview.setImage(image);
            } catch (IOException e) {
                showError("Error", "Failed to read image file: " + e.getMessage());
                setDefaultImage();
            }
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
                    currentImageData
            );

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