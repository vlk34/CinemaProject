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
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class CashierSeatSelectController {
    @FXML private Label movieTitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label hallLabel;
    @FXML private GridPane seatGrid;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmButton;

    private Movie movie;
    private MovieSession session;
    private LocalDate date;
    private Set<String> selectedSeats = new TreeSet<>();
    private Set<String> occupiedSeats = new HashSet<>();
    private double ticketPrice = 50.0;
    private PriceDAO priceDAO;
    private ShoppingCart cart;

    private static final int HALL_A_SIZE = 16; // 4x4
    private static final int HALL_B_SIZE = 48; // 6x8

    private CashierController cashierController;
    private static CashierSeatSelectController currentInstance;

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

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

    public static void clearSelectedSeatsStatic() {
        // This static method will be called when logging out or selecting a new movie
        ShoppingCart.getInstance().clear();

        if (currentInstance != null) {
            currentInstance.resetSeats();
        }
    }

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

    // New method to restore previously selected seats
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

    private void updateSessionInfo() {
        movieTitleLabel.setText(movie.getTitle());
        dateLabel.setText(date.toString());
        timeLabel.setText(session.getTime().toString());
        hallLabel.setText(session.getHall());
    }

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
                scaleUp.setToX(1.1); // Scale by 20%
                scaleUp.setToY(1.1); // Scale by 20%
                scaleUp.play();

                seatCircle.setOpacity(0.7);
                seatPane.setCursor(javafx.scene.Cursor.HAND);
            });

            seatPane.setOnMouseExited(e -> {
                // Scale-down animation
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), seatCircle);
                scaleDown.setToX(1.0); // Return to original size
                scaleDown.setToY(1.0); // Return to original size
                scaleDown.play();

                if (!selectedSeats.contains(seatId)) {
                    seatCircle.setOpacity(1.0);
                }
            });
        }

        return seatPane;
    }

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

    // Modify toggleSeatSelection to work with Circle instead of Region
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

    private void updateSelectionSummary() {
        selectedSeatsLabel.setText(String.join(", ", selectedSeats));
        double total = selectedSeats.size() * ticketPrice;
        totalPriceLabel.setText(String.format("₺%.2f", total));
        confirmButton.setDisable(selectedSeats.isEmpty());
    }

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

    private void addSeatsToCart() {
        CashierCartController cartController = cashierController.getCartController();
        if (cartController != null) {

            cartController.setSessionContext(movie, session, date);
            cartController.addSeatsToCart(selectedSeats);
        }
    }

    private String convertNumberToSeatId(int number) {
        // Convert 1, 5 etc. back to A1, B1 etc.
        int cols = session.getHall().equals("Hall_A") ? 4 : 8;
        int row = (number - 1) / cols;
        int col = ((number - 1) % cols) + 1;
        return String.format("%c%d", (char)('A' + row), col);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}