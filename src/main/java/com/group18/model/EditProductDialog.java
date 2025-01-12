package com.group18.model;

import com.group18.dao.ProductDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;

/**
 * A dialog for editing the details of a product. This dialog allows the user to modify
 * attributes such as name, price, stock quantity, category, and product image.
 * Provides validation for input fields and updates the product in the database upon confirmation.
 *
 * This class extends the {@code Dialog<Product>} and uses JavaFX components to construct the form UI.
 */
public class EditProductDialog extends Dialog<Product> {
    /**
     * The ProductDAO instance used for managing data persistence and retrieval
     * operations related to products. This variable handles communication with
     * the data source, enabling functions such as querying, adding, updating,
     * and deleting product records. It serves as a key dependency for ensuring
     * the dialog's behavior is consistent with the underlying product data.
     */
    private ProductDAO productDAO;
    /**
     * Stores the original state of the product being edited in the dialog.
     * This is used to retain the initial product details before any modifications are made.
     * Useful for comparison, rollback, or validation purposes during the editing process.
     */
    private Product originalProduct;
    /**
     * Represents the binary data of the currently selected image in the EditProductDialog.
     * This byte array is used for storing and processing the image associated with the product being edited.
     * It may be updated during the dialog's lifecycle when the image is selected, previewed, or replaced.
     */
    private byte[] currentImageData;

    /**
     * Represents a text input field for entering or editing the name of a product.
     * This field is a GUI component within the EditProductDialog class, designed
     * to capture and display the product's name during creation or modification.
     */
    private TextField nameField;
    /**
     * Represents the input field for entering or displaying the price of a product.
     * This field is used to capture or manipulate the numeric value representing the product's price.
     */
    private TextField priceField;
    /**
     * Represents a text field for entering or displaying the stock quantity of a product.
     * This field is used to allow the user to input and modify the stock value associated with a product.
     * It is typically validated to ensure the entered value adheres to expected formats and constraints.
     */
    private TextField stockField;
    /**
     * Represents a visual component for displaying an image preview within the dialog.
     * Used to provide a graphical view of the currently selected or updated product image.
     */
    private ImageView imagePreview;
    /**
     * A ComboBox component used for selecting a product category in the EditProductDialog.
     * This field is specifically designed to allow users to choose from a predefined list
     * of product categories such as "beverage", "biscuit", or "toy".
     *
     * The categoryComboBox is populated and initialized as part of the dialog's setup process,
     * ensuring that the available options correspond to permissible product types.
     */
    private ComboBox<String> categoryComboBox;

    /**
     * Constructs a new EditProductDialog for editing details of a specified product.
     *
     * @param productDAO the data access object used for managing product operations
     * @param productToEdit the product object to be edited, containing the initial details
     */
    public EditProductDialog(ProductDAO productDAO, Product productToEdit) {
        this.productDAO = productDAO;
        this.originalProduct = productToEdit;
        this.currentImageData = productToEdit.getImageData();

        setupDialog();
        initializeFields();
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? updateProduct() : null);
    }

    /**
     * Configures and initializes the dialog for editing product details.
     * Sets up the title, header text, form layout, input fields, category selection,
     * and image preview component. Includes options for selecting an image for the product.
     * Integrates validation logic to restrict invalid input in form fields.
     * Adds OK and Cancel buttons to the dialog.
     *
     * This method builds the user interface of the dialog dynamically,
     * arranging the components in a GridPane layout. It also initializes
     * the event handlers for specific actions such as image selection.
     */
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

    /**
     * Handles the process of selecting an image file for the product, allowing the user
     * to browse and choose an image from their file system.
     *
     * When a file is selected:
     * - Reads the contents of the file as a byte array and updates the current image data.
     * - Updates the image preview with the selected image.
     *
     * If an error occurs during file reading, an error dialog is displayed to the user.
     *
     * This method uses a {@link FileChooser} to open a dialog for selecting image files
     * and supports common image formats such as PNG, JPG, JPEG, and GIF.
     */
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

    /**
     * Updates the image preview displayed in the dialog.
     *
     * This method checks whether the field `currentImageData` contains valid image data.
     * If valid data is present, it attempts to load the data into an `Image` object
     * and assigns it to the `imagePreview`. If the image data is invalid or an error
     * occurs during processing, it displays an error dialog with a relevant message.
     *
     * If no image data is available, the method sets a default image in the preview.
     */
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

    /**
     * Sets a default image to the imagePreview component if no custom image data is available.
     *
     * This method attempts to load a predefined default image from the specified resource path
     * (e.g., "/images/no-image.png"). If the resource is successfully loaded, it is converted
     * into an {@code Image} and set to the image preview component, and the byte data of the image
     * is stored in the {@code currentImageData} field. If the resource cannot be loaded or an
     * error occurs, the {@code imagePreview} component is set to null, and an exception stack trace
     * is printed to the standard error stream.
     *
     * Exceptions:
     * If an {@link IOException} occurs during the image reading process, the method handles it
     * by printing the stack trace and setting the {@code imagePreview} to null.
     */
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

    /**
     * Initializes the input fields and UI components with the values
     * of the product being edited. This includes setting text fields
     * for the product's name, price, and stock, as well as pre-selecting
     * the appropriate category in the category combo box.
     *
     * The method formats and capitalizes the product type for display
     * in the combo box. Additionally, it triggers an update to the
     * image preview, displaying the current image associated with
     * the product.
     */
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

    /**
     * Configures real-time validation for input fields in the dialog.
     *
     * The method adds listeners to input fields to ensure valid data entry and
     * dynamically updates the state of the OK button based on the validity of
     * inputs. The following validation rules are enforced:
     *
     * 1. Price Field: Allows only numeric values and a single decimal point. Reverts
     *    to the previous valid value if an invalid input is detected.
     * 2. Stock Field: Allows only positive numeric values. Automatically removes
     *    any invalid characters from the input.
     * 3. Name Field and Category ComboBox: Tracks changes and triggers validation
     *    to ensure these fields are not left empty.
     *
     * Additionally, the OK button is enabled only when all fields satisfy their
     * respective validation requirements.
     *
     * The method performs an initial validation to set the proper state of the
     * OK button when the dialog is first displayed.
     */
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

    /**
     * Validates the state of the "OK" button in the dialog based on the input
     * provided in the form fields. The button will be enabled only if the
     * required fields meet specific criteria:
     *
     * 1. The text in the {@code nameField} is not empty or containing only whitespace.
     * 2. The {@code priceField} is not empty.
     * 3. The {@code stockField} is not empty.
     * 4. The {@code categoryComboBox} has a selected value.
     *
     * When any of the above conditions are not met, the "OK" button will be disabled.
     */
    private void validateOkButton() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        boolean isValid = !nameField.getText().trim().isEmpty() &&
                !priceField.getText().isEmpty() &&
                !stockField.getText().isEmpty() &&
                categoryComboBox.getValue() != null;

        okButton.setDisable(!isValid);
    }

    /**
     * Updates a product's details in the database using the current inputs provided by the user.
     * This method creates a new Product object based on the user inputs such as name, category, price, stock,
     * and image data, and attempts to update it using the productDAO. If an exception occurs or the update fails,
     * an error dialog will be displayed.
     *
     * @return the updated Product object if the update operation is successful, or null if the update fails or an error occurs.
     */
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

    /**
     * Displays an error dialog with the specified title and content message.
     *
     * @param title   the title of the error dialog
     * @param content the content message to be displayed in the error dialog
     */
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}