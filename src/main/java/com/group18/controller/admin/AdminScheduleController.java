package com.group18.controller.admin;

import com.group18.dao.ScheduleDAO;
import com.group18.dao.MovieDAO;
import com.group18.model.Schedule;
import com.group18.model.Movie;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.scene.control.ListCell;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Controller for managing movie schedules in the admin interface.
 * Handles displaying schedules, creating new schedules, and deleting schedules.
 */
public class AdminScheduleController {
    @FXML
    private DatePicker monthPicker;

    @FXML
    private TableView<Schedule> hallATable;

    @FXML
    private TableColumn<Schedule, LocalDate> dateAColumn;

    @FXML
    private TableColumn<Schedule, LocalTime> timeAColumn;

    @FXML
    private TableColumn<Schedule, String> movieAColumn;

    @FXML
    private TableColumn<Schedule, String> statusAColumn;

    @FXML
    private TableColumn<Schedule, Void> actionsAColumn;

    @FXML
    private TableView<Schedule> hallBTable;

    @FXML
    private TableColumn<Schedule, LocalDate> dateBColumn;

    @FXML
    private TableColumn<Schedule, LocalTime> timeBColumn;

    @FXML
    private TableColumn<Schedule, String> movieBColumn;

    @FXML
    private TableColumn<Schedule, String> statusBColumn;

    @FXML
    private TableColumn<Schedule, Void> actionsBColumn;

    @FXML
    private Button createScheduleButton;

    private ScheduleDAO scheduleDAO;
    private MovieDAO movieDAO;

    private LocalDate selectedMonth;

    /**
     * Initializes the controller. Sets up the initial state, table columns,
     * and event listeners for the schedule creation and month selection.
     */
    @FXML
    private void initialize() {
        scheduleDAO = new ScheduleDAO();
        movieDAO = new MovieDAO();

        setupButtonHoverAnimation(createScheduleButton);
        setupTableColumns();
        createScheduleButton.setDisable(true);

        // Set initial month to current month
        monthPicker.setValue(LocalDate.now());
        selectedMonth = LocalDate.now().withDayOfMonth(1);
        filterSchedulesByMonth(selectedMonth);

        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMonth = newVal.withDayOfMonth(1);
                filterSchedulesByMonth(selectedMonth);
                createScheduleButton.setDisable(false);
            } else {
                createScheduleButton.setDisable(true);
            }
        });

        createScheduleButton.setOnAction(event -> showScheduleCreationDialog());
    }

    /**
     * Sets up the columns for displaying schedule information in the tables for both halls.
     */
    private void setupTableColumns() {
        // Setup Hall A columns
        dateAColumn.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        dateAColumn.setStyle("-fx-alignment: CENTER;");

        timeAColumn.setCellValueFactory(new PropertyValueFactory<>("sessionTime"));
        timeAColumn.setStyle("-fx-alignment: CENTER;");

        movieAColumn.setCellValueFactory(cellData -> {
            Movie movie = movieDAO.findById(cellData.getValue().getMovieId());
            return new SimpleStringProperty(movie != null ? movie.getTitle() : "Unknown");
        });
        movieAColumn.setStyle("-fx-alignment: CENTER;");

        statusAColumn.setCellValueFactory(cellData -> {
            int availableSeats = scheduleDAO.getAvailableSeatsCount(cellData.getValue().getScheduleId());
            return new SimpleStringProperty(getStatusText(availableSeats));
        });
        statusAColumn.setStyle("-fx-alignment: CENTER;");

        // Setup Hall B columns
        dateBColumn.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        dateBColumn.setStyle("-fx-alignment: CENTER;");

        timeBColumn.setCellValueFactory(new PropertyValueFactory<>("sessionTime"));
        timeBColumn.setStyle("-fx-alignment: CENTER;");

        movieBColumn.setCellValueFactory(cellData -> {
            Movie movie = movieDAO.findById(cellData.getValue().getMovieId());
            return new SimpleStringProperty(movie != null ? movie.getTitle() : "Unknown");
        });
        movieBColumn.setStyle("-fx-alignment: CENTER;");

        statusBColumn.setCellValueFactory(cellData -> {
            int availableSeats = scheduleDAO.getAvailableSeatsCount(cellData.getValue().getScheduleId());
            return new SimpleStringProperty(getStatusText(availableSeats));
        });
        statusBColumn.setStyle("-fx-alignment: CENTER;");

        // Add to setupTableColumns() method
        setupActionsColumn(actionsAColumn);
        setupActionsColumn(actionsBColumn);
    }

    /**
     * Sets up the actions column, adding delete functionality to the schedule table.
     *
     * @param column the column to set up
     */
    private void setupActionsColumn(TableColumn<Schedule, Void> column) {
        column.setCellFactory(col -> new TableCell<Schedule, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    handleDeleteSchedule(schedule);
                });

                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteButton.setPadding(new Insets(1, 4, 1, 4));
                setAlignment(Pos.CENTER);
                deleteButton.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                    setAlignment(Pos.CENTER);
                    setContentDisplay(ContentDisplay.CENTER);
                }
            }
        });
        column.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Handles the deletion of a schedule, confirming with the user before deleting.
     *
     * @param schedule the schedule to be deleted
     */
    private void handleDeleteSchedule(Schedule schedule) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Schedule");
        confirmation.setHeaderText("Delete schedule for " + movieDAO.findById(schedule.getMovieId()).getTitle());
        confirmation.setContentText("Are you sure you want to delete this schedule?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            if (scheduleDAO.deleteSchedule(schedule.getScheduleId())) {
                filterSchedulesByMonth(selectedMonth);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Delete Failed");
                error.setContentText("Could not delete the schedule. It may have existing bookings.");
                error.show();
            }
        }
    }

    /**
     * Displays a dialog for creating a new schedule.
     */
    private void showScheduleCreationDialog() {
        // Create the custom dialog
        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Create New Schedule");
        dialog.setHeaderText("Enter schedule details");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the grid pane for the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form elements
        ComboBox<Movie> movieComboBox = new ComboBox<>();
        ComboBox<String> hallComboBox = new ComboBox<>();
        DatePicker datePicker = new DatePicker();
        ComboBox<LocalTime> timeComboBox = new ComboBox<>();

        // Populate the combo boxes
        movieComboBox.setItems(FXCollections.observableArrayList(movieDAO.getAllMovies()));
        movieComboBox.setPromptText("Select Movie");

        // Set movie display and conversion
        movieComboBox.setCellFactory(param -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                setText(movie == null ? null : movie.getTitle());
            }
        });
        movieComboBox.setConverter(new StringConverter<Movie>() {
            @Override
            public String toString(Movie movie) {
                return movie == null ? null : movie.getTitle();
            }

            @Override
            public Movie fromString(String title) {
                return null;
            }
        });

        hallComboBox.setItems(FXCollections.observableArrayList("Hall A", "Hall B"));
        hallComboBox.setPromptText("Select Hall");

        // Set date picker to not pre-select a date
        datePicker.setPromptText("Select Date");
        datePicker.setValue(null);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !isValidDate(date));
            }
        });

        ObservableList<LocalTime> times = FXCollections.observableArrayList(
                LocalTime.of(10, 0), LocalTime.of(12, 0), LocalTime.of(14, 0),
                LocalTime.of(16, 0), LocalTime.of(18, 0), LocalTime.of(20, 0)
        );
        timeComboBox.setItems(times);
        timeComboBox.setPromptText("Select Time");

        // Add form elements to grid
        grid.add(new Label("Movie:"), 0, 0);
        grid.add(movieComboBox, 1, 0);
        grid.add(new Label("Hall:"), 0, 1);
        grid.add(hallComboBox, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Time:"), 0, 3);
        grid.add(timeComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable create button depending on whether a movie was selected
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Add listeners to enable/disable create button
        movieComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(createButton, movieComboBox, hallComboBox, datePicker, timeComboBox));
        hallComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(createButton, movieComboBox, hallComboBox, datePicker, timeComboBox));
        datePicker.valueProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(createButton, movieComboBox, hallComboBox, datePicker, timeComboBox));
        timeComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(createButton, movieComboBox, hallComboBox, datePicker, timeComboBox));

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    Movie selectedMovie = movieComboBox.getValue();
                    int hallId = hallComboBox.getValue().equals("Hall A") ? 1 : 2;
                    LocalDate date = datePicker.getValue();
                    LocalTime time = timeComboBox.getValue();

                    Schedule newSchedule = new Schedule(
                            selectedMovie.getMovieId(),
                            hallId,
                            date,
                            time
                    );

                    if (scheduleDAO.isScheduleExists(hallId, date, time)) {
                        showAlert("Error", "A schedule for this hall, date, and time already exists.");
                        return null;
                    }

                    if (scheduleDAO.createSchedule(newSchedule)) {
                        return newSchedule;
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to create schedule: " + e.getMessage());
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Schedule> result = dialog.showAndWait();
        result.ifPresent(schedule -> {
            filterSchedulesByMonth(selectedMonth);
            showAlert("Success", "Schedule created successfully!");
        });
    }

    /**
     * Validates the input fields for schedule creation, enabling or disabling the create button.
     *
     * @param createButton the button to enable/disable
     * @param movieComboBox the movie selection combo box
     * @param hallComboBox the hall selection combo box
     * @param datePicker the date picker
     * @param timeComboBox the time selection combo box
     */
    private void validateInputs(Node createButton, ComboBox<Movie> movieComboBox,
                                ComboBox<String> hallComboBox, DatePicker datePicker,
                                ComboBox<LocalTime> timeComboBox) {
        boolean isValid = movieComboBox.getValue() != null &&
                hallComboBox.getValue() != null &&
                datePicker.getValue() != null &&
                timeComboBox.getValue() != null;
        createButton.setDisable(!isValid);
    }

    /**
     * Checks if a given date is valid for the selected month.
     *
     * @param date the date to check
     * @return true if the date is valid, false otherwise
     */
    private boolean isValidDate(LocalDate date) {
        return date != null &&
                date.getYear() == selectedMonth.getYear() &&
                date.getMonth() == selectedMonth.getMonth() &&
                !date.isBefore(LocalDate.now());
    }

    /**
     * Displays an alert dialog with a given title and content.
     *
     * @param title the title of the alert
     * @param content the content text of the alert
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Returns the status text based on the available seats.
     *
     * @param availableSeats the number of available seats
     * @return a string representing the schedule's status
     */
    private String getStatusText(int availableSeats) {
        if (availableSeats == 0) return "Full";
        if (availableSeats < 5) return "Filling Up";
        return "Available";
    }

    /**
     * Filters the schedules based on the selected month and updates the table views for Hall A and Hall B.
     * The schedules are divided into two categories based on the hall (1 for Hall A and 2 for Hall B).
     *
     * @param selectedMonth The selected month to filter the schedules by.
     */
    private void filterSchedulesByMonth(LocalDate selectedMonth) {
        List<Schedule> schedules = scheduleDAO.getSchedulesByMonth(selectedMonth);

        ObservableList<Schedule> hallASchedules = FXCollections.observableArrayList(
                schedules.stream()
                        .filter(s -> s.getHallId() == 1)
                        .collect(Collectors.toList())
        );

        ObservableList<Schedule> hallBSchedules = FXCollections.observableArrayList(
                schedules.stream()
                        .filter(s -> s.getHallId() == 2)
                        .collect(Collectors.toList())
        );

        hallATable.setItems(hallASchedules);
        hallBTable.setItems(hallBSchedules);
    }

    /**
     * Sets up a hover animation effect for the given button. The button will scale down when pressed and
     * scale back to its original size when released or the mouse exits.
     *
     * @param button The button to apply the hover animation to.
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