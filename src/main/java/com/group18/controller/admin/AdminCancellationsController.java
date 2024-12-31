//package com.group18.controller.admin;
//
//import com.group18.dao.OrderDAO;
//import com.group18.dao.PriceDAO;
//import com.group18.dao.ProductDAO;
//import com.group18.dao.MovieDAO;
//import com.group18.model.Order;
//import com.group18.model.OrderItem;
//import com.group18.model.Movie;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class AdminCancellationsController {
//    @FXML
//    private TableView<Order> ordersTable;
//
//    @FXML
//    private TableColumn<Order, Integer> orderIdColumn;
//
//    @FXML
//    private TableColumn<Order, LocalDateTime> orderDateColumn;
//
//    @FXML
//    private TableColumn<Order, Double> totalPriceColumn;
//
//    @FXML
//    private TableView<OrderItem> orderItemsTable;
//
//    @FXML
//    private TableColumn<OrderItem, String> itemTypeColumn;
//
//    @FXML
//    private TableColumn<OrderItem, String> itemNameColumn;
//
//    @FXML
//    private TableColumn<OrderItem, Integer> quantityColumn;
//
//    @FXML
//    private Button processCancellationButton;
//
//    @FXML
//    private TextField orderSearchField;
//
//    private OrderDAO orderDAO;
//    private PriceDAO priceDAO;
//    private ProductDAO productDAO;
//    private MovieDAO movieDAO;
//    private Order selectedOrder;
//
//    @FXML
//    private void initialize() {
//        orderDAO = new OrderDAO();
//        priceDAO = new PriceDAO();
//        productDAO = new ProductDAO();
//        movieDAO = new MovieDAO();
//
//        // Setup orders table columns
//        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
//        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
//        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
//
//        // Setup order items table columns
//        itemTypeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));
//        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        itemNameColumn.setCellValueFactory(cellData -> {
//            OrderItem item = cellData.getValue();
//            if ("ticket".equals(item.getItemType())) {
//                // If it's a ticket, find the movie name
//                return new SimpleStringProperty(getMovieName(item));
//            } else {
//                // If it's a product, find the product name
//                return new SimpleStringProperty(getProductName(item));
//            }
//        });
//
//        // Load orders
//        loadOrders();
//
//        // Setup table selection listeners
//        setupTableListeners();
//
//        // Setup search functionality
//        setupSearch();
//    }
//
//    private void setupTableListeners() {
//        // Orders table selection listener
//        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//            if (newSelection != null) {
//                selectedOrder = newSelection;
//                loadOrderItems(newSelection);
//            }
//        });
//    }
//
//    private void setupSearch() {
//        orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
//            filterOrders(newValue);
//        });
//    }
//
//    private void loadOrders() {
//        List<Order> orders = orderDAO.getAllOrders(); // You'll need to add this to OrderDAO
//        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
//        ordersTable.setItems(orderList);
//    }
//
//    private void loadOrderItems(Order order) {
//        List<OrderItem> items = priceDAO.getItemsByOrderId(order.getOrderId());
//        ObservableList<OrderItem> itemsList = FXCollections.observableArrayList(items);
//        orderItemsTable.setItems(itemsList);
//    }
//
//    private void filterOrders(String searchText) {
//        List<Order> allOrders = orderDAO.getAllOrders();
//        List<Order> filteredOrders = allOrders.stream()
//                .filter(order ->
//                        searchText.isEmpty() ||
//                                String.valueOf(order.getOrderId()).contains(searchText)
//                )
//                .collect(Collectors.toList());
//
//        ordersTable.setItems(FXCollections.observableArrayList(filteredOrders));
//    }
//
//    private String getMovieName(OrderItem item) {
//        if (item.getScheduleId() != null) {
//            // You might need to add a method in ScheduleDAO to get movie name by schedule ID
//            // For now, we'll use MovieDAO to get movie details
//            Movie movie = movieDAO.findMovieByScheduleId(item.getScheduleId());
//            return movie != null ? movie.getTitle() : "Unknown Movie";
//        }
//        return "Unknown Movie";
//    }
//
//    private String getProductName(OrderItem item) {
//        if (item.getProductId() != null) {
//            return productDAO.findById(item.getProductId()).getProductName();
//        }
//        return "Unknown Product";
//    }
//
//    @FXML
//    private void handleProcessCancellation() {
//        if (selectedOrder == null) {
//            showAlert("Please select an order to cancel.");
//            return;
//        }
//
//        // Process cancellation
//        boolean cancellationSuccessful = processCancellation(selectedOrder);
//
//        if (cancellationSuccessful) {
//            showAlert("Order cancellation processed successfully.");
//            loadOrders();
//            orderItemsTable.getItems().clear();
//        } else {
//            showAlert("Failed to process cancellation.");
//        }
//    }
//
//    private boolean processCancellation(Order order) {
//        // Refund logic
//        // 1. Cancel the order in the database
//        boolean orderCancelled = orderDAO.cancelOrder(order.getOrderId());
//
//        if (orderCancelled) {
//            // 2. Restore product stocks
//            for (OrderItem item : priceDAO.getItemsByOrderId(order.getOrderId())) {
//                if ("product".equals(item.getItemType())) {
//                    productDAO.increaseStock(item.getProductId(), item.getQuantity());
//                }
//                // Optionally, release ticket seats
//            }
//            return true;
//        }
//
//        return false;
//    }
//
//    private void showAlert(String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Cancellation");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}