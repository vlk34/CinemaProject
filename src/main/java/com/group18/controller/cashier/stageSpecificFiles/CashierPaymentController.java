// CashierPaymentController.java
package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.MovieDAO;
import com.group18.dao.OrderDAO;
import com.group18.dao.ProductDAO;
import com.group18.dao.UserDAO;
import com.group18.model.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.text.Document;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import javafx.stage.FileChooser;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

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
    private ProductDAO productDAO;
    private MovieDAO movieDAO;
    private User currentCashier;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private ObservableList<OrderItemTable> tableItems = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        cart = ShoppingCart.getInstance();
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        movieDAO = new MovieDAO();
        setupAmountReceivedValidation();
        processPaymentButton.setDisable(true);
        setupTable();

        currentCashier = null;
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

        currentCashier = cashierController.getCurrentUser();

        if (currentCashier == null) {
            showError("System Error", "No authenticated cashier found.");
            processPaymentButton.setDisable(true);
        }

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
            showSuccessDialog(order.getOrderId());
            resetTransaction();
        } else {
            showError("Payment Failed",
                    "Failed to process payment. Please try again.");
        }
    }

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
            double tax = order.getTotalPrice().doubleValue() - subtotal;
            addTotalRow(totalsTable, "Tax:", currencyFormatter.format(tax), turkishFont);

            // Total
            addTotalRow(totalsTable, "Total:", currencyFormatter.format(order.getTotalPrice().doubleValue()), turkishFont);

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

    // Helper method to add a row to a details table
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

    // Helper method to add a row to the totals table
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

    // Convert seat number back to seat ID (A1, B2, etc.)
    private String convertNumberToSeatId(int seatNumber) {
        MovieSession session = cashierController.getSelectedSession();
        int cols = session.getHall().equals("Hall_A") ? 4 : 8;
        int row = (seatNumber - 1) / cols;
        int col = ((seatNumber - 1) % cols) + 1;
        return String.format("%c%d", (char)('A' + row), col);
    }

    private void generateTicketsAndReceipt(Order order) {
        // Generate PDF
        byte[] receiptPdf = generateReceiptPDF(order);

        if (receiptPdf != null) {
            // Store PDF in database
            if (orderDAO.storeReceipt(order.getOrderId(), receiptPdf)) {
                System.out.println("Receipt PDF stored successfully");
            } else {
                System.err.println("Failed to store receipt PDF");
            }
        }
    }

    // Add a new method to show the receipt after payment
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

    private void showSuccessDialog(int orderId) {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Payment Successful");
        success.setHeaderText(null);
        success.setContentText("Payment has been processed successfully. " +
                "Tickets and receipt have been generated.");

        // Create custom buttons
        ButtonType showReceiptButton = new ButtonType("Show Receipt", ButtonBar.ButtonData.LEFT);
        ButtonType okButton = ButtonType.OK;

        // Set the buttons with a specific order
        success.getButtonTypes().clear();
        success.getButtonTypes().addAll(showReceiptButton, okButton);

        Optional<ButtonType> result = success.showAndWait();

        if (result.isPresent() && result.get() == showReceiptButton) {
            showReceiptDialog(orderId);
        }
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