package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.dao.ScheduleDAO;
import com.group18.model.Movie;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for managing movies in the admin interface.
 * Allows adding, updating, deleting movies, and filtering them based on genre and search terms.
 */
public class AdminMoviesController {
    @FXML private TextField titleField;
    @FXML private TextArea summaryField;
    @FXML private TextField durationField;
    @FXML private ImageView posterImageView;
    @FXML private Button addMovieButton;
    @FXML private Button updateMovieButton;
    @FXML private Button selectPosterButton;
    @FXML private TableView<Movie> moviesTable;
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, String> genreColumn;
    @FXML private TableColumn<Movie, Integer> durationColumn;
    @FXML private TableColumn<Movie, Void> actionsColumn;
    @FXML private FlowPane genreContainer;
    @FXML private MenuButton filterGenreMenuButton;
    @FXML private ComboBox<String> filterGenreComboBox;
    @FXML private TextField searchField;

    private MovieDAO movieDAO;
    private Movie selectedMovie;
    private byte[] currentPosterData;
    private Set<String> selectedGenres;
    private static final String[] AVAILABLE_GENRES = {"Action", "Comedy", "Drama", "Horror", "Science Fiction", "Fantasy"};

    /**
     * Initializes the controller by setting up genre checkboxes, filtering menu, table columns, and loading movies.
     */
    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        selectedGenres = new HashSet<>();

        setupGenreCheckboxes();
        setupFilterGenreMenuButton();
        setupButtonHoverAnimation(addMovieButton);
        setupButtonHoverAnimation(updateMovieButton);
        // Make duration field non-editable
        durationField.setEditable(false);

        setupTableColumns();
        setupSearchField();
        loadMovies();

        moviesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMovie = newSelection;
                populateMovieDetails(newSelection);
            }
        });
    }

    /**
     * Sets up the genre filter menu button with available genres.
     */
    private void setupFilterGenreMenuButton() {
        // Clear existing items
        filterGenreMenuButton.getItems().clear();

        // Create menu items with listeners
        for (String genre : AVAILABLE_GENRES) {
            CheckMenuItem genreItem = new CheckMenuItem(genre);
            genreItem.setOnAction(event -> {
                updateFilterGenreButtonText();
                filterMovies();
            });
            filterGenreMenuButton.getItems().add(genreItem);
        }
    }

    /**
     * Updates the text on the filter genre button based on selected genres.
     */
    private void updateFilterGenreButtonText() {
        List<String> selectedFilterGenres = filterGenreMenuButton.getItems().stream()
                .filter(item -> item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected())
                .map(MenuItem::getText)
                .collect(Collectors.toList());

        if (selectedFilterGenres.isEmpty()) {
            filterGenreMenuButton.setText("Select Genres");
        } else {
            filterGenreMenuButton.setText(String.join(", ", selectedFilterGenres));
        }
    }

    /**
     * Sets up the search field to filter movies based on the search query.
     */
    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterMovies();
        });
    }

    /**
     * Sets up the genre checkboxes for available genres.
     */
    private void setupGenreCheckboxes() {
        genreContainer.getChildren().clear();

        for (String genre : AVAILABLE_GENRES) {
            CheckBox checkBox = new CheckBox(genre);
            checkBox.setPadding(new Insets(0, 10, 0, 0));  // Add some spacing between checkboxes
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
            });
            genreContainer.getChildren().add(checkBox);
        }
    }

    /**
     * Filters the movie list based on selected genres and search query.
     */
    private void filterMovies() {
        String searchText = searchField.getText().toLowerCase();
        List<String> selectedFilterGenres = filterGenreMenuButton.getItems().stream()
                .filter(item -> item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected())
                .map(MenuItem::getText)
                .collect(Collectors.toList());

        ObservableList<Movie> allMovies = FXCollections.observableArrayList(movieDAO.getAllMovies());
        ObservableList<Movie> filteredMovies = allMovies.filtered(movie -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    movie.getTitle().toLowerCase().contains(searchText);

            boolean matchesGenre = selectedFilterGenres.isEmpty() ||
                    selectedFilterGenres.stream().anyMatch(movie.getGenres()::contains);

            return matchesSearch && matchesGenre;
        });

        moviesTable.setItems(filteredMovies);
    }

    /**
     * Sets up the table columns for displaying movie data.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setStyle("-fx-alignment: CENTER;");

        genreColumn.setCellValueFactory(cellData -> {
            // Join genres with comma and space
            String genres = cellData.getValue().getGenres().stream()
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(genres);
        });
        genreColumn.setStyle("-fx-alignment: CENTER;");

        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setStyle("-fx-alignment: CENTER;");

        setupActionsColumn();
    }

    /**
     * Loads movies from the database into the table.
     */
    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        ObservableList<Movie> movieList = FXCollections.observableArrayList(movies);
        moviesTable.setItems(movieList);
    }

    /**
     * Populates movie details into the UI fields for the selected movie.
     *
     * @param movie the selected movie
     */
    private void populateMovieDetails(Movie movie) {
        titleField.setText(movie.getTitle());
        summaryField.setText(movie.getSummary());
        durationField.setText("120");
        durationField.setEditable(false);

        // Reset all checkboxes
        genreContainer.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                checkBox.setSelected(movie.getGenres().contains(checkBox.getText()));
            }
        });

        selectedGenres = new HashSet<>(movie.getGenres());

        // Load poster image from byte array
        byte[] posterData = movie.getPosterData();
        if (posterData != null && posterData.length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(posterData));
                if (!image.isError()) {
                    posterImageView.setImage(image);
                    currentPosterData = posterData;
                } else {
                    System.err.println("Error loading image from byte array");
                    posterImageView.setImage(null);
                    currentPosterData = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterImageView.setImage(null);
                currentPosterData = null;
            }
        } else {
            posterImageView.setImage(null);
            currentPosterData = null;
        }
    }

    /**
     * Opens a file chooser to select a movie poster image.
     */
    @FXML
    private void handleSelectPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Read file into byte array
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());

                // Validate image data
                try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                    Image testImage = new Image(bis);
                    if (testImage.isError()) {
                        throw new IOException("Invalid image data");
                    }
                }

                // Update UI
                Image image = new Image(new ByteArrayInputStream(imageData));
                posterImageView.setImage(image);
                currentPosterData = imageData;

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image file: " + e.getMessage());
            }
        }
    }

    /**
     * Opens the dialog for adding a new movie.
     */
    @FXML
    private void handleAddMovie() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AddMovieDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addMovieButton.getScene().getWindow());
            dialogStage.setTitle("Add New Movie");

            AddMovieDialogController dialogController = loader.getController();
            dialogController.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            loadMovies();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open add movie dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Updates the selected movie with the new details.
     */
    @FXML
    private void handleUpdateMovie() {
        if (selectedMovie == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No movie selected");
            return;
        }

        // First check if movie has schedules
        ScheduleDAO scheduleDAO = new ScheduleDAO();
        if (scheduleDAO.hasSchedules(selectedMovie.getMovieId())) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Cannot update movie because it has existing schedules");
            // Clear the selection and fields after showing the error
            clearFields();
            selectedMovie = null;
            moviesTable.getSelectionModel().clearSelection();
            return;
        }


        String newTitle = titleField.getText().trim();
        // If the title is different from current title, check if it exists
        if (!newTitle.equalsIgnoreCase(selectedMovie.getTitle()) &&
                movieDAO.existsByTitleIgnoreCase(newTitle)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Another movie with this title already exists");
            return;
        }

        selectedMovie.setTitle(titleField.getText().trim());
        selectedMovie.setGenres(selectedGenres);
        selectedMovie.setSummary(summaryField.getText().trim());
        selectedMovie.setDuration(120);
        selectedMovie.setPosterData(currentPosterData);

        if (movieDAO.updateMovie(selectedMovie)) {
            loadMovies();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Movie updated successfully");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update movie");
        }
    }

    /**
     * Sets up the actions column in the movies table for handling delete actions.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Movie, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    handleDeleteMovie(movie);
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

        actionsColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Handles deleting a inserted Movie object by using movieDAO.removeMovie()
     */
    private void handleDeleteMovie(Movie movie) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Movie");
        confirmation.setHeaderText("Delete " + movie.getTitle());
        confirmation.setContentText("Are you sure you want to delete this movie?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            if (movieDAO.removeMovie(movie.getMovieId())) {
                moviesTable.getItems().remove(movie);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Delete Failed");
                error.setContentText("Could not delete the movie. It may be scheduled for showing.");
                error.show();
            }
        }
    }

    /**
     * Displays an alert with the specified alert type, title, and content.
     *
     * @param type    The type of the alert (e.g., ERROR, INFORMATION, etc.).
     * @param title   The title of the alert window.
     * @param content The content of the alert message.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Clears the form fields, resets selected genres, and unchecks all genre checkboxes.
     */
    private void clearFields() {
        titleField.clear();
        summaryField.clear();
        durationField.clear();
        posterImageView.setImage(null);
        selectedMovie = null;
        currentPosterData = null;
        selectedGenres.clear();

        // Uncheck all genre checkboxes
        genreContainer.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        });
    }

    /**
     * Sets up hover animation for the given button, where the button shrinks slightly when pressed
     * and returns to its original size when released or the mouse exits the button area.
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