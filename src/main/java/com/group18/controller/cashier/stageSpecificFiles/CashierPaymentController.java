// CashierPaymentController.java
package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.OrderDAO;
import com.group18.dao.ProductDAO;
import com.group18.dao.UserDAO;
import com.group18.model.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @FXML private TableView<OrderItemTable> orderItemsTable;
    @FXML private TableColumn<OrderItemTable, String> itemNameColumn;
    @FXML private TableColumn<OrderItemTable, Integer> quantityColumn;
    @FXML private TableColumn<OrderItemTable, Double> priceColumn;
    @FXML private TableColumn<OrderItemTable, Double> totalColumn;

    private CashierController cashierController;
    private ShoppingCart cart;
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private User currentCashier;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private ObservableList<OrderItemTable> tableItems = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cart = ShoppingCart.getInstance();
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        setupAmountReceivedValidation();
        processPaymentButton.setDisable(true);
        setupTable();

        currentCashier = userDAO.authenticateUser("cashier1", "cashier1");
        if (currentCashier == null) {
            showError("System Error", "Failed to authenticate cashier.");
            processPaymentButton.setDisable(true);
        }
    }

    private void setupTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemNameColumn.setStyle("-fx-alignment: CENTER;");

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setStyle("-fx-alignment: CENTER;");

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setStyle("-fx-alignment: CENTER;");

        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setStyle("-fx-alignment: CENTER;");

        // Format price and total columns to show currency
        priceColumn.setCellFactory(column -> new TableCell<OrderItemTable, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setAlignment(Pos.CENTER);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(new Locale("tr", "TR")).format(price));
                }
            }
        });

        totalColumn.setCellFactory(column -> new TableCell<OrderItemTable, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setAlignment(Pos.CENTER);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(new Locale("tr", "TR")).format(total));
                }
            }
        });

        orderItemsTable.setItems(tableItems);
    }

    private void setupAmountReceivedValidation() {
        amountReceivedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                amountReceivedField.setText(oldValue);
            } else {
                calculateChange();
                validatePaymentAmount();
            }
        });
    }

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        Movie movie = cashierController.getSelectedMovie();
        MovieSession session = cashierController.getSelectedSession();

        // Set basic information
        movieTitleLabel.setText(movie.getTitle());
        hallLabel.setText(session.getHall());
        sessionLabel.setText(session.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        seatsLabel.setText(cashierController.getSelectedSeats().stream()
                .sorted()
                .collect(Collectors.joining(", ")));

        // Get totals directly from cart controller
        totalAmount = BigDecimal.valueOf(cashierController.getCartController().getTotal());
        amountDueLabel.setText(formatCurrency(totalAmount));

        // Set labels based on cart items
        setCustomerDetailsFromCart();

        // Load items into table
        loadTableItems();
    }

    private void loadTableItems() {
        tableItems.clear();

        // Get items from the cart controller instead of directly from the cart
        CashierCartController cartController = cashierController.getCartController();
        Map<String, HBox> activeCartItems = cartController.getCartItems();
        VBox cartItemsContainer = cartController.getCartItemsContainer();

        // Only process items that are currently in the cart UI
        for (Node node : cartItemsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox itemContainer = (HBox) node;
                VBox details = (VBox) itemContainer.getChildren().get(0);
                Label nameLabel = (Label) details.getChildren().get(0);
                Label priceLabel = (Label) details.getChildren().get(1);
                Label quantityLabel = (Label) itemContainer.getChildren().get(1);

                // Extract item details
                String itemName = nameLabel.getText();

                // Extract price (remove currency symbol and parse)
                String priceText = priceLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
                double price = Double.parseDouble(priceText);

                // Extract quantity (remove 'x' prefix)
                int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

                // Calculate total
                double total = price * quantity;

                tableItems.add(new OrderItemTable(itemName, quantity, price, total));
            }
        }

        // Add tax row
        double taxAmount = cashierController.getCartController().getTax();
        tableItems.add(new OrderItemTable("Tax", 1, taxAmount, taxAmount));
    }

    private void setCustomerDetailsFromCart() {
        cart.getItems().stream()
                .filter(item -> "ticket".equals(item.getItemType()))
                .findFirst()
                .ifPresent(ticketItem -> {
                    customerNameLabel.setText(String.format("%s %s",
                            ticketItem.getOccupantFirstName(),
                            ticketItem.getOccupantLastName()));
                    ageDiscountLabel.setText(ticketItem.getDiscountApplied() ?
                            "Age discount applied" : "No discount");
                });
    }

    private void calculateChange() {
        try {
            BigDecimal amountReceived = new BigDecimal(amountReceivedField.getText());
            BigDecimal change = amountReceived.subtract(totalAmount);
            changeLabel.setText(formatCurrency(change.max(BigDecimal.ZERO)));
        } catch (NumberFormatException e) {
            changeLabel.setText(formatCurrency(BigDecimal.ZERO));
        }
    }

    private void validatePaymentAmount() {
        try {
            BigDecimal amountReceived = new BigDecimal(amountReceivedField.getText());
            processPaymentButton.setDisable(amountReceived.compareTo(totalAmount) < 0);
        } catch (NumberFormatException e) {
            processPaymentButton.setDisable(true);
        }
    }

    @FXML
    private void handleProcessPayment() {
        if (!validatePayment()) {
            return;
        }

        Optional<ButtonType> result = showConfirmationDialog();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processPayment();
        }
    }

    private boolean validatePayment() {
        try {
            BigDecimal amountReceived = new BigDecimal(amountReceivedField.getText());
            if (amountReceived.compareTo(totalAmount) < 0) {
                showError("Insufficient Payment",
                        "The amount received is less than the total amount due.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Please enter a valid amount.");
            return false;
        }
    }

    private Optional<ButtonType> showConfirmationDialog() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Process Payment");
        confirm.setContentText("Are you sure you want to process this payment?");
        return confirm.showAndWait();
    }

    private void processPayment() {
        if (currentCashier == null) {
            showError("System Error", "No authenticated cashier found.");
            return;
        }

        Order order = cart.createOrder();
        order.setCashierId(currentCashier.getUserId());

        if (orderDAO.createOrder(order)) {
            generateTicketsAndReceipt(order);
            showSuccessDialog();
            resetTransaction();
        } else {
            showError("Payment Failed",
                    "Failed to process payment. Please try again.");
        }
    }

    private void generateTicketsAndReceipt(Order order) {
        // TODO: Implement ticket and receipt generation
        // This will be implemented when we add PDF/HTML generation functionality
    }

    private void showSuccessDialog() {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Payment Successful");
        success.setHeaderText(null);
        success.setContentText("Payment has been processed successfully. " +
                "Tickets and receipt have been generated.");
        success.showAndWait();
    }

    private void resetTransaction() {
        System.out.println("Resetting transaction. Cart size before clear: " + cart.getItems().size());
        cart.clear();
        System.out.println("Cart size after clear: " + cart.getItems().size());
        cashierController.resetTransaction();
    }

    private String formatCurrency(BigDecimal amount) {
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
}