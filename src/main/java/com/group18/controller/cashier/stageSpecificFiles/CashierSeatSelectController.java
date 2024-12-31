package com.group18.controller.cashier.stageSpecificFiles;
import com.group18.controller.cashier.CashierController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
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
        seatPane.setPrefSize(40, 40);
        seatPane.getStyleClass().add("seat");

        // Create the clickable region
        Region seatRegion = new Region();
        seatRegion.setPrefSize(30, 30);
        seatRegion.getStyleClass().add(occupiedSeats.contains(seatId) ?
                "seat-occupied" : "seat-available");

        // Add seat number
        Label label = new Label(seatId);
        label.setStyle("-fx-font-size: 12px;");

        seatPane.getChildren().addAll(seatRegion, label);

        if (!occupiedSeats.contains(seatId)) {
            seatPane.setOnMouseClicked(e -> toggleSeatSelection(seatId, seatRegion));
        }

        return seatPane;
    }

    private void toggleSeatSelection(String seatId, Region seatRegion) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seatRegion.getStyleClass().setAll("seat-available");
        } else {
            selectedSeats.add(seatId);
            seatRegion.getStyleClass().setAll("seat-selected");
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