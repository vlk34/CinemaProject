package com.group18.controller.cashier.stageSpecificFiles;
import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.DBConnection;
import com.group18.dao.OrderDAO;
import com.group18.dao.PriceDAO;
import com.group18.model.OrderItem;
import com.group18.model.ShoppingCart;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller class for selecting movie seats in the cashier's system.
 * Handles seat selection, updating seat availability, and adding selected seats to the cart.
 */
public class CashierSeatSelectController {
    /**
     * Represents a Label control within the CashierSeatSelectController used to display the title of the selected movie.
     * This label is updated dynamically to reflect the title of the movie currently associated with the selected session.
     */
    @FXML private Label movieTitleLabel;
    /**
     * Represents a label in the UI used to display the selected date
     * associated with a movie session in the CashierSeatSelectController.
     * This label is updated to reflect the current date selection
     * for the session being configured.
     */
    @FXML private Label dateLabel;
    /**
     * Represents a label in the user interface that displays the selected movie session time.
     * This label is updated with the specific time information corresponding to the currently chosen movie session.
     * It is a part of the UI elements managed by the {@code CashierSeatSelectController}.
     */
    @FXML private Label timeLabel;
    /**
     * Represents a label in the UI that displays the name or identifier of the hall
     * in which a movie session is being held. This label is updated dynamically
     * to reflect the selected movie session's hall information.
     */
    @FXML private Label hallLabel;
    /**
     * Represents a grid layout used for displaying and managing seat arrangements in a cinema hall.
     * Each cell in the grid corresponds to a seat, with its state (e.g., available, selected, or occupied)
     * dynamically updated based on user interactions and session information.
     *
     * This field is annotated with @FXML, indicating it is injected by the JavaFX framework
     * and defined within the associated FXML file of the controller.
     */
    @FXML private GridPane seatGrid;
    /**
     * Represents the label in the UI that displays the summary of selected seats.
     * This label is dynamically updated to show the seat numbers selected by the user
     * during the seat selection process.
     */
    @FXML private Label selectedSeatsLabel;
    /**
     * Represents the label used to display the total price of selected seats.
     * This UI component is part of the CashierSeatSelectController, and it is updated
     * dynamically based on the user's seat selection to reflect the total cost of all chosen seats.
     */
    @FXML private Label totalPriceLabel;
    /**
     * Represents the confirm button in the seat selection interface.
     * This button is used to confirm the selected seats during the booking process.
     * Triggering this button finalizes the current selection and proceeds to the next step.
     */
    @FXML private Button confirmButton;

    /**
     * Represents the current movie selected in the cashier seat selection process.
     *
     * This variable is used to store information about the movie for which the seats
     * are being booked. It includes details such as the movie's title, genres, duration,
     * and other relevant properties encapsulated within the {@link Movie} class.
     *
     * The movie variable is updated when the session information is set, providing
     * context for seat selection and ticket confirmation.
     */
    private Movie movie;
    /**
     * Represents the current movie session selected by the cashier.
     * Contains information about the scheduled movie session, including its schedule ID,
     * cinema hall, start time, and available seats.
     * Used to manage the session-related operations within the cashier seat selection process.
     */
    private MovieSession session;
    /**
     * Represents the date for a specific movie session.
     *
     * This field stores the LocalDate object corresponding to the date of the session
     * being managed or selected. It is used in determining the session's schedule,
     * alongside other details such as time and hall information.
     */
    private LocalDate date;
    /**
     * Represents the set of currently selected seats for a movie session.
     * Each seat is identified by a unique string, typically corresponding to a seat identifier.
     * This set ensures that there are no duplicate selected seats and that the selection
     * is maintained in a consistent order due to the TreeSet implementation.
     */
    private Set<String> selectedSeats = new TreeSet<>();
    /**
     * Represents the set of seats in a session that are currently marked as occupied.
     * Maintains a collection of seat identifiers (e.g., "A1", "B2") that are unavailable
     * for selection by the user.
     *
     * This field is primarily used to track and update the availability status of seats
     * during seat selection and to prevent the user from selecting already occupied seats.
     */
    private Set<String> occupiedSeats = new HashSet<>();
    /**
     * Represents the price of a single ticket for a movie session.
     * This value is used to calculate the total cost of selected tickets
     * during the seat selection and checkout process.
     */
    private double ticketPrice = 50.0;
    /**
     * Represents the Data Access Object (DAO) for managing price-related data and operations
     * within the application, such as ticket pricing, discounts, and price history.
     * Used for retrieving or updating price information from the database.
     */
    private PriceDAO priceDAO;
    /**
     * Represents the shopping cart used to store ticket selections and associated details
     * during the seat selection process. This cart is used to manage and persist user
     * selections until confirmation.
     */
    private ShoppingCart cart;

    /**
     * Represents the total number of seats available in Hall A.
     * This value is utilized for seat layout creation and related validation processes.
     * Hall A is designed as a 4x4 grid.
     */
    private static final int HALL_A_SIZE = 16; // 4x4
    /**
     * Represents the number of seats available in Hall B.
     * This constant defines the total seat capacity in Hall B as 48, arranged in a 6x8 grid.
     * Used for initializing and managing seat grid creation and seat availability in Hall B.
     */
    private static final int HALL_B_SIZE = 48; // 6x8

    /**
     * A reference to the {@code CashierController}, which manages the cashier-related operations
     * in the application. This controller is used for communication and coordination between
     * different parts of the system regarding cashier functionality.
     */
    private CashierController cashierController;
    /**
     * Represents the singleton instance of the {@code CashierSeatSelectController}.
     * This variable provides access to the globally shared controller instance
     * for managing seat selection functionality in the cashier interface.
     * It is intended to ensure a consistent and centralized point of interaction
     * across different parts of the application.
     */
    private static CashierSeatSelectController currentInstance;

    /**
     * Sets the CashierController instance for this controller.
     *
     * @param controller the CashierController instance to be associated with this controller
     */
    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    /**
     * Initializes the controller for the user interface.
     *
     * The method performs several setup operations, such as:
     * - Disabling the confirmButton initially.
     * - Initializing a PriceDAO instance for database operations.
     * - Retrieving the singleton instance of the ShoppingCart.
     * - Assigning the current instance of the controller to a field for reference.
     * - Setting up interactive animations for the confirmButton when
     *   the mouse enters or exits the button.
     *
     * The animations use scale transitions to provide visual feedback for user actions.
     * The button scales up slightly when hovered over and reverts to its original size
     * when the mouse exits.
     */
    @FXML
    private void initialize() {
        confirmButton.setDisable(true);
        priceDAO = new PriceDAO();
        cart = ShoppingCart.getInstance();

        currentInstance = this;

        confirmButton.setOnMouseEntered(e -> {
            // Scale-up animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), confirmButton);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();
        });

        confirmButton.setOnMouseExited(e -> {
            // Scale-down animation
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), confirmButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    /**
     * Resets the seat selection state and updates the UI components accordingly.
     * This method clears the currently selected seats, resets associated
     * labels and buttons, and recreates the visual seat grid to reflect the changes.
     * It ensures that the selection summary and total price are updated,
     * and disables the confirm button until new seats are selected.
     */
    public void resetSeats() {
        System.out.println("Selected Seats before clear: " + selectedSeats); // Before clearing
        selectedSeats.clear();
        System.out.println("Selected Seats after clear: " + selectedSeats); // After clearing
        updateSelectionSummary();
        if (seatGrid != null) {
            createSeatGrid(); // Recreate the grid to reset all visual states
        }

        // Reset labels and button
        if (selectedSeatsLabel != null) {
            selectedSeatsLabel.setText("");
        }
        if (totalPriceLabel != null) {
            totalPriceLabel.setText("₺0.00");
        }
        if (confirmButton != null) {
            confirmButton.setDisable(true);
        }
    }

    /**
     * Clears the selected seats and resets the current instance state if applicable.
     *
     * This static method performs the following actions:
     * - Clears the current shopping cart instance by invoking the clear method.
     * - If a current instance of CashierSeatSelectController exists, calls the instance's resetSeats method to reset selected seats.
     *
     * Typically used when logging out or selecting a new movie session to reset all seat-related selections.
     */
    public static void clearSelectedSeatsStatic() {
        // This static method will be called when logging out or selecting a new movie
        ShoppingCart.getInstance().clear();

        if (currentInstance != null) {
            currentInstance.resetSeats();
        }
    }

    /**
     * Sets the session information for the selected movie and updates relevant details and components,
     * including ticket price, session-related labels, and seating grid.
     *
     * @param movie   The Movie object representing the selected movie.
     * @param session The MovieSession object representing the selected session.
     * @param date    The LocalDate representing the session date.
     */
    public void setSessionInfo(Movie movie, MovieSession session, LocalDate date) {
        this.movie = movie;
        this.session = session;
        this.date = date;

        // Load ticket price from database using your existing PriceDAO
        this.ticketPrice = priceDAO.getTicketPrice(session.getHall());

        updateSessionInfo();
        loadOccupiedSeats();
        createSeatGrid();
    }

    /**
     * Restores the previously selected seats by ensuring they are not already occupied
     * and adding them back to the selection.
     *
     * @param previousSeats A set of seat identifiers representing the previously
     *                      selected seats. If the set contains seats that are
     *                      already occupied, those seats are filtered out from
     *                      the selection before restoration.
     */
    public void restorePreviousSeats(Set<String> previousSeats) {
        if (previousSeats != null && !previousSeats.isEmpty()) {
            // Filter out occupied seats from previous selection
            previousSeats.removeAll(occupiedSeats);

            // Add previously selected seats
            for (String seatId : previousSeats) {
                addSeatToSelection(seatId);
            }
        }
    }

    /**
     * Updates the labels displayed in the user interface to show the current movie session's details.
     * This method sets the text for the movie title, session date, session time, and cinema hall name
     * based on the provided movie, session, and date information.
     *
     * It ensures that the labels reflect the current session data, such as:
     * - The movie title fetched using the movie's {@code getTitle} method.
     * - The session date converted to a string using the {@code toString} method.
     * - The session time retrieved through the session's {@code getTime} method and displayed as a string.
     * - The cinema hall name retrieved using the session's {@code getHall} method.
     */
    private void updateSessionInfo() {
        movieTitleLabel.setText(movie.getTitle());
        dateLabel.setText(date.toString());
        timeLabel.setText(session.getTime().toString());
        hallLabel.setText(session.getHall());
    }

    /**
     * Loads the currently occupied seats for a specific movie session from the database
     * and updates the local list of occupied seats.
     *
     * The method retrieves seat numbers that correspond to unprocessed orders
     * for the specified movie session. It clears the existing list of occupied seats,
     * fetches the relevant records from the database, and converts the seat numbers
     * into their display format before adding them to the list.
     *
     * This method executes the following steps:
     * 1. Clears the `occupiedSeats` collection.
     * 2. Executes a query to retrieve seat numbers from `order_items` for the
     *    current session, filtering by schedule ID and excluding fully processed orders.
     * 3. Converts each numeric seat representation to its display format
     *    (e.g., A1, B1) using the `convertNumberToSeatId` method.
     * 4. Adds the converted seats to the `occupiedSeats` collection.
     *
     * This method also manages database connections and handles SQL exceptions
     * that may occur during the query execution.
     *
     * Note: The built query uses the session's schedule ID to filter seat reservations.
     * It assumes the presence of the required database schema and data integrity.
     */
    private void loadOccupiedSeats() {
        occupiedSeats.clear();
        String query = """
        SELECT seat_number 
        FROM order_items 
        WHERE schedule_id = ? 
          AND item_type = 'ticket'
          AND order_id IN (
              SELECT order_id 
              FROM orders 
              WHERE status NOT IN ('PROCESSED_FULL', 'PROCESSED_TICKETS')
          )
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, session.getScheduleId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Convert numeric seat to display format (A1, B1, etc.)
                int seatNumber = rs.getInt("seat_number");
                occupiedSeats.add(convertNumberToSeatId(seatNumber));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a grid layout for the seating arrangement of a cinema hall, based on the current session's hall.
     * The grid dynamically adapts to the hall's configuration, arranging seats into rows and columns.
     * Each seat is represented by a {@code StackPane}, which includes a circular representation of the seat and a label with its identifier.
     * Occupied seats are marked differently from available ones, and interactive behavior is added for seat selection.
     *
     * The method performs the following operations:
     * 1. Clears any existing content in the {@code seatGrid}.
     * 2. Determines the number of total seats, rows, and columns based on the hall associated with the session.
     * 3. Iterates through rows and columns to generate seat identifiers (e.g., A1, B2).
     * 4. Creates individual seat components using the {@code createSeat} method and adds them to the grid.
     */
    private void createSeatGrid() {
        seatGrid.getChildren().clear();

        int totalSeats = session.getHall().equals("Hall_A") ? HALL_A_SIZE : HALL_B_SIZE;
        int cols = session.getHall().equals("Hall_A") ? 4 : 8;
        int rows = totalSeats / cols;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String seatId = String.format("%c%d", (char)('A' + row), col + 1);
                StackPane seatPane = createSeat(seatId);
                seatGrid.add(seatPane, col, row);
            }
        }
    }

    /**
     * Creates a visual representation of a seat in the theater as a StackPane.
     * The appearance and behavior of the seat are determined based on whether
     * it is occupied or available. Includes interactivity for selection and
     * hover effects for available seats.
     *
     * @param seatId The unique identifier of the seat to be created.
     * @return A StackPane representing the specified seat, including its visual
     *         components and interactive functionalities.
     */
    private StackPane createSeat(String seatId) {
        StackPane seatPane = new StackPane();
        seatPane.setPrefSize(50, 50);
        seatPane.setMaxSize(50, 50);

        // Create the seat circle
        Circle seatCircle = new Circle(20);

        // Determine seat style based on occupation status
        if (occupiedSeats.contains(seatId)) {
            seatCircle.setFill(javafx.scene.paint.Color.valueOf("#E74C3C")); // Red for occupied
            seatCircle.setOpacity(0.7);
        } else {
            seatCircle.setFill(javafx.scene.paint.Color.valueOf("#2ECC71")); // Green for available
        }

        // Add subtle border
        seatCircle.setStroke(javafx.scene.paint.Color.valueOf("#2a1b35"));
        seatCircle.setStrokeWidth(2);

        // Create seat label
        Label label = new Label(seatId);
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setStyle("-fx-font-weight: bold;");

        seatPane.getChildren().addAll(seatCircle, label);

        // Add click handler for available seats
        if (!occupiedSeats.contains(seatId)) {
            seatPane.setOnMouseClicked(e -> toggleSeatSelection(seatId, seatCircle));

            // Add hover effects with scaling animation
            seatPane.setOnMouseEntered(e -> {
                // Scale-up animation
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), seatCircle);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();

                seatCircle.setOpacity(0.7);
                seatPane.setCursor(javafx.scene.Cursor.HAND);
            });

            seatPane.setOnMouseExited(e -> {
                // Scale-down animation
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), seatCircle);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();

                if (!selectedSeats.contains(seatId)) {
                    seatCircle.setOpacity(1.0);
                }
            });
        }

        return seatPane;
    }

    /**
     * Adds the specified seat to the current selection, updates its visual appearance to indicate
     * it has been selected, and refreshes the selection summary.
     *
     * @param seatId the unique identifier of the seat to be added to the selection
     */
    private void addSeatToSelection(String seatId) {
        // Find the corresponding seat pane
        for (Node node : seatGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane seatPane = (StackPane) node;
                Label seatLabel = (Label) seatPane.getChildren().get(1);

                if (seatLabel.getText().equals(seatId)) {
                    Circle seatCircle = (Circle) seatPane.getChildren().get(0);

                    System.out.println("adding selected seats to grid");
                    // Add seat to selection
                    selectedSeats.add(seatId);
                    seatCircle.setFill(javafx.scene.paint.Color.valueOf("#3498DB")); // Blue for selected
                    seatCircle.setOpacity(0.8);
                    break;
                }
            }
        }

        updateSelectionSummary();
    }

    /**
     * Toggles the selection state of a seat by adding or removing it from the selected seats list
     * and updates the visual representation of the seat.
     *
     * @param seatId      The identifier of the seat to toggle.
     * @param seatCircle  The circle UI element representing the seat that will be updated visually.
     */
    private void toggleSeatSelection(String seatId, Circle seatCircle) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seatCircle.setFill(javafx.scene.paint.Color.valueOf("#2ECC71")); // Back to green
            seatCircle.setOpacity(1.0);

        } else {
            selectedSeats.add(seatId);
            seatCircle.setFill(javafx.scene.paint.Color.valueOf("#3498DB")); // Blue for selected
            seatCircle.setOpacity(0.8);
        }

        updateSelectionSummary();
    }

    /**
     * Updates the selection summary by refreshing the selected seats label, total price label,
     * and enabling or disabling the confirmation button based on the current selection.
     *
     * The selected seats are displayed as a comma-separated list in the selectedSeatsLabel.
     * The total price is calculated based on the size of the selected seats and the ticket price,
     * and displayed in the totalPriceLabel in currency format.
     * The confirmButton is disabled if no seats are selected and enabled otherwise.
     */
    private void updateSelectionSummary() {
        selectedSeatsLabel.setText(String.join(", ", selectedSeats));
        double total = selectedSeats.size() * ticketPrice;
        totalPriceLabel.setText(String.format("₺%.2f", total));
        confirmButton.setDisable(selectedSeats.isEmpty());
    }

    /**
     * Handles the confirmation of selected seats by the user.
     *
     * This method validates the selection of seats, displays a confirmation dialog
     * showing the selected seats and total price, and proceeds with the selection if confirmed.
     * If no seats are selected, an error message is displayed.
     *
     * Behavior:
     * 1. If no seats are selected, shows an error dialog indicating that at least one seat must be selected.
     * 2. Displays a confirmation dialog summarizing the selected seats and total price.
     * 3. If the user confirms the selection:
     *    - Adds the selected seats to the cart.
     *    - Navigates to the next stage with the selected seats data.
     */
    @FXML
    private void handleConfirmSelection() {
        if (selectedSeats.isEmpty()) {
            showError("No Seats Selected", "Please select at least one seat.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Seats");
        confirm.setHeaderText("Selected Seats: " + String.join(", ", selectedSeats));
        confirm.setContentText(String.format(
                "Total Price: ₺%.2f%n%nDo you want to proceed with these seats? Any products in your cart will be reset.",
                selectedSeats.size() * ticketPrice
        ));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            addSeatsToCart();

            cashierController.navigateWithData(selectedSeats);
        }
    }

    /**
     * Adds the selected seats to the shopping cart through the CashierCartController.
     * This method ensures the session context is properly set in the cart before adding the selected seats.
     * The method interacts with the CashierController to retrieve the cart controller and performs the operation
     * only if the cart controller is not null.
     *
     * Preconditions:
     * - The `cashierController` field should be initialized.
     * - `movie`, `session`, `date`, and `selectedSeats` fields should be properly set before invoking this method.
     *
     * Behavior:
     * - Retrieves the CashierCartController from the CashierController.
     * - Sets the session context in the CashierCartController based on the current movie, session, and date.
     * - Adds the selected seats to the cart via the CashierCartController.
     */
    private void addSeatsToCart() {
        CashierCartController cartController = cashierController.getCartController();
        if (cartController != null) {

            cartController.setSessionContext(movie, session, date);
            cartController.addSeatsToCart(selectedSeats);
        }
    }

    /**
     * Converts a numeric seat number to a seat identifier in the format of a lettered row
     * and numbered column (e.g., A1, B3). The conversion is based on the cinema hall's row
     * and column configuration. The hall configuration is determined by the session's hall value.
     *
     * @param number the numeric seat number to be converted
     * @return the formatted seat identifier as a String
     */
    private String convertNumberToSeatId(int number) {
        // Convert 1, 5 etc. back to A1, B1 etc.
        int cols = session.getHall().equals("Hall_A") ? 4 : 8;
        int row = (number - 1) / cols;
        int col = ((number - 1) % cols) + 1;
        return String.format("%c%d", (char)('A' + row), col);
    }

    /**
     * Displays an error alert with the specified title and content.
     *
     * @param title   the title of the error alert
     * @param content the content or message displayed in the error alert
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}