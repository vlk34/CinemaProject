package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.MovieDAO;
import com.group18.dao.OrderDAO;
import com.group18.dao.ProductDAO;
import com.group18.dao.UserDAO;
import com.group18.model.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * Controller class responsible for managing the cashier payment process in a JavaFX application.
 * Handles the interaction between the cashier and the application's backend services, enabling
 * the display, management, and processing of user transactions including order items, customer
 * details, and payments.
 *
 * Key Responsibilities:
 * - Initialize and configure UI components for the cashier payment workflow.
 * - Load and display cart details, including customer information and order items.
 * - Handle the payment process, including user confirmation, order persistence, and receipt generation.
 * - Integrate with external data access objects (DAO) to manage orders, products, users, and movies.
 *
 * Dependencies:
 * - Relies on various DAOs for data retrieval and persistence.
 * - Uses the shopping cart controller for accessing cart-related information.
 * - Interacts with the `CashierController` to retrieve cashier and session-specific details.
 *
 * Fields:
 * - Contains multiple fields related to UI components, data models, and DAOs used in the cashier workflow.
 *
 * Methods:
 * - Provides various functionalities including setting up the table, loading order details,
 *   processing payments, and generating receipts.
 */
public class CashierPaymentController {
    /**
     * A Label field used to display the title of a selected movie.
     * This label is part of the user interface and is updated to show the
     * movie name relevant to the current transaction or order.
     *
     * This field is annotated with @FXML, indicating that it is linked
     * to a corresponding element in the application's FXML layout file.
     */
    @FXML private Label movieTitleLabel;
    /**
     * Represents a label in the user interface for displaying the cinema hall associated
     * with the user's booking or selection during the payment process.
     *
     * This label is part of the `CashierPaymentController` and is used to provide
     * visual feedback about the specific hall where a movie or event will take place.
     */
    @FXML private Label hallLabel;
    /**
     * Represents a JavaFX Label used to display the session information
     * in the payment interface of the CashierPaymentController.
     *
     * This label holds and represents the session details such as timing
     * or venue associated with a specific movie booking, allowing clear
     * identification and presentation to the cashier or user.
     */
    @FXML private Label sessionLabel;
    /**
     * Represents a label element in the user interface displaying seat information.
     *
     * This label is used to show details about the selected seats for a specific order
     * or booking within the cashier payment system.
     *
     * It is typically updated dynamically during the transaction process to indicate
     * the selected seat IDs or availability.
     */
    @FXML private Label seatsLabel;
    /**
     * Represents a label for displaying the name of the customer in the CashierPaymentController.
     *
     * This label is used within the user interface to show the customer's name
     * during the transaction process in the payment section. It is dynamically updated
     * based on the customer's details associated with the current order.
     */
    @FXML private Label customerNameLabel;
    /**
     * Represents the label in the user interface that displays age-based discount information for the current transaction.
     *
     * This label is typically updated dynamically to reflect the discount applicable based on the customer's age group,
     * such as child, adult, or senior discounts, during the payment process in the cashier system.
     */
    @FXML private Label ageDiscountLabel;
    /**
     * Label for displaying the total amount due in the cashier payment interface.
     * This label is dynamically updated to reflect the amount to be paid by the customer
     * for the current transaction. The value displayed is typically formatted as currency.
     *
     * It is part of the user interface elements controlled by the CashierPaymentController class.
     */
    @FXML private Label amountDueLabel;
    /**
     * A JavaFX Button that triggers the payment processing workflow in the cashier system.
     *
     * This button is part of the user interface for the payment process. It is expected
     * to be associated with an event handler that executes the logic for processing a payment
     * when the button is clicked. The functionality typically involves validating payment details,
     * confirming transactions, and updating the system with the payment information.
     */
    @FXML private Button processPaymentButton;

    /**
     * A JavaFX TableView designed to display the details of order items in the context
     * of a cashier payment interface.
     *
     * Each row in the table represents an order item, including information such as
     * the item name, quantity, unit price, and total price. The table is dynamically
     * populated with data from instances of {@link OrderItemTable}.
     */
    @FXML private TableView<OrderItemTable> orderItemsTable;
    /**
     * The TableColumn representing the "Item Name" field for an order item in the table view.
     * This column is used to display the name of each item in an order within the
     * associated {@link TableView} in the user interface.
     *
     * It is bound to the `itemName` property in the {@link OrderItemTable} model, enabling
     * the display of item names in the table and supporting dynamic updates when data changes.
     */
    @FXML private TableColumn<OrderItemTable, String> itemNameColumn;
    /**
     * Represents a column in a TableView for displaying the quantity of items in an order.
     *
     * This TableColumn is associated with the `quantity` property of the `OrderItemTable` class.
     * It is used to display and manipulate the quantity of a given order item within the user interface.
     */
    @FXML private TableColumn<OrderItemTable, Integer> quantityColumn;
    /**
     * Represents a table column in the order items table that displays the price of each item.
     * This column is bound to the {@code price} property from the {@link OrderItemTable} model class.
     * It is used to show the unit price of an order item in a JavaFX TableView.
     */
    @FXML private TableColumn<OrderItemTable, Double> priceColumn;
    /**
     * Represents the "Total" column in the table view within the cashier payment interface.
     * This column is used to display the total price of each order item, which is calculated
     * as the product of the quantity and unit price.
     *
     * The column is associated with the {@code total} property of the {@link OrderItemTable} class
     * and allows for dynamic updates in the table view when the underlying data changes.
     */
    @FXML private TableColumn<OrderItemTable, Double> totalColumn;

    /**
     * Represents a reference to the CashierController instance used for managing
     * cashier-related operations within the CashierPaymentController.
     * This variable is used to delegate cashier-specific tasks such as handling
     * payments, orders, and managing cashier data, ensuring the separation of
     * concerns between the payment controller and cashier logic.
     */
    private CashierController cashierController;
    /**
     * Represents the shopping cart that holds the items and details related
     * to the current transaction or order being processed.
     *
     * It is used to manage the state and information of the current order
     * within the CashierPaymentController workflow, such as selected items,
     * quantities, prices, and other order-related data.
     */
    private ShoppingCart cart;
    /**
     * Represents the Data Access Object (DAO) for handling operations related to orders.
     * Provides an interface to communicate with the underlying database or data source for
     * performing order-related CRUD operations, such as retrieval, creation, updating, and deletion.
     * Used primarily within the {@code CashierPaymentController} for managing order data.
     */
    private OrderDAO orderDAO;
    /**
     * Handles interactions with user data in the database.
     * Provides high-level access to the UserDAO for performing CRUD operations
     * and user-related queries within the context of the CashierPaymentController.
     */
    private UserDAO userDAO;
    /**
     * Represents the data access object (DAO) responsible for handling operations
     * related to Product entities in the system. This variable provides an interface
     * for interacting with the data layer, enabling CRUD (Create, Read, Update, Delete)
     * operations associated with products. It is used to facilitate the retrieval and
     * manipulation of product-related data from a persistent storage.
     */
    private ProductDAO productDAO;
    /**
     * Represents a MovieDAO instance used to interact with the movies database table.
     * Provides CRUD operations (Create, Read, Update, Delete) for managing movie data.
     * Used within the CashierPaymentController to retrieve or manipulate movie-related information.
     */
    private MovieDAO movieDAO;
    /**
     * Represents the currently logged-in cashier in the payment process.
     *
     * This variable is used to store the User object of the cashier who is managing
     * the current transaction. It contains information about the cashier, such as
     * their username, role, and personal details (e.g., first name and last name).
     *
     * By maintaining the context of the current cashier, this variable ensures that
     * the system can track and log which user performed the transaction for auditing
     * and operational purposes.
     */
    private User currentCashier;
    /**
     * Represents the total monetary amount currently associated with the transaction.
     *
     * This variable is used to store the cumulative total of all item prices and
     * any associated costs for the transaction in the application. It is updated
     * as items are added or modified in the user's cart and reflects the final
     * amount due for payment.
     *
     * The value is initialized to BigDecimal.ZERO to ensure a default starting
     * state and to prevent null-related issues during calculations.
     */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /**
     * Represents the observable list of order items to be displayed in the table view.
     * This list serves as the data source for the table that visualizes the order details
     * in the CashierPaymentController.
     *
     * Each element in the list is an instance of the OrderItemTable class, which encapsulates
     * the properties of an individual order item, such as name, quantity, price, and total.
     *
     * The ObservableList is managed to automatically notify the UI components of any
     * updates, such as additions, removals, or modifications of table items.
     */
    private ObservableList<OrderItemTable> tableItems = FXCollections.observableArrayList();

    /**
     * Initializes the CashierPaymentController by setting up necessary components and dependencies.
     * This method is automatically invoked during the loading of the JavaFX controller.
     *
     * Responsibilities:
     * - Initializes the shopping cart instance shared across the application.
     * - Instantiates Data Access Object (DAO) classes to interact with persistent storage for orders, users, products, and movies.
     * - Calls setupTable() to prepare and configure the table view for displaying order-related items during the cashier's transaction.
     * - Sets the current cashier reference to null as part of the initialization process.
     */
    @FXML
    private void initialize() {
        cart = ShoppingCart.getInstance();
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        movieDAO = new MovieDAO();
        setupTable();

        currentCashier = null;
    }

    /**
     * Configures the table view to display order item details, including item name, quantity,
     * unit price, and total price. The method sets cell value factories for columns to bind
     * data properties and applies styling and formatting rules.
     *
     * The item name, quantity, and total columns are aligned to the center, while the price
     * and total columns are formatted to display currency in the Turkish locale.
     *
     * Table data is populated using the `tableItems` observable list.
     *
     * Responsibilities include:
     * - Binding data properties from the `OrderItemTable` model to their respective table columns.
     * - Applying styles to align the text content for all columns.
     * - Formatting numeric values in the price and total columns as currency.
     */
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

    /**
     * Sets the CashierController instance and initializes related components.
     * This method is responsible for updating the current cashier, handling any errors
     * related to authentication, and loading the order details into the view.
     *
     * @param controller the CashierController instance to be set, managing cashier-specific
     * operations and user authentication details.
     */
    public void setCashierController(CashierController controller) {
        this.cashierController = controller;

        currentCashier = cashierController.getCurrentUser();

        if (currentCashier == null) {
            showError("System Error", "No authenticated cashier found.");
            processPaymentButton.setDisable(true);
        }

        loadOrderDetails();
    }

    /**
     * Loads order details and updates the relevant UI components for the cashier payment process.
     * This method retrieves the selected movie and session information from the
     * CashierController, calculates the total amount due including tax, and displays it
     * along with other relevant details such as seat information and customer details.
     * The order items are also loaded into the table for display.
     *
     * The method performs the following key tasks:
     * 1. Sets basic details such as movie title, session time, hall, and selected seats.
     * 2. Retrieves subtotal and tax amounts from the cart controller and calculates the total amount.
     * 3. Updates customer-related information.
     * 4. Populates the table with order items.
     */
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

        // Get totals from cart controller
        CashierCartController cartController = cashierController.getCartController();
        double subtotal = cartController.getSubtotal(); // Already includes discounts
        double tax = cartController.getTax();

        // Calculate total amount (subtotal + tax)
        totalAmount = BigDecimal.valueOf(subtotal + tax);
        amountDueLabel.setText(formatCurrency(totalAmount));

        // Set labels based on cart items
        setCustomerDetailsFromCart();

        // Load items into table
        loadTableItems();
    }

    /**
     * Refreshes and populates the `tableItems` list by processing the current items in the cart.
     * This method retrieves and calculates details for each item including name, price, quantity,
     * and total cost, and adds them to the `tableItems` collection. Additionally, a tax row is
     * appended to the collection.
     *
     * Functionality:
     * - Clears the existing `tableItems` list.
     * - Retrieves the active items and their details from the cart controller.
     * - Processes only visible cart items in the user interface (UI).
     * - Extracts item details including name, price, and quantity.
     * - Identifies if the item has a discount and uses the discounted price if available.
     * - Calculates the price and total cost for each item.
     * - Adds a tax row to `tableItems` using the tax value retrieved from the cart controller.
     *
     * Error handling:
     * - Catches and logs exceptions that occur during item processing or tax row addition.
     *
     * Precondition:
     * - The `cartController` must be properly initialized and contain the latest cart state.
     *
     * Postcondition:
     * - The `tableItems` list contains all updated cart items along with a tax row, ready for display or processing.
     */
    private void loadTableItems() {
        tableItems.clear();

        // Get items from the cart controller instead of directly from the cart
        CashierCartController cartController = cashierController.getCartController();
        Map<String, HBox> activeCartItems = cartController.getCartItems();
        VBox cartItemsContainer = cartController.getCartItemsContainer();

        // Create a Turkish currency formatter
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

        // Only process items that are currently in the cart UI
        for (Node node : cartItemsContainer.getChildren()) {
            if (node instanceof HBox) {
                try {
                    HBox itemContainer = (HBox) node;
                    VBox details = (VBox) itemContainer.getChildren().get(0);
                    Label nameLabel = (Label) details.getChildren().get(0);
                    Label priceLabel = (Label) details.getChildren().get(1);
                    Label quantityLabel = (Label) itemContainer.getChildren().get(1);

                    // Extract item details
                    String itemName = nameLabel.getText();

                    // Check if the item is a ticket and has a discount label
                    double price;
                    if (itemName.startsWith("Seat") && details.getChildren().size() > 2) {
                        // Extract discounted price
                        Label discountLabel = (Label) details.getChildren().get(2);
                        String discountedPriceText = discountLabel.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
                        price = Double.parseDouble(discountedPriceText);
                    } else {
                        // Extract regular price using currency formatter
                        Number parsedPrice = formatter.parse(priceLabel.getText());
                        price = parsedPrice.doubleValue();
                    }

                    // Extract quantity (remove 'x' prefix)
                    int quantity = Integer.parseInt(quantityLabel.getText().substring(1));

                    // Calculate total
                    double total = price * quantity;

                    tableItems.add(new OrderItemTable(itemName, quantity, price, total));
                } catch (Exception e) {
                    System.err.println("Error processing cart item: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Add tax row
        try {
            double taxAmount = cashierController.getCartController().getTax();
            tableItems.add(new OrderItemTable("Tax", 1, taxAmount, taxAmount));
        } catch (Exception e) {
            System.err.println("Error adding tax row: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Populates customer-related details from the cart and updates the respective labels
     * on the UI to reflect the occupant's name and discount status.
     *
     * This method iterates through the items in the shopping cart, searching for an item
     * of type "ticket". If such an item is found, the occupant's first and last names
     * are combined and displayed on the customer name label. Additionally, the method
     * checks whether an age-related discount has been applied to the ticket and updates
     * the discount status label accordingly.
     *
     * The customer name and discount labels are only updated if a "ticket" item exists
     * in the cart.
     */
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

    /**
     * Handles the process of confirming and executing a payment transaction.
     *
     * This method is triggered when the user initiates a payment action. It displays a
     * confirmation dialog to the user, prompting them to confirm the payment. If the user
     * confirms the action, the payment is processed using the {@code processPayment} method.
     *
     * The method ensures the payment workflow is performed only after user confirmation,
     * enhancing the transaction security and preventing unintended actions.
     *
     * Steps included in this method:
     * 1. Display a confirmation dialog using {@code showConfirmationDialog}.
     * 2. If the user confirms (by clicking OK), invoke the {@code processPayment} method
     *    to handle the payment logic.
     *
     * This function relies on helper methods {@code showConfirmationDialog} and
     * {@code processPayment} to perform its tasks. These methods are responsible for
     * user interaction and backend processing, respectively.
     */
    @FXML
    private void handleProcessPayment() {
        Optional<ButtonType> result = showConfirmationDialog();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processPayment();
        }
    }

    /**
     * Displays a confirmation dialog to the user for processing a payment.
     * The dialog contains a title, a header, and a content message asking
     * the user to confirm their action. The dialog supports waiting for the
     * user's response.
     *
     * @return an Optional containing the user's response as a ButtonType,
     *         which might be empty if no response is provided
     */
    private Optional<ButtonType> showConfirmationDialog() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Process Payment");
        confirm.setContentText("Are you sure you want to process this payment?");
        return confirm.showAndWait();
    }

    /**
     * Processes the payment for the current transaction.
     *
     * This method validates that an authenticated cashier is available.
     * It creates an order from the shopping cart and assigns the current cashier's
     * user ID to the order. The order is then submitted to the data access object
     * (DAO) for persistence. If the order is successfully created, the method
     * generates tickets and a receipt, displays a success dialog, and resets
     * the transaction state. If the order creation fails, an error dialog is displayed
     * to notify the cashier of the failure.
     *
     * Error scenarios:
     * - If there is no authenticated cashier (`currentCashier` is null), the method
     *   will display a "System Error" alert and stop further processing.
     * - If the order could not be persisted (DAO failure), a "Payment Failed" alert
     *   will be displayed.
     */
    private void processPayment() {
        if (currentCashier == null) {
            showError("System Error", "No authenticated cashier found.");
            return;
        }

        Order order = cart.createOrder();
        order.setCashierId(currentCashier.getUserId());

        if (orderDAO.createOrder(order)) {
            generateTicketsAndReceipt(order);
            showSuccessDialog(order.getOrderId());
            resetTransaction();
        } else {
            showError("Payment Failed",
                    "Failed to process payment. Please try again.");
        }
    }

    /**
     * Generates a PDF receipt for the given order.
     * The receipt includes details such as cashier information, transaction date, order items,
     * and totals (subtotal, tax, and total). It is formatted using a font that supports Turkish characters.
     *
     * @param order the {@code Order} object containing details of the transaction such as items purchased,
     *              customer information, cashier, and order date.
     * @return a byte array representing the generated PDF receipt. Returns {@code null} if an error occurs during PDF generation.
     */
    private byte[] generateReceiptPDF(Order order) {
        try {
            // Use a font that supports Turkish characters
            BaseFont turkishFont = BaseFont.createFont(
                    "src/main/resources/fonts/arial-unicode.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            Font titleFont = new Font(turkishFont, 18, Font.BOLD);
            Font headerFont = new Font(turkishFont, 12, Font.BOLD);
            Font normalFont = new Font(turkishFont, 10, Font.NORMAL);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 50, 50, 50, 50);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();

            // Cinema Header
            Paragraph cinemaHeader = new Paragraph("Group 18 Cinema Center", titleFont);
            cinemaHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(cinemaHeader);

            // Subtitle
            Paragraph receiptTitle = new Paragraph("Ticket and Sales Receipt", new Font(turkishFont, 14, Font.BOLD));
            receiptTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(receiptTitle);

            // Horizontal Line
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);

            // Transaction Details
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10f);

            // Cashier Information
            User cashier = userDAO.findById(order.getCashierId());
            addTableRow(detailsTable, "Cashier:",
                    cashier.getFirstName() + " " + cashier.getLastName(),
                    turkishFont);

            // Order Date
            addTableRow(detailsTable, "Date:",
                    order.getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                    turkishFont);

            // Customer Details (from ticket)
            OrderItem firstTicketItem = order.getOrderItems().stream()
                    .filter(item -> "ticket".equals(item.getItemType()))
                    .findFirst()
                    .orElse(null);

            if (firstTicketItem != null) {
                addTableRow(detailsTable, "Customer Name:",
                        firstTicketItem.getOccupantFirstName() + " " +
                                firstTicketItem.getOccupantLastName(),
                        turkishFont);

                addTableRow(detailsTable, "Discount:",
                        firstTicketItem.getDiscountApplied() ? "Applied" : "Not Applicable",
                        turkishFont);
            }

            document.add(detailsTable);
            document.add(Chunk.NEWLINE);

            // Order Items Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3, 1, 1, 1});

            // Table Header
            String[] headers = {"Item", "Quantity", "Price", "Total"};
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                itemsTable.addCell(headerCell);
            }

            // Populate table with order items
            double subtotal = 0;
            for (OrderItem item : order.getOrderItems()) {
                String itemName = getItemName(item);
                int quantity = item.getQuantity();
                double itemPrice = item.getItemPrice().doubleValue();
                double total = itemPrice * quantity;

                // Item Name
                PdfPCell nameCell = new PdfPCell(new Phrase(itemName, normalFont));
                nameCell.setPadding(5);
                itemsTable.addCell(nameCell);

                // Quantity
                PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(quantity), normalFont));
                quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                quantityCell.setPadding(5);
                itemsTable.addCell(quantityCell);

                // Price
                PdfPCell priceCell = new PdfPCell(new Phrase(currencyFormatter.format(itemPrice), normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                priceCell.setPadding(5);
                itemsTable.addCell(priceCell);

                // Total
                PdfPCell totalCell = new PdfPCell(new Phrase(currencyFormatter.format(total), normalFont));
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalCell.setPadding(5);
                itemsTable.addCell(totalCell);

                subtotal += total;
            }

            document.add(itemsTable);
            document.add(Chunk.NEWLINE);

            // Totals Section
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Subtotal
            addTotalRow(totalsTable, "Subtotal:", currencyFormatter.format(subtotal), turkishFont);

            // Tax
            double tax = cashierController.getCartController().getTax();
            addTotalRow(totalsTable, "Tax:", currencyFormatter.format(tax), turkishFont);

            // Total
            double total = subtotal + tax;
            addTotalRow(totalsTable, "Total:", currencyFormatter.format(total), turkishFont);

            document.add(totalsTable);

            // Footer
            Paragraph footer = new Paragraph("\n\nThank you for your purchase!", new Font(turkishFont, 10, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a row with a label and a corresponding value to the provided PDF table.
     *
     * @param table the PdfPTable to which the row will be added
     * @param label the text to display in the label cell
     * @param value the text to display in the value cell
     * @param font  the font to be used for styling the text in the cells
     */
    private void addTableRow(PdfPTable table, String label, String value, BaseFont font) {
        Font labelFont = new Font(font, 10, Font.BOLD);
        Font valueFont = new Font(font, 10, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    /**
     * Adds a row to a PDF table displaying a total amount with a label and value.
     * This method uses custom fonts for label and value styling and aligns
     * the content to the right.
     *
     * @param table the PdfPTable to which the row will be added
     * @param label the label describing the total (e.g., "Subtotal", "Tax", "Total")
     * @param value the total value corresponding to the label
     * @param font the BaseFont used to style the label and value text
     */
    private void addTotalRow(PdfPTable table, String label, String value, BaseFont font) {
        Font labelFont = new Font(font, 10, Font.BOLD);
        Font valueFont = new Font(font, 10, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    /**
     * Retrieves the name or description of an item based on its type and related details.
     *
     * @param item the object representing the order item, containing details such as item type, schedule ID, seat number, or product ID
     * @return the name or description of the item, such as the movie ticket description or the product name; returns "Unknown Item" if the type is unrecognized
     */
    private String getItemName(OrderItem item) {
        if ("ticket".equals(item.getItemType())) {
            // Use MovieDAO to fetch movie details
            Movie movie = movieDAO.findMovieByScheduleId(item.getScheduleId());
            return movie != null
                    ? "Ticket: " + movie.getTitle() + " (Seat: " + convertNumberToSeatId(item.getSeatNumber()) + ")"
                    : "Ticket: Unknown Movie (Seat: " + convertNumberToSeatId(item.getSeatNumber()) + ")";
        } else if ("product".equals(item.getItemType())) {
            Product product = productDAO.findById(item.getProductId());
            return product != null ? product.getProductName() : "Unknown Product";
        }
        return "Unknown Item";
    }

    /**
     * Converts a numeric seat number into a seat ID string representation based
     * on the layout of the cinema hall. The format of the seat ID is a combination
     * of a row letter (e.g., 'A', 'B') and a column number (e.g., '1', '2').
     * The specific hall layout determines the number of columns in the hall.
     *
     * @param seatNumber the numeric seat number to be converted, where the first seat
     *                   starts from 1 and is incremented sequentially.
     * @return the string representation of the seat ID, combining the row letter
     *         and column number (e.g., "A1", "B3").
     */
    private String convertNumberToSeatId(int seatNumber) {
        MovieSession session = cashierController.getSelectedSession();
        int cols = session.getHall().equals("Hall_A") ? 4 : 8;
        int row = (seatNumber - 1) / cols;
        int col = ((seatNumber - 1) % cols) + 1;
        return String.format("%c%d", (char)('A' + row), col);
    }

    /**
     * Generates the receipt and tickets for the given order and stores them in the database.
     *
     * @param order the order for which receipt and tickets are to be generated and stored
     */
    private void generateTicketsAndReceipt(Order order) {
        byte[] receiptPdf = generateReceiptPDF(order);
        byte[] ticketsPdf = generateTicketsPDF(order);

        if (orderDAO.storeDocuments(order.getOrderId(), receiptPdf, ticketsPdf)) {
            System.out.println("Receipt and tickets stored successfully");
        } else {
            System.err.println("Failed to store receipt and tickets");
        }
    }

    /**
     * Generates a PDF document for the tickets associated with the given order.
     * The PDF includes details such as movie information, session details,
     * seat numbers, pricing, and other relevant information in a format suitable
     * for printing or digital distribution.
     *
     * @param order the {@code Order} object containing the details of
     *              the tickets and products to be included in the PDF
     * @return a byte array representing the generated PDF document,
     *         or {@code null} if an error occurs during generation
     */
    private byte[] generateTicketsPDF(Order order) {
        try {
            // Use a font that supports Turkish characters
            BaseFont turkishFont = BaseFont.createFont(
                    "src/main/resources/fonts/arial-unicode.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            Font titleFont = new Font(turkishFont, 24, Font.BOLD);
            Font headerFont = new Font(turkishFont, 16, Font.BOLD);
            Font normalFont = new Font(turkishFont, 12, Font.NORMAL);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 50, 50, 50, 50);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Get ticket items only
            List<OrderItem> ticketItems = order.getOrderItems().stream()
                    .filter(item -> "ticket".equals(item.getItemType()))
                    .toList();

            // Get product items
            List<OrderItem> productItems = order.getOrderItems().stream()
                    .filter(item -> "product".equals(item.getItemType()))
                    .toList();

            // Get movie details
            Movie movie = movieDAO.findMovieByScheduleId(ticketItems.get(0).getScheduleId());
            if (movie == null) return null;

            // Cinema Header
            Paragraph cinemaHeader = new Paragraph("Group 18 Cinema Center", titleFont);
            cinemaHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(cinemaHeader);

            // Movie Title
            Paragraph movieTitle = new Paragraph(movie.getTitle(), headerFont);
            movieTitle.setAlignment(Element.ALIGN_CENTER);
            movieTitle.setSpacingBefore(20);
            document.add(movieTitle);

            // Horizontal Line
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);

            // Create table for ticket details
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(20f);

            // Add customer details
            OrderItem firstTicketItem = ticketItems.get(0);
            addTableRow(detailsTable, "Customer:",
                    firstTicketItem.getOccupantFirstName() + " " + firstTicketItem.getOccupantLastName(),
                    turkishFont);

            // Add session details
            MovieSession session = cashierController.getSelectedSession();
            addTableRow(detailsTable, "Date:",
                    cashierController.getSelectedDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    turkishFont);
            addTableRow(detailsTable, "Time:",
                    session.getTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    turkishFont);
            addTableRow(detailsTable, "Hall:", session.getHall(), turkishFont);

            // Collect and format seat numbers
            String seats = ticketItems.stream()
                    .map(item -> convertNumberToSeatId(item.getSeatNumber()))
                    .sorted()
                    .collect(Collectors.joining(", "));
            addTableRow(detailsTable, "Seats:", seats, turkishFont);

            // Calculate prices
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

            // Ticket Price
            double totalTicketPrice = ticketItems.stream()
                    .mapToDouble(item -> item.getItemPrice().doubleValue())
                    .sum();
            addTableRow(detailsTable, "Ticket Price:",
                    currencyFormatter.format(totalTicketPrice),
                    turkishFont);

            // Product Price
            double totalProductPrice = productItems.stream()
                    .mapToDouble(item -> item.getItemPrice().doubleValue() * item.getQuantity())
                    .sum();
            addTableRow(detailsTable, "Products Price:",
                    currencyFormatter.format(totalProductPrice),
                    turkishFont);

            // Tax Calculation
            double totalTax = cashierController.getCartController().getTax();
            addTableRow(detailsTable, "Tax:",
                    currencyFormatter.format(totalTax),
                    turkishFont);

            // Total Price (including tax)
            double totalPrice = totalTicketPrice + totalProductPrice + totalTax;
            addTableRow(detailsTable, "Total Price:",
                    currencyFormatter.format(totalPrice),
                    turkishFont);

            // Check if any ticket has a discount
            boolean hasDiscount = ticketItems.stream()
                    .anyMatch(OrderItem::getDiscountApplied);
            if (hasDiscount) {
                addTableRow(detailsTable, "Discount:", "Age-based discount applied", turkishFont);
            }

            document.add(detailsTable);

            // Add footer with terms and conditions
            Paragraph footer = new Paragraph(
                    "\n\nThis ticket is valid only for the specified date and time." +
                            "\nFor cancellations please contact us from our website." +
                            "\nPlease arrive at least 15 minutes before showtime.",
                    new Font(turkishFont, 8, Font.ITALIC)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Displays a dialog to view the tickets associated with a specific order ID.
     * This method retrieves the ticket PDF for the given order, temporarily saves it to a file,
     * and opens it using the system's default PDF viewer. If no tickets are found or an error occurs,
     * appropriate error dialogs are displayed.
     *
     * @param orderId the ID of the order whose tickets need to be displayed
     */
    private void showTicketsDialog(int orderId) {
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
     * Displays a receipt dialog for a specific order by loading the associated receipt PDF.
     * If the receipt is found, it is displayed in the system's default PDF viewer.
     * If an error occurs or the receipt is not found, an alert is shown to the user.
     *
     * @param orderId The unique identifier of the order for which the receipt will be displayed.
     */
    private void showReceiptDialog(int orderId) {
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
     * Displays a success dialog after the payment is processed successfully.
     * The dialog includes options to view the receipt, view the tickets,
     * or acknowledge the success via an OK button. Buttons for viewing the
     * receipt and tickets prevent the dialog from closing, allowing users
     * to interact with them without dismissing the dialog.
     *
     * @param orderId the unique identifier of the order associated with
     *                the payment. This ID is used to retrieve the receipt
     *                and tickets for the order.
     */
    private void showSuccessDialog(int orderId) {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Payment Successful");
        success.setHeaderText(null);
        success.setContentText("Payment has been processed successfully. " +
                "Tickets and receipt have been generated.");

        // Create custom buttons
        ButtonType showReceiptButton = new ButtonType("Show Receipt", ButtonBar.ButtonData.LEFT);
        ButtonType showTicketsButton = new ButtonType("Show Tickets", ButtonBar.ButtonData.LEFT);
        ButtonType okButton = ButtonType.OK;

        // Set the buttons with a specific order
        success.getButtonTypes().clear();
        success.getButtonTypes().addAll(showReceiptButton, showTicketsButton, okButton);

        // Customize buttons to prevent closing
        success.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Button button = (Button) success.getDialogPane().lookupButton(buttonType);
            button.addEventFilter(ActionEvent.ACTION, event -> {
                if (buttonType == showReceiptButton) {
                    showReceiptDialog(orderId);
                    event.consume(); // Prevent dialog from closing
                } else if (buttonType == showTicketsButton) {
                    showTicketsDialog(orderId);
                    event.consume(); // Prevent dialog from closing
                }
            });
        });

        // Show the dialog and handle the final result
        Optional<ButtonType> result = success.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            // Reset transaction and return to movie selection
            if (cashierController != null) {
                cashierController.resetTransaction();
            }
        }
    }

    /**
     * Resets the current transaction by clearing the shopping cart and resetting the
     * associated cashier controller's transaction state.
     *
     * This method performs the following operations:
     * 1. Logs the current cart size before clearing its contents.
     * 2. Clears all items from the cart.
     * 3. Logs the updated cart size after it has been cleared.
     * 4. Invokes the resetTransaction method of the cashier controller to reset the
     *    transaction state and interface.
     *
     * Use this method to ensure all transaction data is properly cleared and ready
     * for a new transaction to be started.
     */
    private void resetTransaction() {
        System.out.println("Resetting transaction. Cart size before clear: " + cart.getItems().size());
        cart.clear();
        System.out.println("Cart size after clear: " + cart.getItems().size());
        cashierController.resetTransaction();
    }

    /**
     * Formats a given monetary amount into a currency string representation
     * based on the locale specifications for Turkey (tr-TR).
     *
     * @param amount the monetary amount to be formatted, represented as a BigDecimal
     * @return the formatted currency string, including currency symbol and appropriate formatting
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
        return formatter.format(amount);
    }

    /**
     * Displays an error alert dialog to the user with the provided title and content.
     * This method is used to show error messages in a uniform and user-friendly manner.
     *
     * @param title the title of the error dialog, which provides a summary of the issue
     * @param content the content of the error dialog, which describes the error in detail
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}