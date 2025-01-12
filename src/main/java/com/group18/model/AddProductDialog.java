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
 * AddProductDialog is a dialog window used for adding new products. It provides
 * a user interface for entering product details such as name, price, stock,
 * category, and an optional product image.
 *
 * This class extends the Dialog class and returns a Product object upon
 * successful completion. It manages input validations and displays validation
 * errors to ensure proper input from the user.
 */
public class AddProductDialog extends Dialog<Product> {
    /**
     * Represents an instance of ProductDAO used for managing product-related operations.
     * This variable is utilized to perform CRUD operations on products, manage stock levels,
     * and interact with the database for retrieving and modifying product data.
     */
    private ProductDAO productDAO;
    /**
     * Represents a text input field for entering the name of a product
     * in the dialog for adding or editing product details.
     * This field is used to capture and display the name of the product.
     */
    private TextField nameField;
    /**
     * Represents the input field for entering the price of a product.
     * This field is used within the dialog to capture or modify the price information.
     * It is associated with the Product's price attribute, which is managed as a BigDecimal.
     */
    private TextField priceField;
    /**
     * This TextField is used for inputting the stock quantity of a product.
     * It allows the user to specify or adjust the current inventory level
     * of the product within the AddProductDialog interface.
     *
     * The stockField is an integral part of the product addition form and
     * requires appropriate data validation to ensure only valid stock
     * quantities are entered by the user.
     */
    private TextField stockField;
    /**
     * A graphical element used to preview the image associated with the product.
     * This field is tied to the product's image data and displays the current image in the dialog.
     * It is used within the AddProductDialog class to provide visual feedback to the user when selecting or editing an image.
     */
    private ImageView imagePreview;
    /**
     * A ComboBox component used for selecting a product category in the AddProductDialog.
     * This ComboBox is populated with predefined product categories (e.g., 'beverage', 'biscuit', 'toy')
     * and serves as a mechanism to categorize products being entered or edited in the application.
     */
    private ComboBox<String> categoryComboBox;
    /**
     * Stores the binary image data for the product being added or edited within the dialog.
     * This data is typically used for displaying a preview of the image or associating it with a product instance.
     * If no image is selected or set, this byte array might remain null or empty.
     */
    private byte[] currentImageData;

    /**
     * Constructs a new AddProductDialog instance.
     * This dialog allows users to input details for adding a new product
     * and integrates with the provided ProductDAO for performing operations.
     *
     * @param productDAO the ProductDAO object used to manage product-related operations
     */
    public AddProductDialog(ProductDAO productDAO) {
        this.productDAO = productDAO;
        setupDialog();
    }

    /**
     * Configures and initializes the dialog for adding a new product.
     * This method sets the dialog's title and header, creates necessary
     * input fields for product details (such as name, price, stock, category,
     * and image), and arranges them in a layout.
     *
     * The dialog includes:
     * - Text fields for entering product name, price, and stock.
     * - A combo box for selecting the product category.
     * - An image preview section with a button to select an image.
     * - Validation to ensure all required inputs are valid before enabling
     *   the confirmation button.
     * - A mechanism to set a default image if no image is selected.
     *
     * The layout is managed using a GridPane, and the dialog content is constructed
     * by assembling all the fields and components together. This method also specifies
     * the behavior for the result converter to create a product when the OK button
     * is pressed, and adds the default image when initializing the dialog.
     */
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

    /**
     * Opens a file chooser dialog for the user to select an image file, updates the image preview,
     * and reads the image data into a byte array for further use. If the user does not select a file
     * or an error occurs during file reading, appropriate actions such as default image restoration
     * or error display are performed.
     *
     * This method supports common image file formats including PNG, JPG, JPEG, and GIF.
     * If a valid file is selected, it updates the `imagePreview` control with the selected image
     * and stores the image data in the `currentImageData` field. In the event of an error during
     * the file-reading process, the method sets a default image and displays an error message.
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
                Image image = new Image(new ByteArrayInputStream(currentImageData));
                imagePreview.setImage(image);
            } catch (IOException e) {
                showError("Error", "Failed to read image file: " + e.getMessage());
                setDefaultImage();
            }
        }
    }

    /**
     * Sets the default image for the image preview in the dialog.
     *
     * This method loads a default "no-image" placeholder from the resources folder
     * and assigns it to the image preview. The placeholder is loaded from the path
     * "/images/no-image.png". If the image is successfully loaded, it is displayed
     * in the image preview component. If the loading fails due to an I/O exception
     * or the resource is not found, the image preview will not display anything.
     *
     * Any encountered I/O errors during the loading process are logged to the
     * standard error output.
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
     * Configures validation rules for the dialog's input fields and updates
     * the state of the OK button based on the validity of the inputs.
     *
     * The method applies the following validations:
     * - Ensures the price field allows only decimal values.
     * - Ensures the stock field contains only positive integers.
     * - Monitors all input fields and enables the OK button only when all
     *   fields have valid values and necessary inputs are provided.
     */
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

    /**
     * Validates the state of the "OK" button based on input fields' validity.
     * The button is enabled only if all required fields are valid:
     * - The name field is not empty.
     * - The price field is not empty.
     * - The stock field is not empty.
     * - A category is selected in the category combo box.
     *
     * @param okButton the "OK" button to enable or disable based on the field validations
     */
    private void validateOkButton(Button okButton) {
        boolean isValid = !nameField.getText().trim().isEmpty() &&
                !priceField.getText().isEmpty() &&
                !stockField.getText().isEmpty() &&
                categoryComboBox.getValue() != null;

        okButton.setDisable(!isValid);
    }

    /**
     * Creates a new product based on user input, attempts to save it to the database,
     * and returns the created product if successful.
     * Handles input validation and error reporting.
     *
     * @return the created Product object if successful, or null if an error occurs during creation or saving.
     */
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

    /**
     * Displays an error message in a dialog box.
     *
     * @param title   the title of the error dialog
     * @param content the error message content to be displayed
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}