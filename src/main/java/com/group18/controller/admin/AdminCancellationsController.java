package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.dao.OrderDAO;
import com.group18.dao.ProductDAO;
import com.group18.dao.ScheduleDAO;
import com.group18.model.Order;
import com.group18.model.OrderItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCancellationsController {
    @FXML
    private TableView<Order> requestsTable;

    @FXML
    private TableColumn<Order, String> orderIdColumn;
    @FXML
    private TableColumn<Order, String> customerColumn;
    @FXML
    private TableColumn<Order, String> bookingIdColumn;
    @FXML
    private TableColumn<Order, String> typeColumn;
    @FXML
    private TableColumn<Order, String> itemsColumn;
    @FXML
    private TableColumn<Order, Double> amountColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private TableColumn<Order, Void> actionsColumn;
    @FXML
    private TableColumn<Order, Void> receiptColumn;
    @FXML
    private TableColumn<Order, Void> ticketsColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> requestTypeCombo;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private Label pendingCountLabel;

    @FXML
    private Label processedCountLabel;

    @FXML
    private Label refundedAmountLabel;

    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private MovieDAO movieDAO;
    private ScheduleDAO scheduleDAO;

    @FXML
    private void initialize() {
        // Initialize DAOs
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        movieDAO = new MovieDAO();
        scheduleDAO = new ScheduleDAO();

        // Setup combo boxes
        setupComboBoxes();

        // Setup table columns
        setupTableColumns();

        // Load initial data
        loadOrders();

        // Setup event handlers
        setupEventHandlers();
    }

    private void updateStats() {
        OrderDAO.CancellationStats stats = orderDAO.getCancellationStats();

        if (pendingCountLabel != null) {
            pendingCountLabel.setText(String.valueOf(stats.getPendingCount()));
        }

        if (processedCountLabel != null) {
            processedCountLabel.setText(String.valueOf(stats.getProcessedToday()));
        }

        if (refundedAmountLabel != null) {
            refundedAmountLabel.setText(String.format("₺%.2f", stats.getRefundedToday()));
        }
    }

    private void setupComboBoxes() {
        requestTypeCombo.getItems().addAll("All", "Ticket", "Product", "Mixed");
        requestTypeCombo.setValue("All");

        statusCombo.getItems().addAll("All", "Pending", "Processed", "Rejected");
        statusCombo.setValue("All");

        // Add listeners for filtering
        requestTypeCombo.setOnAction(e -> filterOrders());
        statusCombo.setOnAction(e -> filterOrders());
    }

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdColumn.setStyle("-fx-alignment: CENTER;");

        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        bookingIdColumn.setStyle("-fx-alignment: CENTER;");

        // Custom cell factory for customer column
        customerColumn.setCellValueFactory(data -> {
            Order order = data.getValue();
            List<OrderItem> items = order.getOrderItems();

            // Find the first "ticket" item and get the customer name
            OrderItem ticketItem = items.stream()
                    .filter(item -> "ticket".equals(item.getItemType()))
                    .findFirst()
                    .orElse(null);

            if (ticketItem != null) {
                String fullName = ticketItem.getOccupantFirstName() + " " + ticketItem.getOccupantLastName();
                return new SimpleStringProperty(fullName);
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        customerColumn.setStyle("-fx-alignment: CENTER;");

        // Type column showing order type
        typeColumn.setCellValueFactory(data -> {
            Order order = data.getValue();
            return new SimpleStringProperty(determineOrderType(order));
        });
        typeColumn.setStyle("-fx-alignment: CENTER;");

        // Items summary column
        itemsColumn.setCellValueFactory(data -> {
            Order order = data.getValue();
            String summary = createItemsSummary(order.getOrderItems());
            return new SimpleStringProperty(summary);
        });
        itemsColumn.setStyle("-fx-alignment: CENTER;");

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        amountColumn.setStyle("-fx-alignment: CENTER;");

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setStyle("-fx-alignment: CENTER;");

        // Setup action buttons column
        actionsColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button processButton = new Button("Process");
            private final Button rejectButton = new Button("Reject");

            {
                // Default styling for process button (less saturated green)
                processButton.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;");
                processButton.setOnMouseEntered(event ->
                        processButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );
                processButton.setOnMouseExited(event ->
                        processButton.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );

                // Default styling for reject button (less saturated red)
                rejectButton.setStyle("-fx-background-color: #D9534F; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;");
                rejectButton.setOnMouseEntered(event ->
                        rejectButton.setStyle("-fx-background-color: #C9302C; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );
                rejectButton.setOnMouseExited(event ->
                        rejectButton.setStyle("-fx-background-color: #D9534F; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );

                // Action handlers
                processButton.setOnAction(e -> handleProcessCancellation(getTableView().getItems().get(getIndex())));
                rejectButton.setOnAction(e -> handleRejectCancellation(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(order.getStatus())) {
                        HBox buttons = new HBox(5, processButton, rejectButton);
                        buttons.setAlignment(Pos.CENTER);
                        setGraphic(buttons);
                    } else {
                        setGraphic(null); // No buttons for non-pending orders
                    }
                }
            }
        });

        // Setup receipt column
        receiptColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewReceiptButton = new Button("View Receipt");
            {
                // Styling for the button
                viewReceiptButton.setStyle("-fx-background-color: #F6F2F8; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;");

                // Hover effect
                viewReceiptButton.setOnMouseEntered(event ->
                        viewReceiptButton.setStyle("-fx-background-color: #E2DFF1; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );
                viewReceiptButton.setOnMouseExited(event ->
                        viewReceiptButton.setStyle("-fx-background-color: #F6F2F8; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );

                viewReceiptButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewReceipt(order.getOrderId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Check if receipt exists before showing button
                    byte[] receipt = orderDAO.retrieveReceipt(getTableView().getItems().get(getIndex()).getOrderId());
                    if (receipt != null && receipt.length > 0) {
                        setGraphic(viewReceiptButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        receiptColumn.setStyle("-fx-alignment: CENTER;");

        // Setup tickets column
        ticketsColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewTicketsButton = new Button("View Ticket");
            {
                // Styling for the button
                viewTicketsButton.setStyle("-fx-background-color: #F6F2F8; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;");

                // Hover effect
                viewTicketsButton.setOnMouseEntered(event ->
                        viewTicketsButton.setStyle("-fx-background-color: #E2DFF1; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );
                viewTicketsButton.setOnMouseExited(event ->
                        viewTicketsButton.setStyle("-fx-background-color: #F6F2F8; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-padding: 2 6 2 6;")
                );

                viewTicketsButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewTickets(order.getOrderId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Check if tickets exist before showing button
                    byte[] tickets = orderDAO.retrieveTickets(getTableView().getItems().get(getIndex()).getOrderId());
                    if (tickets != null && tickets.length > 0) {
                        setGraphic(viewTicketsButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        ticketsColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void viewTickets(int orderId) {
        byte[] ticketsPdf = orderDAO.retrieveTickets(orderId);

        if (ticketsPdf != null) {
            try {
                // Create a temporary file
                File tempFile = File.createTempFile("tickets_" + orderId, ".pdf");
                tempFile.deleteOnExit(); // Ensure file is deleted when JVM exits

                // Write PDF content to temp file
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(ticketsPdf);
                }

                // Open the PDF in default browser
                Desktop.getDesktop().browse(tempFile.toURI());

            } catch (IOException e) {
                // Show error if opening fails
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Tickets View Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Could not open tickets: " + e.getMessage());
                errorAlert.showAndWait();
            }
        } else {
            // Show error if no tickets found
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Tickets Not Found");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("No tickets found for this order.");
            errorAlert.showAndWait();
        }
    }

    private void viewReceipt(int orderId) {
        byte[] receiptPdf = orderDAO.retrieveReceipt(orderId);

        if (receiptPdf != null) {
            try {
                // Create a temporary file
                File tempFile = File.createTempFile("receipt_" + orderId, ".pdf");
                tempFile.deleteOnExit(); // Ensure file is deleted when JVM exits

                // Write PDF content to temp file
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(receiptPdf);
                }

                // Open the PDF in default browser
                Desktop.getDesktop().browse(tempFile.toURI());

            } catch (IOException e) {
                // Show error if opening fails
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Receipt View Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Could not open receipt: " + e.getMessage());
                errorAlert.showAndWait();
            }
        } else {
            // Show error if no receipt found
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Receipt Not Found");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("No receipt found for this order.");
            errorAlert.showAndWait();
        }
    }

    private String determineOrderType(Order order) {
        List<OrderItem> items = order.getOrderItems();
        boolean hasTickets = items.stream().anyMatch(item -> "ticket".equals(item.getItemType()));
        boolean hasProducts = items.stream().anyMatch(item -> "product".equals(item.getItemType()));

        if (hasTickets && hasProducts) return "Mixed";
        if (hasTickets) return "Ticket";
        if (hasProducts) return "Product";
        return "Unknown";
    }

    private String createItemsSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "No items";

        int ticketCount = 0;
        int productCount = 0;

        for (OrderItem item : items) {
            if ("ticket".equals(item.getItemType())) {
                ticketCount += item.getQuantity();
            } else {
                productCount += item.getQuantity();
            }
        }

        StringBuilder summary = new StringBuilder();
        if (ticketCount > 0) {
            summary.append(ticketCount).append(" tickets");
        }
        if (productCount > 0) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(productCount).append(" products");
        }

        return summary.toString();
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadOrders());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOrders());
    }

    private void loadOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        requestsTable.setItems(FXCollections.observableArrayList(orders));
        filterOrders();
        updateStats();
    }

    private void filterOrders() {
        if (requestsTable.getItems() == null) return;

        String searchText = searchField.getText().toLowerCase();
        String typeFilter = requestTypeCombo.getValue();
        String statusFilter = statusCombo.getValue();

        List<Order> filteredOrders = orderDAO.getAllOrders().stream()
                .filter(order -> {
                    boolean matchesSearch = String.valueOf(order.getOrderId()).contains(searchText) ||
                            (order.getOrderItems().stream()
                                    .anyMatch(item -> (item.getOccupantFirstName() + " " + item.getOccupantLastName())
                                            .toLowerCase().contains(searchText)));

                    boolean matchesType = "All".equals(typeFilter) || matchesOrderType(order, typeFilter);
                    boolean matchesStatus = "All".equals(statusFilter) ||
                            (order.getStatus() != null && order.getStatus().equalsIgnoreCase(statusFilter));

                    return matchesSearch && matchesType && matchesStatus;
                })
                .collect(Collectors.toList());

        requestsTable.setItems(FXCollections.observableArrayList(filteredOrders));
    }

    private boolean matchesOrderType(Order order, String typeFilter) {
        List<OrderItem> items = order.getOrderItems();
        boolean hasTickets = items.stream().anyMatch(item -> "ticket".equals(item.getItemType()));
        boolean hasProducts = items.stream().anyMatch(item -> "product".equals(item.getItemType()));

        return switch (typeFilter) {
            case "Ticket" -> hasTickets && !hasProducts;
            case "Product" -> !hasTickets && hasProducts;
            case "Mixed" -> hasTickets && hasProducts;
            default -> true;
        };
    }

    private void handleProcessCancellation(Order order) {
        if (!"PENDING".equals(order.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "Only pending cancellations can be processed.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Process Cancellation Request");
        confirm.setContentText("Are you sure you want to process this cancellation request?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = orderDAO.processCancellation(order.getOrderId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Cancellation processed successfully");
                    loadOrders();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to process cancellation");
                }
            }
        });
    }

    private void handleRejectCancellation(Order order) {
        if (!"PENDING".equals(order.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "Only pending cancellations can be rejected.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Rejection");
        confirm.setHeaderText("Reject Cancellation Request");
        confirm.setContentText("Are you sure you want to reject this cancellation request?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = orderDAO.rejectCancellation(order.getOrderId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Cancellation request rejected");
                    loadOrders();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to reject cancellation");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}