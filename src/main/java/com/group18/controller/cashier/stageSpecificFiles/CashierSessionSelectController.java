package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.Pos;
import com.group18.model.Movie;
import com.group18.model.MovieSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class CashierSessionSelectController {
    @FXML private ImageView moviePosterView;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieGenresLabel;
    @FXML private DatePicker datePicker;
    @FXML private GridPane sessionsGrid;

    private Movie selectedMovie;
    private MovieSession selectedSession;
    private List<MovieSession> availableSessions = new ArrayList<>();

    private CashierController cashierController;

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    @FXML
    private void initialize() {
        // Set minimum date to today
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Disable past dates and dates more than 30 days in the future
                setDisable(empty || date.compareTo(today) < 0 ||
                        date.compareTo(today.plusDays(30)) > 0);
            }
        });
    }

    public void setMovie(Movie movie) {
        this.selectedMovie = movie;
        updateMovieDetails();
    }

    private void updateMovieDetails() {
        if (selectedMovie != null) {
            moviePosterView.setImage(new Image(selectedMovie.getPosterPath()));
            movieTitleLabel.setText(selectedMovie.getTitle());
            movieGenresLabel.setText(selectedMovie.getGenre());
        }
    }

    @FXML
    private void handleShowSessions() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            loadAvailableSessions(selectedDate);
            displaySessions();
        }
    }

    private void loadAvailableSessions(LocalDate date) {
        // TODO: Load from database
        // For now, creating sample sessions
        availableSessions.clear();

        // Sample sessions for Hall A
        availableSessions.add(new MovieSession("Hall_A", LocalTime.of(10, 0), 16));
        availableSessions.add(new MovieSession("Hall_A", LocalTime.of(12, 0), 14));
        availableSessions.add(new MovieSession("Hall_A", LocalTime.of(14, 0), 16));

        // Sample sessions for Hall B
        availableSessions.add(new MovieSession("Hall_B", LocalTime.of(11, 0), 45));
        availableSessions.add(new MovieSession("Hall_B", LocalTime.of(13, 0), 48));
        availableSessions.add(new MovieSession("Hall_B", LocalTime.of(15, 0), 40));
    }

    private void displaySessions() {
        // Clear previous sessions
        sessionsGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );

        int row = 1;
        for (MovieSession session : availableSessions) {
            // Time
            Label timeLabel = new Label(session.getTime().toString());
            sessionsGrid.add(timeLabel, 0, row);

            // Hall
            Label hallLabel = new Label(session.getHall());
            sessionsGrid.add(hallLabel, 1, row);

            // Available Seats
            Label seatsLabel = new Label(session.getAvailableSeats() + " seats");
            sessionsGrid.add(seatsLabel, 2, row);

            // Select Button
            Button selectButton = new Button("Select");
            selectButton.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white;");
            selectButton.setOnAction(e -> handleSessionSelection(session));

            // Disable if no seats available
            selectButton.setDisable(session.getAvailableSeats() == 0);

            HBox buttonBox = new HBox(selectButton);
            buttonBox.setAlignment(Pos.CENTER);
            sessionsGrid.add(buttonBox, 3, row);

            row++;
        }
    }

    private void handleSessionSelection(MovieSession session) {
        selectedSession = session;

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Session");
        confirm.setHeaderText("Selected Session Details");
        confirm.setContentText(String.format(
                "Movie: %s%nDate: %s%nTime: %s%nHall: %s%nAvailable Seats: %d",
                selectedMovie.getTitle(),
                datePicker.getValue(),
                session.getTime(),
                session.getHall(),
                session.getAvailableSeats()
        ));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Proceed to next stage (seat selection)
            // This will be handled by the main controller
        }
    }

    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }
}