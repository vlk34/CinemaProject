package com.group18.controller.cashier.stageSpecificFiles;
import com.group18.controller.cashier.CashierController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class CashierPaymentController {
    @FXML private Label movieTitleLabel;
    @FXML private Label hallLabel;
    @FXML private Label sessionLabel;
    @FXML private Label seatsLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label ageDiscountLabel;
    @FXML private Label amountDueLabel;
    @FXML private TextField amountReceivedField;
    @FXML private Label changeLabel;
    @FXML private Button processPaymentButton;

    @FXML private TableView<ProductItem> productsTable;
    @FXML private TableColumn<ProductItem, String> productNameColumn;
    @FXML private TableColumn<ProductItem, Integer> quantityColumn;
    @FXML private TableColumn<ProductItem, Double> priceColumn;
    @FXML private TableColumn<ProductItem, Double> totalColumn;

    private double totalAmount = 0.0;

    private CashierController cashierController;

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    @FXML
    private void initialize() {
        // Setup table columns
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Setup currency formatting for price columns
        priceColumn.setCellFactory(column -> new TableCell<ProductItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(price));
                }
            }
        });

        totalColumn.setCellFactory(column -> new TableCell<ProductItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(total));
                }
            }
        });

        // Setup amount received validation
        amountReceivedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                amountReceivedField.setText(oldValue);
            } else {
                calculateChange();
            }
        });

        // Load order details
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        // TODO: Load actual order details from previous stages
        // For now, setting sample data
        movieTitleLabel.setText("Sample Movie");
        hallLabel.setText("Hall A");
        sessionLabel.setText("15:30");
        seatsLabel.setText("A1, A2");
        customerNameLabel.setText("John Doe");
        ageDiscountLabel.setText("50% (Age: 65)");

        totalAmount = 100.0; // Sample total amount
        amountDueLabel.setText(formatCurrency(totalAmount));

        // Load products table with sample data
        // This should be replaced with actual data from the cart
    }

    @FXML
    private void handleProcessPayment() {
        if (validatePayment()) {
            // Show confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Payment");
            confirm.setHeaderText("Process Payment");
            confirm.setContentText("Are you sure you want to process this payment?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                processPayment();
            }
        }
    }

    private boolean validatePayment() {
        try {
            double amountReceived = Double.parseDouble(amountReceivedField.getText());
            if (amountReceived < totalAmount) {
                showError("Insufficient Payment",
                        "The amount received is less than the total amount due.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError("Invalid Amount",
                    "Please enter a valid amount.");
            return false;
        }
    }

    private void processPayment() {
        // TODO: Implement actual payment processing
        // 1. Save transaction to database
        // 2. Generate tickets
        // 3. Generate receipt
        // 4. Update inventory
        // 5. Clear cart

        // Show success message
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Payment Successful");
        success.setHeaderText(null);
        success.setContentText("Payment has been processed successfully. Printing tickets and receipt...");
        success.showAndWait();

        // Generate and display tickets/receipt
        generateTicketsAndReceipt();
    }

    private void calculateChange() {
        try {
            double amountReceived = Double.parseDouble(amountReceivedField.getText());
            double change = amountReceived - totalAmount;
            changeLabel.setText(formatCurrency(Math.max(0, change)));
        } catch (NumberFormatException e) {
            changeLabel.setText(formatCurrency(0));
        }
    }

    private void generateTicketsAndReceipt() {
        // TODO: Implement ticket and receipt generation
        // This should create PDF/HTML documents as specified in the requirements
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
        return formatter.format(amount);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Product data class for table
    public static class ProductItem {
        private String name;
        private int quantity;
        private double price;
        private double total;

        public ProductItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.total = quantity * price;
        }

        // Getters
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}