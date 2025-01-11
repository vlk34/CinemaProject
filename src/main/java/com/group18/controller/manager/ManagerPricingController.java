package com.group18.controller.manager;

import com.group18.dao.PriceDAO;
import com.group18.model.PriceHistory;
import com.group18.model.User;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

public class ManagerPricingController {
    @FXML
    private TextField hallAPriceField;
    @FXML
    private TextField hallBPriceField;
    @FXML
    private TextField ageDiscountField;
    @FXML
    private Button updateButton1;
    @FXML
    private Button updateButton2;
    @FXML
    private Button updateButton3;
    @FXML
    private TableView<PriceHistory> priceHistoryTable;
    @FXML
    private TableColumn<PriceHistory, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<PriceHistory, String> itemColumn;
    @FXML
    private TableColumn<PriceHistory, Double> oldPriceColumn;
    @FXML
    private TableColumn<PriceHistory, Double> newPriceColumn;
    @FXML
    private TableColumn<PriceHistory, String> updatedByColumn;

    private PriceDAO priceDAO;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void initialize() {
        priceDAO = new PriceDAO();
        setupInputValidation();
        loadCurrentPrices();
        setupPriceHistoryTable();
        loadPriceHistory();
        setupButtonHoverAnimation(updateButton1);
        setupButtonHoverAnimation(updateButton2);
        setupButtonHoverAnimation(updateButton3);

    }

    private void loadCurrentPrices() {
        double hallAPrice = priceDAO.getTicketPrice("Hall_A");
        double hallBPrice = priceDAO.getTicketPrice("Hall_B");
        double ageDiscount = priceDAO.getAgeDiscount();
        System.out.println(ageDiscount);
        hallAPriceField.setText(String.valueOf(hallAPrice));
        hallBPriceField.setText(String.valueOf(hallBPrice));
        ageDiscountField.setText(String.valueOf(ageDiscount));
    }

    @FXML
    private void handleUpdateHallAPrice() {
        updateTicketPrice("Hall_A", hallAPriceField.getText());
    }

    @FXML
    private void handleUpdateHallBPrice() {
        updateTicketPrice("Hall_B", hallBPriceField.getText());
    }

    private void updateTicketPrice(String hall, String newPriceStr) {
        try {
            double newPrice = Double.parseDouble(newPriceStr);
            double oldPrice = priceDAO.getTicketPrice(hall);

            if (newPrice == oldPrice) {
                showErrorAlert("Price is already set to " + newPrice + ". No update needed.");
                return;
            }

            if (priceDAO.updateTicketPrice(hall, newPrice)) {
                showSuccessAlert(hall + " Ticket Price Updated");
                logPriceChange(hall + " Ticket Price", oldPrice, newPrice);
                loadPriceHistory();
            } else {
                showErrorAlert("Failed to Update " + hall + " Ticket Price");
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Price Format");
        }
    }

    @FXML
    private void handleUpdateAgeDiscount() {
        String newDiscountStr = ageDiscountField.getText();
        try {
            double newDiscount = Double.parseDouble(newDiscountStr);

            // Validate discount range
            if (newDiscount < 0 || newDiscount > 100) {
                showErrorAlert("Discount must be between 0 and 100");
                return;
            }

            double oldDiscount = priceDAO.getAgeDiscount();

            if (newDiscount == oldDiscount) {
                showErrorAlert("Discount is already set to " + newDiscount + "%. No update needed.");
                return;
            }

            if (priceDAO.updateAgeDiscount(newDiscount)) {
                showSuccessAlert("Age Discount Updated");
                logPriceChange("Age Discount", oldDiscount, newDiscount);
                loadCurrentPrices(); // Reload all prices
                loadPriceHistory();
            } else {
                showErrorAlert("Failed to Update Age Discount");
                loadCurrentPrices(); // Reload even on failure to ensure current value is shown
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Discount Format. Please enter a number between 0 and 100");
        }
    }

    private void setupPriceHistoryTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("changeTimestamp"));
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setCellFactory(column -> new TableCell<PriceHistory, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toLocalDate().toString() + " " + date.toLocalTime().toString());
                }
            }
        });

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        itemColumn.setStyle("-fx-alignment: CENTER;");

        oldPriceColumn.setCellValueFactory(new PropertyValueFactory<>("oldPrice"));
        oldPriceColumn.setStyle("-fx-alignment: CENTER;");

        newPriceColumn.setCellValueFactory(new PropertyValueFactory<>("newPrice"));
        newPriceColumn.setStyle("-fx-alignment: CENTER;");

        updatedByColumn.setCellValueFactory(new PropertyValueFactory<>("updatedBy"));
        updatedByColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupInputValidation() {
        // Validate Hall A price input
        hallAPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                hallAPriceField.setText(oldValue);
            }
        });

        // Validate Hall B price input
        hallBPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                hallBPriceField.setText(oldValue);
            }
        });

        // Validate age discount input
        ageDiscountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                ageDiscountField.setText(oldValue);
            }
        });
    }

    private void loadPriceHistory() {
        List<PriceHistory> priceHistory = priceDAO.getPriceUpdateHistory();
        priceHistoryTable.setItems(FXCollections.observableList(priceHistory));
    }

    private void logPriceChange(String item, double oldPrice, double newPrice) {
        String user = currentUser != null
                ? currentUser.getFirstName() + " " + currentUser.getLastName()
                : "Unknown Manager";

        PriceHistory log = new PriceHistory(
                LocalDateTime.now(),
                item,
                oldPrice,
                newPrice,
                user
        );
        priceDAO.logPriceChange(log);
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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