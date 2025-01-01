package com.group18.controller.admin;

import com.group18.dao.ScheduleDAO;
import com.group18.dao.MovieDAO;
import com.group18.model.Schedule;
import com.group18.model.Movie;
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
import javafx.util.StringConverter;
import javafx.scene.control.ListCell;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

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

    @FXML
    private void initialize() {
        scheduleDAO = new ScheduleDAO();
        movieDAO = new MovieDAO();

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

                    if (scheduleDAO.isScheduleExists(selectedMovie.getMovieId(), hallId, date, time)) {
                        showAlert("Error", "A schedule for this movie, hall, date, and time already exists.");
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

    private void validateInputs(Node createButton, ComboBox<Movie> movieComboBox,
                                ComboBox<String> hallComboBox, DatePicker datePicker,
                                ComboBox<LocalTime> timeComboBox) {
        boolean isValid = movieComboBox.getValue() != null &&
                hallComboBox.getValue() != null &&
                datePicker.getValue() != null &&
                timeComboBox.getValue() != null;
        createButton.setDisable(!isValid);
    }

    private boolean isValidDate(LocalDate date) {
        return date != null &&
                date.getYear() == selectedMonth.getYear() &&
                date.getMonth() == selectedMonth.getMonth() &&
                !date.isBefore(LocalDate.now());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getStatusText(int availableSeats) {
        if (availableSeats == 0) return "Full";
        if (availableSeats < 5) return "Filling Up";
        return "Available";
    }

    private void loadSchedules() {
        List<Schedule> schedules = scheduleDAO.getAllSchedules();

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
}