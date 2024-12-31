package com.group18.controller.manager;

import com.group18.dao.PriceDAO;
import com.group18.model.PriceHistory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

import java.util.List;

public class ManagerPricingController {
    @FXML
    private TextField hallAPriceField;
    @FXML
    private TextField hallBPriceField;
    @FXML
    private TextField ageDiscountField;
    @FXML
    private TableView<PriceHistory> priceHistoryTable;
    @FXML
    private TableColumn<PriceHistory, LocalDate> dateColumn;
    @FXML
    private TableColumn<PriceHistory, String> itemColumn;
    @FXML
    private TableColumn<PriceHistory, Double> oldPriceColumn;
    @FXML
    private TableColumn<PriceHistory, Double> newPriceColumn;
    @FXML
    private TableColumn<PriceHistory, String> updatedByColumn;

    private PriceDAO priceDAO;

    public void initialize() {
        priceDAO = new PriceDAO();
        loadCurrentPrices();
        setupPriceHistoryTable();
        loadPriceHistory();
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
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("changeDate"));
        dateColumn.setStyle("-fx-alignment: CENTER;");

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        itemColumn.setStyle("-fx-alignment: CENTER;");

        oldPriceColumn.setCellValueFactory(new PropertyValueFactory<>("oldPrice"));
        oldPriceColumn.setStyle("-fx-alignment: CENTER;");

        newPriceColumn.setCellValueFactory(new PropertyValueFactory<>("newPrice"));
        newPriceColumn.setStyle("-fx-alignment: CENTER;");

        updatedByColumn.setCellValueFactory(new PropertyValueFactory<>("updatedBy"));
        updatedByColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void loadPriceHistory() {
        List<PriceHistory> priceHistory = priceDAO.getPriceUpdateHistory();
        priceHistoryTable.setItems(FXCollections.observableList(priceHistory));
    }

    private void logPriceChange(String item, double oldPrice, double newPrice) {
        String user = "Manager1"; // Get the actual logged in user
        PriceHistory log = new PriceHistory(
                LocalDate.now(),  // Using LocalDate.now() instead of formatted string
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
}