package com.group18.controller.cashier.stageSpecificFiles;
import com.group18.controller.cashier.CashierController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import javafx.scene.shape.Circle;

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

    private static final int HALL_A_SIZE = 16; // 4x4
    private static final int HALL_B_SIZE = 48; // 6x8

    private CashierController cashierController;

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    @FXML
    private void initialize() {
        confirmButton.setDisable(true);
    }

    public void setSessionInfo(Movie movie, MovieSession session, LocalDate date) {
        this.movie = movie;
        this.session = session;
        this.date = date;

        updateSessionInfo();
        loadOccupiedSeats();
        createSeatGrid();
    }

    private void updateSessionInfo() {
        movieTitleLabel.setText(movie.getTitle());
        dateLabel.setText(date.toString());
        timeLabel.setText(session.getTime().toString());
        hallLabel.setText(session.getHall());
    }

    private void loadOccupiedSeats() {
        occupiedSeats.clear();
        // Sample occupied seats
        if (session.getHall().equals("Hall_A")) {
            occupiedSeats.addAll(Arrays.asList("A1", "B2"));
        } else {
            occupiedSeats.addAll(Arrays.asList("C4", "D5", "E6"));
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

            // Add hover effects
            seatPane.setOnMouseEntered(e -> {
                seatCircle.setOpacity(0.7);
                seatPane.setCursor(javafx.scene.Cursor.HAND);
            });
            seatPane.setOnMouseExited(e -> {
                if (!selectedSeats.contains(seatId)) {
                    seatCircle.setOpacity(1.0);
                }
            });
        }

        return seatPane;
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
                "Total Price: ₺%.2f%n%nDo you want to proceed with these seats?",
                selectedSeats.size() * ticketPrice
        ));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            addSeatsToCart();
        }
    }

    private void addSeatsToCart() {
        // TODO: Implement cart integration
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getters for selected seats
    public Set<String> getSelectedSeats() {
        return new TreeSet<>(selectedSeats);
    }

    public double getTotalPrice() {
        return selectedSeats.size() * ticketPrice;
    }
}