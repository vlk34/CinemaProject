package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.dao.OrderDAO;
import com.group18.dao.ProductDAO;
import com.group18.dao.ScheduleDAO;
import com.group18.model.Order;
import com.group18.model.OrderItem;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.geometry.Insets;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The AdminCancellationsController class manages the administrative functions related
 * to order cancellation requests within the system. It provides functionality for
 * viewing, filtering, and processing cancellation requests from users. The controller
 * handles user interactions and updates the UI elements accordingly to reflect data
 * and administrative actions.
 *
 * This controller interacts with the underlying data layers, such as DAOs for orders,
 * products, movies, and schedules, to manage persistent data. It also provides utilities
 * to display key information like tickets, receipts, and request summaries.
 * Additionally, it includes methods to filter, process, or reject cancellations, as well
 * as manage relevant statistics.
 */
public class AdminCancellationsController {
    /**
     * A TableView component used for displaying the cancellation requests of orders
     * in the admin interface. Each row in the table corresponds to an order and
     * provides detailed information about the order, such as the customer, order ID,
     * booking ID, order type, and status. This table can also include actions
     * associated with individual orders, such as viewing receipts or processing cancellations.
     *
     * The component is dynamically populated and updated based on the cancellation
     * requests loaded by the system and allows filtering, searching, and interaction
     * with the displayed orders.
     */
    @FXML
    private TableView<Order> requestsTable;

    /**
     * Represents a TableColumn in the UI for displaying the unique identifier of an order.
     * This column is bound to the order ID value of each {@link Order} instance,
     * specifically representing the {@code orderId} field from the {@link Order} object.
     * It is used within the table component in the AdminCancellationsController to
     * display order identification details to the user.
     */
    @FXML
    private TableColumn<Order, String> orderIdColumn;
    /**
     * Represents a table column in the cancellations management table that displays customer information.
     * This column is configured to display string values representing the customer's details associated
     * with each order.
     *
     * The data type for this column is defined as {@code String}, extracted from {@code Order} objects.
     */
    @FXML
    private TableColumn<Order, String> customerColumn;
    /**
     * Represents the column in a table for displaying the booking ID associated with an order.
     * This column is specifically used in the context of the `AdminCancellationsController` class
     * to populate and manage data related to booking IDs in a table view.
     *
     * The booking ID column displays a unique identifier for a booking within the system,
     * formatted as a string.
     */
    @FXML
    private TableColumn<Order, String> bookingIdColumn;
    /**
     * The TableColumn instance representing the "Type" column in the table view used for displaying
     * order-related data in the "Admin Cancellations" management interface. This column displays
     * the type of the order (e.g., product, ticket, or other categories) as a string.
     *
     * The "typeColumn" field binds to the relevant property of the {@code Order} object,
     * specifically the {@code determineOrderType} logic in the encompassing controller class.
     * It is used to assist in filtering, sorting, and rendering order type information.
     */
    @FXML
    private TableColumn<Order, String> typeColumn;
    /**
     * Represents a table column in the UI that displays the summary of items included
     * in an order. This column will show a string representation of the items
     * associated with each order in the table.
     *
     * The column is designed to bind to the `items` attribute of the {@code Order} class,
     * specifically rendering a concise summary of the items in a human-readable format.
     * Typically used within a {@code TableView} in the administrative panel.
     */
    @FXML
    private TableColumn<Order, String> itemsColumn;
    /**
     * Represents a table column in the Admin Cancellations interface that displays
     * the monetary amount associated with an order. Specifically, this column
     * is tied to the {@link Order#getTotalPrice()} method and is designed to show
     * the total price or cost of an order in the cancellations table view.
     *
     * The column is a part of the graphical user interface (GUI) and is responsible
     * for displaying data of type {@code Double}, which corresponds to the total
     * monetary value for each order in the table.
     *
     * This field is annotated with {@code @FXML}, indicating that it is linked
     * to an FXML file and is injected automatically when the FXML file is loaded.
     */
    @FXML
    private TableColumn<Order, Double> amountColumn;
    /**
     * Represents the "Status" column in the orders table within the cancellation management interface.
     * This column displays the current status of an order, such as "PENDING", "COMPLETED", "CANCELLED",
     * or other business-specific statuses. The displayed status corresponds to the {@code status} field
     * in the {@link Order} class.
     *
     * This column is used to provide a clear, textual representation of the state of each order to the user.
     */
    @FXML
    private TableColumn<Order, String> statusColumn;
    /**
     * Represents the "Actions" column in the cancellations management table within the
     * AdminCancellationsController. This column is dynamically populated with action
     * buttons or controls (e.g., "View", "Process", or "Reject") specific to each
     * cancellation request displayed in the table.
     *
     * Provides a way for the admin to interact with individual entries, enabling them
     * to perform necessary actions for managing order cancellations. Each cell in this
     * column is associated with a corresponding {@link Order} entry.
     *
     * Declared as a TableColumn binding for direct use with FXML framework.
     */
    @FXML
    private TableColumn<Order, Void> actionsColumn;
    /**
     * Represents a table column in the cancellations management view that holds
     * buttons or actions allowing users to view receipts for specific orders.
     *
     * This column does not directly represent data but provides user interaction
     * for accessing detailed receipt information for orders within the table.
     *
     * The column operates in coordination with the underlying {@code Order} object,
     * where each cell in this column is dynamically populated with an interaction
     * mechanism (e.g., a button) tied to the respective order's receipt details.
     */
    @FXML
    private TableColumn<Order, Void> receiptColumn;
    /**
     * Represents a table column in the admin cancellations interface that is intended
     * to handle operations or actions related to viewing tickets associated with
     * a specific order. This column is of type {@code TableColumn<Order, Void>},
     * where the "Void" indicates that the column is not inherently tied to data
     * representation but is instead used for functionalities like rendering action controls.
     *
     * In the context of the associated {@code AdminCancellationsController}, this column
     * is used to display controls or buttons (such as a "View Tickets" button) for
     * interacting with ticket-related data of an order. The data type of the {@code Order}
     * parameter represents the contextual model this column operates on.
     *
     * This variable is annotated with {@code @FXML}, enabling it to be injected and referenced
     * within the FXML layout file that defines the GUI structure.
     */
    @FXML
    private TableColumn<Order, Void> ticketsColumn;

    /**
     * The Refresh button in the Admin Cancellations view.
     * This button is typically used to trigger the reloading or updating
     * of the displayed data, such as pending and processed cancellations.
     *
     * It is controlled by the AdminCancellationsController class and
     * can be associated with event handlers for performing the refresh
     * operation when clicked.
     */
    @FXML
    private Button refreshButton;

    /**
     * Represents a text input field used for searching orders in the admin cancellations interface.
     * This field allows the user to input search criteria to filter and locate specific orders within
     * the cancellations view. It is bound to the UI component defined in the associated FXML file.
     *
     * Responsibilities:
     * - Enables text-based filtering of orders.
     * - Triggers filtering logic when the input changes, narrowing down the displayed data.
     *
     * This field is managed by the {@code AdminCancellationsController} and is part of the
     * user interface elements related to managing and processing order cancellations.
     */
    @FXML
    private TextField searchField;
    /**
     * Represents a ComboBox that is bound to the JavaFX UI and is used to allow
     * users to select a specific type of request for filtering or processing purposes.
     *
     * This component is defined as part of the {@code AdminCancellationsController}.
     * It is likely populated with predefined options during the initialization process
     * and interacts with other components or methods to filter or handle requests based
     * on the selected type.
     */
    @FXML
    private ComboBox<String> requestTypeCombo;
    /**
     * Represents a ComboBox element in the user interface for managing order statuses.
     * This ComboBox is designed to provide a list of order status options,
     * allowing administrators to filter and manage orders based on their current status.
     * Typical status options could include "PENDING", "COMPLETED", or "CANCELLED".
     *
     * This field is initialized and configured within the controller's initialization
     * process and is used in conjunction with other UI elements for managing cancellations.
     *
     * Marked with the `@FXML` annotation, this ComboBox is linked to a corresponding
     * element in the FXML file and is bound to the application UI defined in the FXML
     * layout.
     */
    @FXML
    private ComboBox<String> statusCombo;
    /**
     * The `pendingCountLabel` represents a UI label component in the `AdminCancellationsController`
     * class that displays the count of orders currently in the "Pending" status.
     *
     * This label is dynamically updated to reflect the number of pending orders
     * whenever the data or filters are refreshed in the application.
     */
    @FXML
    private Label pendingCountLabel;

    /**
     * A label in the user interface that displays the count of processed cancellation requests.
     * This label is used to provide real-time feedback to the administrator about the
     * number of cancellation requests that have been successfully processed.
     * It is dynamically updated based on system interactions.
     */
    @FXML
    private Label processedCountLabel;

    /**
     * A UI label element that displays the total refunded amount in the administrative
     * cancellations interface. This label is used to provide a visual representation
     * of the cumulative refunded value for processed orders or cancellations.
     * It is updated dynamically based on the business logic related to cancellations.
     */
    @FXML
    private Label refundedAmountLabel;

    /**
     * A data access object for managing operations related to orders.
     * This variable is used within the AdminCancellationsController to perform CRUD operations,
     * retrieve order details, and handle cancellations.
     */
    private OrderDAO orderDAO;
    /**
     * Provides access to product-related operations and data storage handling.
     * The ProductDAO instance is used to manage product records, including CRUD operations,
     * stock management, and retrieval of product data for the application.
     */
    private ProductDAO productDAO;
    /**
     * Responsible for accessing and managing movie-related data.
     * Provides methods to interact with the MovieDAO for operations such as fetching,
     * updating, and manipulating movie records in the database.
     */
    private MovieDAO movieDAO;
    /**
     * The ScheduleDAO instance used to manage operations related to movie schedules.
     * Acts as the data access layer to interact with the "schedules" table in the database.
     * Utilized by AdminCancellationsController for handling schedule-related functionality.
     */
    private ScheduleDAO scheduleDAO;

    /**
     * Initializes the controller by setting up DAOs, configuring UI components, and loading initial data.
     *
     * Responsibilities include:
     * - Initializing data access objects (DAOs) for orders, products, movies, and schedules.
     * - Configuring combo boxes and adding hover animations to buttons.
     * - Setting up table columns for displaying data related to cancellations.
     * - Loading initial orders to populate the user interface.
     * - Setting up event handlers for user interactions.
     *
     * This method is executed automatically when the FXML file is loaded, ensuring
     * that the controller and its associated view are properly initialized and ready for use.
     */
    @FXML
    private void initialize() {
        // Initialize DAOs
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        movieDAO = new MovieDAO();
        scheduleDAO = new ScheduleDAO();

        // Setup combo boxes
        setupComboBoxes();
        setupButtonHoverAnimation(refreshButton);
        // Setup table columns
        setupTableColumns();

        // Load initial data
        loadOrders();

        // Setup event handlers
        setupEventHandlers();
    }

    /**
     * Updates the statistical information displayed in the admin cancellations view.
     * This includes updating the displayed counts for pending cancellations,
     * processed cancellations, and the refunded amount for the current day.
     *
     * The method retrieves the cancellation statistics from the OrderDAO and updates
     * the corresponding labels if they are not null.
     *
     * Updates:
     * - Pending cancellations count.
     * - Processed cancellations count for the day.
     * - Total refunded amount for the day.
     */
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

    /**
     * Configures the combo boxes used for filtering orders in the interface.
     *
     * The method populates the `requestTypeCombo` with predefined values representing
     * different order types: "All", "Ticket", "Product", and "Mixed". It sets the default
     * selection to "All". Similarly, the `statusCombo` is populated with predefined
     * values representing the order status: "All", "Pending", "Rejected",
     * "Processed (Full)", "Processed (Products)", and "Processed (Tickets)", with
     * "All" set as the default option.
     *
     * Event handlers are added to both combo boxes such that any change in their values
     * triggers the `filterOrders` method, which updates the displayed order list
     * based on the selected filters.
     */
    private void setupComboBoxes() {
        requestTypeCombo.getItems().addAll("All", "Ticket", "Product", "Mixed");
        requestTypeCombo.setValue("All");

        statusCombo.getItems().addAll("All", "Pending", "Rejected", "Processed (Full)", "Processed (Products)", "Processed (Tickets)");
        statusCombo.setValue("All");

        requestTypeCombo.setOnAction(e -> filterOrders());
        statusCombo.setOnAction(e -> filterOrders());
    }

    /**
     * Configures table columns for displaying and interacting with order data in a user interface.
     * This method sets up cell value factories, custom cell factories, and styles for various columns
     * in the table to handle order-related information, such as general attributes, customer details,
     * order type, actions, receipts, and tickets.
     *
     * Steps include:
     * - Setting cell value factories to bind columns to their respective data from the order model.
     * - Applying alignment and styles for consistent column appearance.
     * - Defining custom cell factories for special columns like customer details, actions, receipts, and tickets.
     * - Adding interactive buttons for actions like processing/rejecting cancellations, viewing receipts, and viewing tickets.
     *
     * The interaction with orders is supported by action handlers and validators to ensure buttons
     * appear and respond contextually based on the order's status and availability of associated resources.
     */
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

    /**
     * Opens and displays the tickets associated with a specific order. The tickets are retrieved
     * as a PDF file, saved temporarily on the file system, and opened using the default application
     * for handling PDF files on the user's system. If any errors occur or no tickets are found,
     * appropriate error alerts are displayed to the user.
     *
     * @param orderId The unique identifier of the order for which the tickets need to be viewed.
     */
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

    /**
     * Opens and displays the receipt in PDF format for the specified order.
     *
     * The method retrieves the receipt PDF from the database using the given order ID. If found,
     * it creates a temporary file to store the PDF content, then opens the PDF in the user's
     * default browser. If the receipt is not found or an error occurs while opening, appropriate
     * alerts are displayed to notify the user.
     *
     * @param orderId The ID of the order for which the receipt will be displayed.
     */
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

    /**
     * Determines the type of order based on its items.
     * The order can be categorized as "Ticket" if it only contains ticket items,
     * "Product" if it only contains product items, "Mixed" if it contains both
     * ticket and product items, or "Unknown" if neither category applies.
     *
     * @param order the order to analyze, containing a list of items
     * @return a string representing the type of the order, which can be
     *         "Ticket", "Product", "Mixed", or "Unknown"
     */
    private String determineOrderType(Order order) {
        List<OrderItem> items = order.getOrderItems();
        boolean hasTickets = items.stream().anyMatch(item -> "ticket".equals(item.getItemType()));
        boolean hasProducts = items.stream().anyMatch(item -> "product".equals(item.getItemType()));

        if (hasTickets && hasProducts) return "Mixed";
        if (hasTickets) return "Ticket";
        if (hasProducts) return "Product";
        return "Unknown";
    }

    /**
     * Creates a summary string for the given list of items, categorizing them into tickets and products
     * and displaying their respective quantities.
     *
     * @param items the list of {@code OrderItem} objects to summarize; if {@code null} or empty,
     *              "No items" is returned
     * @return a textual summary of the items, showing the count of tickets and/or products;
     *         for example, "3 tickets, 5 products"
     */
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

    /**
     * Configures event handlers for UI components in the controller.
     *
     * The method sets up an action handler for the refresh button to reload the list of orders
     * by invoking the {@code loadOrders} method. It also adds a listener to the text property
     * of the search field, so that the displayed orders are filtered in real-time based on
     * the search input by invoking the {@code filterOrders} method.
     */
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadOrders());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOrders());
    }

    /**
     * Loads all orders from the database and updates the user interface with the retrieved data.
     * <p>
     * This method performs the following steps:
     * - Retrieves a list of all orders from the `orderDAO`.
     * - Updates the `requestsTable` in the user interface with the list of orders
     *   as an observable list.
     * - Applies necessary filtering to the orders using the `filterOrders` method.
     * - Updates relevant statistics related to the loaded orders by invoking `updateStats`.
     * <p>
     * This method is typically invoked to refresh and display the most recent order data
     * in the application interface.
     */
    private void loadOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        requestsTable.setItems(FXCollections.observableArrayList(orders));
        filterOrders();
        updateStats();
    }

    /**
     * Filters the list of orders displayed in the request table based on the search text,
     * request type, and status filters provided by the user.
     *
     * The method performs the following:
     * 1. Retrieves the user input from the search text field, request type combo box,
     *    and status combo box.
     * 2. Maps the user-selected status values to corresponding backend status values.
     * 3. Filters the list of orders using the following conditions:
     *    - The order ID or occupant's name contains the search text.
     *    - The order type matches the selected request type (or "All" is selected).
     *    - The order status matches the selected status (or "All" is selected).
     * 4. Updates the items in the request table with the filtered results.
     *
     * This method assumes that `requestsTable`, `searchField`, `requestTypeCombo`,
     * `statusCombo`, and `orderDAO` are properly initialized.
     *
     * Note:
     * - If the table items are null, the method exits early without applying any filters.
     * - If the status mapping does not contain a user-selected status,
     *   it defaults to "All".
     */
    private void filterOrders() {
        if (requestsTable.getItems() == null) return;

        String searchText = searchField.getText().toLowerCase();
        String typeFilter = requestTypeCombo.getValue();
        String statusFilter = statusCombo.getValue();

        // Map of display values to backend values
        Map<String, String> statusMapping = Map.of(
                "All", "All",
                "Pending", "PENDING",
                "Rejected", "REJECTED",
                "Processed (Full)", "PROCESSED_FULL",
                "Processed (Products)", "PROCESSED_PRODUCTS",
                "Processed (Tickets)", "PROCESSED_TICKETS"
        );

        // Get the backend status value
        String backendStatusFilter = statusMapping.getOrDefault(statusFilter, "All");

        List<Order> filteredOrders = orderDAO.getAllOrders().stream()
                .filter(order -> {
                    boolean matchesSearch = String.valueOf(order.getOrderId()).contains(searchText) ||
                            order.getOrderItems().stream()
                                    .anyMatch(item -> (item.getOccupantFirstName() + " " + item.getOccupantLastName())
                                            .toLowerCase().contains(searchText));

                    boolean matchesType = "All".equals(typeFilter) || matchesOrderType(order, typeFilter);
                    boolean matchesStatus = "All".equals(backendStatusFilter) ||
                            (order.getStatus() != null && order.getStatus().equalsIgnoreCase(backendStatusFilter));

                    return matchesSearch && matchesType && matchesStatus;
                })
                .collect(Collectors.toList());

        requestsTable.setItems(FXCollections.observableArrayList(filteredOrders));
    }


    /**
     * Determines if the given order matches the specified type filter.
     * The method checks the types of items in the order (e.g., tickets, products)
     * and evaluates whether they correspond to the filter criteria.
     *
     * @param order the order to evaluate, containing the list of items and related details
     * @param typeFilter the filter type to match against; valid values are "Ticket" (only tickets),
     *                   "Product" (only products), "Mixed" (both ticket and product), and
     *                   other values which default to allowing all orders
     * @return true if the order matches the specified type filter, false otherwise
     */
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

    /**
     * Handles the cancellation process for a specific order. This method checks
     * if the cancellation is allowed based on the order's status, displays a
     * user interface for selecting items to cancel, and processes the cancellation
     * if confirmed by the user.
     *
     * @param order The order for which the cancellation process is invoked.
     *              Must contain the necessary details such as status and items
     *              to perform validation and processing.
     */
    private void handleProcessCancellation(Order order) {
        if (!"PENDING".equals(order.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Only pending cancellations can be processed.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Process Cancellation");
        dialog.setHeaderText("Select Items to Cancel");

        ButtonType processButtonType = new ButtonType("Process", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #F5F7FA;");

        List<OrderItem> items = order.getOrderItems();
        long ticketCount = items.stream().filter(item -> "ticket".equals(item.getItemType())).count();
        long productCount = items.stream().filter(item -> "product".equals(item.getItemType())).count();

        Label headerLabel = new Label("Select which items to cancel:");
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        int totalProductQuantity = items.stream()
                .filter(item -> "product".equals(item.getItemType()))
                .mapToInt(OrderItem::getQuantity)
                .sum();

        VBox ticketSection = new VBox(5);
        CheckBox ticketsCheckbox = new CheckBox("Cancel Tickets (" + ticketCount + " " + (ticketCount == 1 ? "ticket" : "tickets") + ")");
        ticketsCheckbox.setStyle("-fx-font-size: 13px;");

        BigDecimal ticketSubtotal = items.stream()
                .filter(item -> "ticket".equals(item.getItemType()))
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate ticket refund with 20% tax
        BigDecimal ticketTaxRate = new BigDecimal("0.20");
        BigDecimal ticketTotalWithTax = ticketSubtotal.multiply(BigDecimal.ONE.add(ticketTaxRate));

        Label ticketPriceLabel = new Label(String.format(
                "Ticket Subtotal: ₺%.2f%nTicket Tax (20%%): ₺%.2f%nTicket Refund Total: ₺%.2f",
                ticketSubtotal,
                ticketSubtotal.multiply(ticketTaxRate),
                ticketTotalWithTax
        ));
        ticketPriceLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");

        ticketSection.getChildren().addAll(ticketsCheckbox, ticketPriceLabel);

        VBox productSection = new VBox(5);
        CheckBox productsCheckbox = new CheckBox("Cancel Products (" + totalProductQuantity + " " + (totalProductQuantity == 1 ? "item" : "items") + ")");
        productsCheckbox.setStyle("-fx-font-size: 13px;");

        BigDecimal productSubtotal = items.stream()
                .filter(item -> "product".equals(item.getItemType()))
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate product refund with 10% tax
        BigDecimal productTaxRate = new BigDecimal("0.10");
        BigDecimal productTotalWithTax = productSubtotal.multiply(BigDecimal.ONE.add(productTaxRate));

        Label productPriceLabel = new Label(String.format(
                "Product Subtotal: ₺%.2f%nProduct Tax (10%%): ₺%.2f%nProduct Refund Total: ₺%.2f",
                productSubtotal,
                productSubtotal.multiply(productTaxRate),
                productTotalWithTax
        ));
        productPriceLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");

        productSection.getChildren().addAll(productsCheckbox, productPriceLabel);

        if (ticketCount == 0) {
            ticketsCheckbox.setDisable(true);
            ticketSection.setOpacity(0.5);
        }
        if (productCount == 0) {
            productsCheckbox.setDisable(true);
            productSection.setOpacity(0.5);
        }

        content.getChildren().addAll(headerLabel, ticketSection, productSection);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(400);
        dialog.getDialogPane().setStyle("-fx-background-color: #F5F7FA;");

        Node processButton = dialog.getDialogPane().lookupButton(processButtonType);
        processButton.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white;");
        processButton.setDisable(true);

        ticketsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) ->
                processButton.setDisable(!newVal && !productsCheckbox.isSelected()));
        productsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) ->
                processButton.setDisable(!newVal && !ticketsCheckbox.isSelected()));

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == processButtonType) {
            boolean success = orderDAO.processCancellation(
                    order.getOrderId(),
                    productsCheckbox.isSelected(),
                    ticketsCheckbox.isSelected()
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cancellation processed successfully");
                loadOrders();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to process cancellation");
            }
        }
    }

    /**
     * Handles the rejection of a cancellation request for an order. Displays a warning if the order
     * is not in a "PENDING" status. If the order can be rejected, confirms the action with the user
     * and processes the rejection through the data access layer.
     *
     * @param order the order whose cancellation request is being rejected
     */
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

    /**
     * Displays an alert dialog with the specified type, title, and content.
     *
     * @param type   The type of alert to display (e.g., INFORMATION, WARNING, ERROR, etc.).
     * @param title  The title of the alert dialog.
     * @param content The content or message to display in the alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Configures a hover animation for a button to give it a scaling effect when pressed or released.
     * Creates a smooth scaling transition that slightly shrinks the button on press and restores
     * it to its original size on release. Additionally, ensures the button state is reset when
     * the mouse exits the button while pressed.
     *
     * @param button the Button to which the hover animation will be applied
     */
    public void setupButtonHoverAnimation(Button button) {
        // Create scale transition
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), button);
        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), button);

        // Add pressed state animation
        button.setOnMousePressed(e -> {
            pressTransition.setToX(0.95);
            pressTransition.setToY(0.95);
            pressTransition.play();
        });

        button.setOnMouseReleased(e -> {
            releaseTransition.setToX(1.0);
            releaseTransition.setToY(1.0);
            releaseTransition.play();
        });

        // Reset button state when mouse exits during press
        button.setOnMouseExited(e -> {
            releaseTransition.setToX(1.0);
            releaseTransition.setToY(1.0);
            releaseTransition.play();
        });
    }
}