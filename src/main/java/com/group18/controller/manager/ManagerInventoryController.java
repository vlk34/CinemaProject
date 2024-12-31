package com.group18.controller.manager;

import com.group18.dao.ProductDAO;
import com.group18.model.Product;
import com.group18.model.AddProductDialog;
import com.group18.model.EditProductDialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManagerInventoryController implements Initializable {
    // FXML Injected Components
    @FXML
    private Button addProductButton;

    @FXML
    private Label beveragesCountLabel;
    @FXML
    private Label beveragesLowStockLabel;
    @FXML
    private Label biscuitsCountLabel;
    @FXML
    private Label biscuitsLowStockLabel;
    @FXML
    private Label toysCountLabel;
    @FXML
    private Label toysLowStockLabel;

    @FXML
    private TableView<Product> inventoryTable;

    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Product, String> statusColumn;
    @FXML
    private TableColumn<Product, Void> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> stockStatusComboBox;

    // Data Management
    private ProductDAO productDAO;
    private ObservableList<Product> masterData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize dependencies
        productDAO = new ProductDAO();

        // Setup UI components
        setupTableColumns();
        setupCategoryComboBox();
        setupStockStatusComboBox();
        setupSearchFunctionality();
        setupAddProductButton();

        // Load initial data
        loadProductData();
    }

    /**
     * Configure table columns with appropriate cell value factories and custom cell factories
     */
    private void setupTableColumns() {
        // Basic column bindings
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn.setStyle("-fx-alignment: CENTER;");

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("productType"));
        categoryColumn.setStyle("-fx-alignment: CENTER;");

        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockColumn.setStyle("-fx-alignment: CENTER;");

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setStyle("-fx-alignment: CENTER;");


        // Custom status column with color coding
        // Custom status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    if (product != null) {  // Add null check
                        if (product.getStock() < 10) {
                            setText("Low Stock");
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setText("In Stock");
                            setStyle("-fx-text-fill: green;");
                        }
                    }
                }
            }
        });
        statusColumn.setStyle("-fx-alignment: CENTER;");

        // Actions column with edit and delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Base styles matching staff view (transparent with purple text)
                editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;");
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;");

                // Hover effects - only change background color
                editButton.setOnMouseEntered(e ->
                        editButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #2a1b35;")
                );
                editButton.setOnMouseExited(e ->
                        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;")
                );

                deleteButton.setOnMouseEntered(e ->
                        deleteButton.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #2a1b35;")
                );
                deleteButton.setOnMouseExited(e ->
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;")
                );

                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditProductDialog(product);
                });

                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });

                // Center the HBox containing the buttons
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, editButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Load all products from database and update UI
     */
    private void loadProductData() {
        masterData = FXCollections.observableArrayList(productDAO.getAllProducts());
        inventoryTable.setItems(masterData);
        updateCategoryCards();
    }

    /**
     * Update category summary cards with product counts and low stock information
     */
    private void updateCategoryCards() {
        var categoryCounts = masterData.stream()
                .collect(Collectors.groupingBy(
                        Product::getProductType,
                        Collectors.toList()
                ));

        Platform.runLater(() -> {
            updateCategoryCard(
                    categoryCounts, "beverage",
                    beveragesCountLabel, beveragesLowStockLabel
            );
            updateCategoryCard(
                    categoryCounts, "biscuit",
                    biscuitsCountLabel, biscuitsLowStockLabel
            );
            updateCategoryCard(
                    categoryCounts, "toy",
                    toysCountLabel, toysLowStockLabel
            );
        });
    }

    /**
     * Helper method to update individual category card
     */
    private void updateCategoryCard(
            java.util.Map<String, java.util.List<Product>> categoryCounts,
            String category,
            Label countLabel,
            Label lowStockLabel
    ) {
        var categoryProducts = categoryCounts.getOrDefault(category, java.util.Collections.emptyList());
        countLabel.setText(categoryProducts.size() + " items");
        lowStockLabel.setText(
                categoryProducts.stream().filter(p -> p.getStock() < 10).count() +
                        " items low on stock"
        );
    }

    /**
     * Setup category filter combo box
     */
    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll("No Filter", "Beverages", "Biscuits", "Toys");
        categoryComboBox.setValue("No Filter"); // Set default value
        categoryComboBox.setOnAction(event -> filterProducts());
    }

    /**
     * Setup stock status filter combo box
     */
    private void setupStockStatusComboBox() {
        stockStatusComboBox.getItems().addAll("No Filter", "In Stock", "Low Stock");
        stockStatusComboBox.setValue("No Filter"); // Set default value
        stockStatusComboBox.setOnAction(event -> filterProducts());
    }

    /**
     * Setup live search functionality
     */
    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());
    }

    /**
     * Filter products based on search, category, and stock status
     */
    /**
     * Filter products based on search, category, and stock status
     */
    private void filterProducts() {
        if (masterData == null) return;

        // Start with master data and apply filters sequentially
        ObservableList<Product> filteredList = masterData;

        // Filter by search text
        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            String searchText = searchField.getText().toLowerCase();
            filteredList = filteredList.filtered(product ->
                    product.getProductName().toLowerCase().contains(searchText)
            );
        }

        // Filter by category
        if (categoryComboBox.getValue() != null && !categoryComboBox.getValue().equals("No Filter")) {
            String categoryFilter = categoryComboBox.getValue()
                    .toLowerCase()
                    .substring(0, categoryComboBox.getValue().length() - 1);
            filteredList = filteredList.filtered(product ->
                    product.getProductType().equalsIgnoreCase(categoryFilter)
            );
        }

        // Filter by stock status
        if (stockStatusComboBox.getValue() != null && !stockStatusComboBox.getValue().equals("No Filter")) {
            boolean isLowStock = stockStatusComboBox.getValue().equals("Low Stock");
            filteredList = filteredList.filtered(product ->
                    isLowStock == (product.getStock() < 10)
            );
        }

        // Update table with filtered list or original master data if no filters are applied
        if (searchField.getText().isEmpty() &&
                categoryComboBox.getValue().equals("No Filter") &&
                stockStatusComboBox.getValue().equals("No Filter")) {
            inventoryTable.setItems(masterData);
        } else {
            inventoryTable.setItems(filteredList);
        }

        // Force refresh of table to update all cells
        inventoryTable.refresh();
    }

    /**
     * Setup add product button action
     */
    private void setupAddProductButton() {
        addProductButton.setOnAction(event -> showAddProductDialog());
    }

    /**
     * Show dialog to add a new product
     */
    private void showAddProductDialog() {
        AddProductDialog dialog = new AddProductDialog(productDAO);
        dialog.showAndWait().ifPresent(newProduct -> {
            // Add to master data
            masterData.add(newProduct);

            // Update category cards
            updateCategoryCards();

            // Reapply current filters to ensure the new product shows up if it matches
            filterProducts();

            // Make sure the new product is visible by scrolling to it
            Platform.runLater(() -> {
                inventoryTable.scrollTo(newProduct);
                inventoryTable.getSelectionModel().select(newProduct);
            });
        });
    }

    /**
     * Show dialog to edit an existing product
     */
    private void showEditProductDialog(Product product) {
        EditProductDialog dialog = new EditProductDialog(productDAO, product);
        dialog.showAndWait().ifPresent(updatedProduct -> {
            int index = masterData.indexOf(product);
            if (index != -1) {
                masterData.set(index, updatedProduct);
                updateCategoryCards();
                inventoryTable.refresh();
            }
        });
    }

    /**
     * Delete a product from inventory
     */
    private void deleteProduct(Product product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Product");
        confirmDialog.setHeaderText("Are you sure you want to delete " + product.getProductName() + "?");
        confirmDialog.setContentText("This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Actual deletion would be implemented in ProductDAO
                boolean deleted = productDAO.deleteProduct(product.getProductId());

                if (deleted) {
                    masterData.remove(product);
                    updateCategoryCards();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Product Deleted");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Product has been successfully deleted.");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Deletion Failed");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Could not delete the product. Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });
    }
}