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

public class ManagerRevenueController {

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalTicketsLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label taxAmountLabel;
    @FXML
    private Label revenueChangeLabel;
    @FXML
    private Label ticketsChangeLabel;
    @FXML
    private Label productsChangeLabel;

    @FXML
    private TableView<RevenueEntry> revenueTable;
    @FXML
    private TableColumn<RevenueEntry, String> dateColumn;
    @FXML
    private TableColumn<RevenueEntry, String> typeColumn;
    @FXML
    private TableColumn<RevenueEntry, BigDecimal> amountColumn;

    private OrderDAO orderDAO;
    private static final BigDecimal TICKET_VAT_RATE = new BigDecimal("0.20");
    private static final BigDecimal PRODUCT_VAT_RATE = new BigDecimal("0.10");

    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();
        setupTable();
        loadData();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

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

    private RevenueStatistics calculateStatistics(List<Order> orders) {
        RevenueStatistics stats = new RevenueStatistics();

        for (Order order : orders) {
            // Skip cancelled orders
            if ("REJECTED".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                for (OrderItem item : order.getOrderItems()) {
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

    private void updateStatistics(RevenueStatistics current, RevenueStatistics previous) {
        // Update labels with current month data
        totalRevenueLabel.setText(String.format("₺%.2f", current.totalRevenue));
        totalTicketsLabel.setText(String.valueOf(current.ticketCount));
        totalProductsLabel.setText(String.valueOf(current.productCount));
        taxAmountLabel.setText(String.format("₺%.2f", current.totalVAT));

        // Calculate and display percentage changes
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

    private double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous.equals(BigDecimal.ZERO)) {
            return 100.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private void updateRevenueTable(List<Order> orders) {
        ObservableList<RevenueEntry> entries = FXCollections.observableArrayList();

        for (Order order : orders) {
            // Skip cancelled orders
            if ("REJECTED".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                String date = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Group items by type and calculate total amount considering quantity
                Map<String, BigDecimal> typeRevenue = order.getOrderItems().stream()
                        .collect(Collectors.groupingBy(
                                OrderItem::getItemType,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                        BigDecimal::add
                                )
                        ));

                // Add entries for each type
                typeRevenue.forEach((type, amount) -> {
                    // Calculate total quantity for this type
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