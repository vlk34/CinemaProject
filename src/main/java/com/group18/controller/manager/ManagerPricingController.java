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

/**
 * The ManagerPricingController class manages the user interface and interactions
 * for updating ticket prices and discounts. It facilitates operations such as
 * fetching current pricing details, processing updates, handling input validation,
 * and maintaining a history of price changes.
 *
 * The class interacts with the database through the `priceDAO` object for managing
 * price-related data and utilizes various UI components to provide functionality
 * for the manager.
 */
public class ManagerPricingController {
    /**
     * Represents the input field for setting or updating the price of Hall A tickets.
     * This field is part of the user interface and allows entering and editing the
     * price associated with Hall A. It is linked to FXML and managed within the
     * ManagerPricingController class.
     */
    @FXML
    private TextField hallAPriceField;
    /**
     * Represents the text field for entering or displaying the price of Hall B.
     * This field is used to input or update the pricing information specific to Hall B
     * within the manager pricing interface.
     */
    @FXML
    private TextField hallBPriceField;
    /**
     * Represents a text field in the UI that allows users to input age-based discount values.
     * This field is linked to the controller for managing and processing age discount updates.
     *
     * This field is part of the ManagerPricingController and interacts with related methods
     * such as handling age discount updates and input validation.
     */
    @FXML
    private TextField ageDiscountField;
    /**
     * Represents a button in the ManagerPricingController used to trigger
     * the update functionality for a specific field or operation.
     * It is assigned an event handler to handle user interactions
     * for updating relevant data.
     */
    @FXML
    private Button updateButton1;
    /**
     * Represents the button component used to trigger the update action for a specific
     * pricing configuration in the ManagerPricingController. This button is
     * designed to handle user interaction for modifying pricing data.
     *
     * The functionality tied to this button is typically specified in associated
     * event handler methods within the ManagerPricingController class, such as
     * updating pricing details or initiating specific form submissions.
     */
    @FXML
    private Button updateButton2;
    /**
     * Represents the third button in the user interface for updating a specific pricing component
     * or configuration in the application.
     *
     * This button is associated with specific functionality that enables the modification
     * of pricing fields or controls based on the application's requirements.
     *
     * The use and actions triggered by this button are defined in the
     * ManagerPricingController class.
     */
    @FXML
    private Button updateButton3;
    /**
     * Represents the TableView UI component in the ManagerPricingController class
     * that displays a list of price history records.
     * Each record reflects a change in price for a specific item, including details such as
     * the item name, old price, new price, timestamp of the change, and the user who updated it.
     * The table is tied to the PriceHistory model class.
     */
    @FXML
    private TableView<PriceHistory> priceHistoryTable;
    /**
     * Represents the TableColumn in the PriceHistory table that displays the timestamp
     * of when a price change occurred. This column is associated with the `changeTimestamp`
     * field in the PriceHistory model, which holds the date and time of each price modification.
     */
    @FXML
    private TableColumn<PriceHistory, LocalDateTime> dateColumn;
    /**
     * Represents a column in a TableView that displays the item name associated with a
     * price history record. Each cell in this column corresponds to the 'item' property
     * in a PriceHistory object.
     *
     * This column is part of the price history table in the ManagerPricingController
     * and is used to show the name or identifier of the item whose price history
     * is being tracked.
     */
    @FXML
    private TableColumn<PriceHistory, String> itemColumn;
    /**
     * Represents a table column within the price history table that displays
     * the previous price of an item before it was updated. This column is
     * associated with the `oldPrice` field of the `PriceHistory` class,
     * which stores the old price value of an item in the price history record.
     *
     * It is used in the `ManagerPricingController` class to manage and present
     * historical price data for items in a visual table format.
     */
    @FXML
    private TableColumn<PriceHistory, Double> oldPriceColumn;
    /**
     * Represents a table column in the `priceHistoryTable` that displays the new price
     * of an item after a price change in the price history records.
     * This column is associated with the `newPrice` field of the `PriceHistory` class.
     */
    @FXML
    private TableColumn<PriceHistory, Double> newPriceColumn;
    /**
     * Represents a table column within the Price History Table of the ManagerPricingController.
     * This column displays the name or identifier of the user who last updated the price of an item.
     * Data for this column is sourced from the 'updatedBy' property of the PriceHistory object.
     */
    @FXML
    private TableColumn<PriceHistory, String> updatedByColumn;

    /**
     * A data access object (DAO) instance used to perform operations related to pricing.
     * This variable facilitates interactions with the `PriceDAO` class, which handles
     * ticket price management, age discount retrieval and updates, price history logging,
     * and fetching the price change history.
     */
    private PriceDAO priceDAO;

    /**
     * Represents the currently logged-in user in the system.
     *
     * This variable holds the reference to a User object, which encapsulates details
     * about the individual interacting with the application, such as their username,
     * role, and personal information. It is used throughout the class to perform
     * user-specific actions, such as logging price updates, maintaining an audit trail,
     * or determining access permissions.
     */
    private User currentUser;

    /**
     * Sets the current user for the ManagerPricingController.
     *
     * @param user the user to be set as the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Initializes the ManagerPricingController by setting up the required components.
     * This method performs the following actions:
     * - Instantiates the PriceDAO object for handling price-related database operations.
     * - Configures input validation for user inputs in the pricing fields.
     * - Loads the current prices and sets them into the appropriate text fields.
     * - Sets up the price history table with the appropriate column mappings and styles.
     * - Loads the historical pricing data into the price history table.
     * - Configures button hover animations for the update buttons.
     */
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

    /**
     * Loads the current ticket prices for specific halls and the age discount
     * into the respective user interface fields.
     *
     * This method fetches the ticket prices for "Hall_A" and "Hall_B" from the
     * database and updates the text fields (`hallAPriceField` and `hallBPriceField`)
     * with the corresponding prices. Additionally, it retrieves the current age
     * discount rate and updates the associated text field (`ageDiscountField`).
     * All fetched values are converted to string format for display.
     *
     * Uses:
     * - `priceDAO.getTicketPrice(String hall)` to retrieve ticket prices for halls.
     * - `priceDAO.getAgeDiscount()` to retrieve the age discount rate.
     */
    private void loadCurrentPrices() {
        double hallAPrice = priceDAO.getTicketPrice("Hall_A");
        double hallBPrice = priceDAO.getTicketPrice("Hall_B");
        double ageDiscount = priceDAO.getAgeDiscount();
        System.out.println(ageDiscount);
        hallAPriceField.setText(String.valueOf(hallAPrice));
        hallBPriceField.setText(String.valueOf(hallBPrice));
        ageDiscountField.setText(String.valueOf(ageDiscount));
    }

    /**
     * Handles the update of ticket pricing for Hall A.
     * This method retrieves the new price input from the corresponding text field,
     * validates and processes the change by invoking the `updateTicketPrice` method.
     * If the price update is successful, it triggers a success message, updates the
     * price history, and logs the change. If the update fails, or the input format is
     * invalid, an appropriate error message is displayed.
     */
    @FXML
    private void handleUpdateHallAPrice() {
        updateTicketPrice("Hall_A", hallAPriceField.getText());
    }

    /**
     * Handles the update of the ticket price for Hall B.
     * This method retrieves the new price input from the user via the associated text field,
     * and passes it to the `updateTicketPrice` method along with the identifier for Hall B ("Hall_B").
     *
     * The `updateTicketPrice` method validates and processes the update request,
     * checks if the price input is valid and different from the current one,
     * and saves the updated price if necessary. The result of the operation is displayed to the user,
     * and the price history is updated accordingly.
     */
    @FXML
    private void handleUpdateHallBPrice() {
        updateTicketPrice("Hall_B", hallBPriceField.getText());
    }

    /**
     * Updates the ticket price for a specified hall after validating the input and
     * checking for changes compared to the current price. Notifies the user of the outcome
     * and logs the price change if successful.
     *
     * @param hall the name of the hall whose ticket price is being updated
     * @param newPriceStr the new price specified as a string
     */
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

    /**
     * Handles the update of the age discount when triggered by the user.
     *
     * This method retrieves the new discount value entered in the ageDiscountField,
     * validates it, and updates the age discount in the database if valid.
     * Validations ensure the discount is a numeric value between 0 and 100
     * and that it is different from the currently set value.
     *
     * If the new discount value is valid and successfully updated, the method:
     * - Displays a success alert to the user.
     * - Logs the price change in the price history.
     * - Reloads the current prices and price history to reflect updates.
     *
     * If the update fails or the input is invalid, the method:
     * - Provides error feedback to the user through an alert.
     * - Reloads current prices to ensure consistency.
     *
     * The method handles user input errors, such as entering non-numeric values or
     * invalid ranges, by catching a NumberFormatException and showing an appropriate alert.
     */
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

    /**
     * Configures the columns of the price history table to display and format data
     * associated with changes in prices. This method initializes each column's cell
     * value factory and style, and customizes the display of the date column.
     *
     * The date column's cells are formatted to display the date and time in a readable
     * string representation. Other columns include item, old price, new price, and the
     * user responsible for the change, which are aligned to the center.
     *
     * The method ensures that all necessary bindings and formatting are in place for
     * properly presenting price change records to the user.
     */
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

    /**
     * Configures input validation for certain text fields to ensure only valid numeric
     * data (including decimal numbers) is entered.
     *
     * This method applies listeners to specific text fields to restrict input to a valid numeric format.
     * It validates the input for Hall A price, Hall B price, and age discount fields. If the user
     * enters an invalid value, the text field reverts to its previous valid value.
     *
     * Validation rules:
     * - Only numeric values are allowed (including optional decimal points).
     * - Invalid input is automatically replaced with the prior valid value.
     *
     * This method contributes to maintaining data integrity by rejecting malformed input values
     * before they can be processed.
     */
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

    /**
     * Loads the price update history and populates the price history table with the retrieved data.
     *
     * This method fetches the price update history using the `priceDAO` instance
     * and updates the `priceHistoryTable` UI component with the list of `PriceHistory`
     * objects in an observable format. The data is used to display previously logged
     * changes in item prices, including details such as the item, old price,
     * new price, update timestamp, and the user who performed the update.
     */
    private void loadPriceHistory() {
        List<PriceHistory> priceHistory = priceDAO.getPriceUpdateHistory();
        priceHistoryTable.setItems(FXCollections.observableList(priceHistory));
    }

    /**
     * Logs the details of a price change for a specific item. This includes the item's name,
     * old price, new price, the timestamp when the change occurred, and the user who performed
     * the change. The information is recorded in the price history using the data access layer.
     *
     * @param item the name of the item whose price has been updated
     * @param oldPrice the prior price of the item before the update
     * @param newPrice the new price of the item after the update
     */
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

    /**
     * Displays a success alert dialog with a specified message.
     *
     * @param message the text to be displayed in the content of the alert
     */
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error alert dialog with the specified message. The alert
     * is shown as a modal dialog and requires user acknowledgment before
     * proceeding.
     *
     * @param message the error message to display in the alert dialog
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Configures hover animation for a button to provide a visual effect when it is pressed
     * or released. This adds a scale transition effect when the mouse interacts with the button.
     * The button slightly scales down on press and returns to its original size on release.
     *
     * @param button the Button instance on which to apply the hover animation
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