package com.group18.controller.manager;

import com.group18.dao.OrderDAO;
import com.group18.model.Order;
import com.group18.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ManagerRevenueController class is responsible for managing and displaying
 * revenue-related data for a manager. It provides features to calculate, update,
 * and display statistics about revenue, tickets, and products for the current
 * and previous months.
 *
 * The class interacts with the graphical components of the application's UI
 * through the use of JavaFX annotations. It uses revenue data from orders and
 * calculates various statistics, including total revenue, VAT amounts, and
 * trends between months. The results are displayed in corresponding UI elements.
 *
 * This class also updates a revenue table to display detailed information about
 * daily revenue, categorized by type (ticket or product).
 *
 * Static constants are defined for calculating VAT rates for tickets and products.
 *
 * It uses the OrderDAO class to retrieve orders and calculate revenue statistics
 * using helper methods and encapsulated logic.
 */
public class ManagerRevenueController {

    /**
     * The label used to display the total revenue in the manager's revenue view.
     * This label is updated with the aggregated revenue data from processed orders.
     * It is typically formatted as a monetary value.
     */
    @FXML
    private Label totalRevenueLabel;
    /**
     * A JavaFX Label used to display the total number of tickets for a given
     * revenue management view within the ManagerRevenueController.
     * This label dynamically updates to represent aggregated ticket data
     * based on loaded orders and calculated statistics.
     */
    @FXML
    private Label totalTicketsLabel;
    /**
     * Label element used for displaying the total number of products sold
     * within the user interface of the ManagerRevenueController.
     * This component is updated dynamically to reflect real-time data
     * retrieved from processed orders or revenue calculations.
     */
    @FXML
    private Label totalProductsLabel;
    /**
     * Represents a label in the UI for displaying the total tax amount.
     * This label is used as part of the revenue management interface
     * to show the calculated tax associated with revenues such as ticket and product sales.
     */
    @FXML
    private Label taxAmountLabel;
    /**
     * The {@code revenueChangeLabel} is a UI component represented by a {@link Label}.
     * It is used to display the percentage or absolute change in revenue over a specific period.
     * This label is updated dynamically within the application based on revenue data.
     *
     * This field is part of the {@code ManagerRevenueController} class, which manages the
     * graphical user interface for displaying revenue statistics and related information.
     */
    @FXML
    private Label revenueChangeLabel;
    /**
     * A label UI element used to display the change in ticket sales over a specified period.
     * This label is updated dynamically to reflect the percentage or numerical change
     * in ticket sales as part of the revenue statistics.
     *
     * It is part of the ManagerRevenueController and is associated with the FXML-defined UI.
     */
    @FXML
    private Label ticketsChangeLabel;
    /**
     * Represents a label in the UI that displays the change in the number
     * of products between different revenue statistics. This label is
     * updated dynamically based on the comparison of current and previous data.
     */
    @FXML
    private Label productsChangeLabel;

    /**
     * A TableView instance used to display revenue entries in a structured format.
     * Each row in this table represents a single {@link RevenueEntry}, which includes
     * details such as the date, type, and amount of revenue.
     *
     * This field is managed by JavaFX's FXML loader and is initialized automatically
     * when the associated FXML file is loaded. It is used within the
     * {@code ManagerRevenueController} to present financial data related to orders
     * and revenue statistics.
     */
    @FXML
    private TableView<RevenueEntry> revenueTable;
    /**
     * The TableColumn instance responsible for displaying the date field
     * of a RevenueEntry object in the revenue table.
     *
     * This column maps to the "date" property of RevenueEntry and is used
     * to show the date associated with each revenue entry.
     *
     * It is part of the revenue table that provides an overview of revenue-related
     * data such as date, type, and amount.
     */
    @FXML
    private TableColumn<RevenueEntry, String> dateColumn;
    /**
     * Represents the "type" column in the revenue table, which displays the type of revenue entry.
     * Binds to the `type` property of {@link RevenueEntry}.
     * This column is part of the revenue table managed in the {@code ManagerRevenueController}.
     */
    @FXML
    private TableColumn<RevenueEntry, String> typeColumn;
    /**
     * Represents the table column in the revenue table that displays
     * the amount of revenue as a BigDecimal value.
     * This column is part of the table representing revenue entries.
     */
    @FXML
    private TableColumn<RevenueEntry, BigDecimal> amountColumn;

    /**
     * Manages database operations related to orders within the context of the revenue management system.
     * This variable is primarily used to interact with the database to fetch, update, or manipulate order-related data,
     * supporting the functionality of the ManagerRevenueController class.
     */
    private OrderDAO orderDAO;
    /**
     * The value-added tax (VAT) rate applied to ticket purchases.
     * This constant represents a standard VAT rate of 20%.
     * It is used in calculations related to revenue generated from tickets in the system.
     */
    private static final BigDecimal TICKET_VAT_RATE = new BigDecimal("0.20");
    /**
     * The VAT (Value Added Tax) rate applied to product sales.
     * This constant represents the tax rate as a BigDecimal for accurate financial calculations.
     * It is used for computing the tax amount in product-related transactions.
     */
    private static final BigDecimal PRODUCT_VAT_RATE = new BigDecimal("0.10");

    /**
     * Initializes the ManagerRevenueController by setting up the necessary table configurations
     * and loading data into the view. This method is automatically invoked by the JavaFX framework
     * when the associated FXML file is loaded.
     *
     * Responsibilities:
     * - Instantiates the OrderDAO object to access order data.
     * - Configures the revenue table structure by calling the setupTable method.
     * - Populates the data into the table and performs initial calculations by calling the loadData method.
     */
    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();
        setupTable();
        loadData();
    }

    /**
     * Configures the columns in the revenue table to display data from the
     * corresponding properties of the {@link RevenueEntry} objects.
     *
     * This method sets up the cell value factories for the `dateColumn`, `typeColumn`,
     * and `amountColumn`, mapping them to the "date", "type", and "amount" properties
     * of the {@link RevenueEntry} class respectively. Each column will extract and
     * display the matching property value from the revenue entry data.
     */
    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    /**
     * Loads the data required to display current and previous month's revenue statistics.
     * This method retrieves and processes order data for the current and previous months,
     * calculates revenue-related statistics, and updates the respective UI components.
     *
     * The operations performed include:
     * 1. Determining the date range for the current and previous months.
     * 2. Fetching orders within these date ranges using the `orderDAO`.
     * 3. Calculating revenue statistics for both the current and previous months using
     *    the `calculateStatistics` method.
     * 4. Updating the UI components with the calculated statistics and order details
     *    using `updateStatistics` and `updateRevenueTable` methods.
     *
     * This method plays a central role in preparing data to monitor and compare
     * revenue performance for the current and previous months.
     */
    private void loadData() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

        // Get current month's orders
        List<Order> currentMonthOrders = orderDAO.getOrdersByDateRange(startOfMonth, endOfMonth);

        // Get previous month's orders
        LocalDateTime startOfPrevMonth = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime endOfPrevMonth = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(23, 59, 59);
        List<Order> prevMonthOrders = orderDAO.getOrdersByDateRange(startOfPrevMonth, endOfPrevMonth);

        // Calculate statistics
        RevenueStatistics currentStats = calculateStatistics(currentMonthOrders);
        RevenueStatistics prevStats = calculateStatistics(prevMonthOrders);

        // Update UI
        updateStatistics(currentStats, prevStats);
        updateRevenueTable(currentMonthOrders);
    }

    /**
     * Calculates revenue statistics including ticket and product revenue, VAT, and quantities
     * based on the provided list of orders. Orders with certain statuses or items with specific
     * types are conditionally processed or excluded in the calculation.
     *
     * @param orders the list of orders from which revenue statistics are calculated.
     *               Each order contains a status and multiple order items of different types.
     * @return a RevenueStatistics instance containing the computed revenue, VAT, and item counts
     *         for both tickets and products, as well as the overall totals.
     */
    private RevenueStatistics calculateStatistics(List<Order> orders) {
        RevenueStatistics stats = new RevenueStatistics();

        for (Order order : orders) {
            if (!"PROCESSED_FULL".equals(order.getStatus())) {
                for (OrderItem item : order.getOrderItems()) {
                    if (("PROCESSED_TICKETS".equals(order.getStatus()) && "ticket".equals(item.getItemType())) ||
                            ("PROCESSED_PRODUCTS".equals(order.getStatus()) && "product".equals(item.getItemType()))) {
                        continue; // Skip cancelled ticket or product items
                    }

                    BigDecimal itemTotalPrice = item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                    if ("ticket".equals(item.getItemType())) {
                        stats.ticketRevenue = stats.ticketRevenue.add(itemTotalPrice);
                        stats.ticketCount += item.getQuantity();
                        stats.ticketVAT = stats.ticketVAT.add(
                                itemTotalPrice.multiply(TICKET_VAT_RATE)
                        );
                    } else {
                        stats.productRevenue = stats.productRevenue.add(itemTotalPrice);
                        stats.productCount += item.getQuantity();
                        stats.productVAT = stats.productVAT.add(
                                itemTotalPrice.multiply(PRODUCT_VAT_RATE)
                        );
                    }
                }
            }
        }

        stats.totalRevenue = stats.ticketRevenue.add(stats.productRevenue);
        stats.totalVAT = stats.ticketVAT.add(stats.productVAT);

        return stats;
    }

    /**
     * Updates the statistics displayed in the UI, including revenue, ticket count, product count,
     * and their respective percentage changes compared to previous statistics.
     *
     * @param current  the current revenue statistics to be displayed
     * @param previous the previous revenue statistics used for calculating percentage changes
     */
    private void updateStatistics(RevenueStatistics current, RevenueStatistics previous) {
        totalRevenueLabel.setText(String.format("₺%.2f", current.totalRevenue));
        totalTicketsLabel.setText(String.valueOf(current.ticketCount));
        totalProductsLabel.setText(String.valueOf(current.productCount));
        taxAmountLabel.setText(String.format("₺%.2f", current.totalVAT));

        double revenueChange = calculatePercentageChange(previous.totalRevenue, current.totalRevenue);
        double ticketsChange = calculatePercentageChange(
                BigDecimal.valueOf(previous.ticketCount),
                BigDecimal.valueOf(current.ticketCount)
        );
        double productsChange = calculatePercentageChange(
                BigDecimal.valueOf(previous.productCount),
                BigDecimal.valueOf(current.productCount)
        );

        revenueChangeLabel.setText(String.format("%+.1f%% from last month", revenueChange));
        ticketsChangeLabel.setText(String.format("%+.1f%% from last month", ticketsChange));
        productsChangeLabel.setText(String.format("%+.1f%% from last month", productsChange));
    }

    /**
     * Calculates the percentage change between a previous value and a current value.
     * If the previous value is zero, the method returns 100.0 to avoid division by zero.
     *
     * @param previous the previous value as a BigDecimal
     * @param current the current value as a BigDecimal
     * @return the percentage change from the previous value to the current value as a double
     */
    private double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous.equals(BigDecimal.ZERO)) {
            return 100.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * Updates the revenue table with data derived from the provided list of orders.
     * The method processes each order, calculates revenue for different item types,
     * and updates the display of revenue entries in the revenue table.
     *
     * @param orders a list of {@link Order} objects that represent customer orders.
     *               Each order is examined to determine the revenue contributions
     *               of its items based on their types and the status of the order.
     */
    private void updateRevenueTable(List<Order> orders) {
        ObservableList<RevenueEntry> entries = FXCollections.observableArrayList();

        for (Order order : orders) {
            if (!"PROCESSED_FULL".equals(order.getStatus())) {
                String date = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                Map<String, BigDecimal> typeRevenue = order.getOrderItems().stream()
                        .filter(item -> !(order.getStatus().equals("PROCESSED_TICKETS") && item.getItemType().equals("ticket")) &&
                                !(order.getStatus().equals("PROCESSED_PRODUCTS") && item.getItemType().equals("product")))
                        .collect(Collectors.groupingBy(
                                OrderItem::getItemType,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                        BigDecimal::add
                                )
                        ));

                typeRevenue.forEach((type, amount) -> {
                    int totalQuantity = order.getOrderItems().stream()
                            .filter(item -> item.getItemType().equals(type))
                            .mapToInt(OrderItem::getQuantity)
                            .sum();

                    entries.add(new RevenueEntry(
                            date,
                            String.format("%s (x%d)", type, totalQuantity),
                            amount
                    ));
                });
            }
        }

        revenueTable.setItems(entries);
    }

    /**
     * The RevenueStatistics class is a data structure designed to hold revenue-related information
     * for ticket and product sales, along with their respective VAT amounts and item counts.
     * It provides a way to encapsulate and manage computed statistics for a given set of orders
     * including their totals.
     *
     * Fields include:
     * - Ticket revenue, VAT, and item count.
     * - Product revenue, VAT, and item count.
     * - Total revenue and VAT.
     *
     * This class is primarily used within the context of calculating and storing revenue
     * data for current and previous time periods, enabling performance analysis and comparison
     * of sales data within the application.
     *
     * Responsibilities:
     * - Store computed monetary values for ticket and product revenues.
     * - Store computed VAT values for tickets and products.
     * - Maintain counts for ticket and product sales.
     * - Track total revenue and VAT combining tickets and products.
     *
     * Instances of this class are typically returned by methods that process order collections
     * and perform aggregated calculations to reflect the overall financial metrics of the business.
     */
    // Helper classes
    private static class RevenueStatistics {
        BigDecimal ticketRevenue = BigDecimal.ZERO;
        BigDecimal productRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal ticketVAT = BigDecimal.ZERO;
        BigDecimal productVAT = BigDecimal.ZERO;
        BigDecimal totalVAT = BigDecimal.ZERO;
        int ticketCount = 0;
        int productCount = 0;
    }

    /**
     * Represents an entry in the revenue table, encapsulating the date, type, and amount
     * of a specific revenue entry. Each instance corresponds to a unique line item
     * expressed in the revenue display.
     *
     * Responsibilities:
     * - Holds the date associated with the revenue source.
     * - Specifies the type of the revenue, such as tickets or products, potentially
     *   including additional context (e.g., quantity).
     * - Stores the monetary value (amount) of the revenue for a particular entry.
     *
     * This class is immutable, ensuring thread safety and consistent data representation.
     */
    public static class RevenueEntry {
        private final String date;
        private final String type;
        private final BigDecimal amount;

        public RevenueEntry(String date, String type, BigDecimal amount) {
            this.date = date;
            this.type = type;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public BigDecimal getAmount() { return amount; }
    }
}