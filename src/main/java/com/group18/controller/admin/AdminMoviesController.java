package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
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
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> filterGenreComboBox;
    @FXML private TextField searchField;

    private MovieDAO movieDAO;
    private Movie selectedMovie;
    private String currentPosterPath;

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();

        // Initialize ComboBox items
        genreComboBox.getItems().addAll("Action", "Comedy", "Drama", "Horror", "Science Fiction");
        filterGenreComboBox.getItems().addAll("All Genres", "Action", "Comedy", "Drama", "Horror", "Science Fiction");

        // Initialize filterGenreComboBox with "All Genres"
        filterGenreComboBox.setValue("All Genres");

        setupTableColumns();
        setupGenreComboBoxes();
        loadMovies();

        moviesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMovie = newSelection;
                populateMovieDetails(newSelection);
            }
        });
    }

    private void setupGenreComboBoxes() {
        // Add listener to filter genre ComboBox
        filterGenreComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Implement filtering logic here
            filterMovies();
        });
    }

    private void filterMovies() {
        String selectedGenre = filterGenreComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        ObservableList<Movie> allMovies = FXCollections.observableArrayList(movieDAO.getAllMovies());
        ObservableList<Movie> filteredMovies = allMovies.filtered(movie -> {
            boolean matchesGenre = selectedGenre == null || selectedGenre.equals("All Genres") ||
                    selectedGenre.equals(movie.getGenre());
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    movie.getTitle().toLowerCase().contains(searchText);
            return matchesGenre && matchesSearch;
        });

        moviesTable.setItems(filteredMovies);
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setStyle("-fx-alignment: CENTER;");

        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreColumn.setStyle("-fx-alignment: CENTER;");

        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setStyle("-fx-alignment: CENTER;");

        setupActionsColumn();
    }

    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        ObservableList<Movie> movieList = FXCollections.observableArrayList(movies);
        moviesTable.setItems(movieList);
    }

    private void populateMovieDetails(Movie movie) {
        titleField.setText(movie.getTitle());
        genreComboBox.setValue(movie.getGenre());
        summaryField.setText(movie.getSummary());
        durationField.setText(String.valueOf(movie.getDuration()));

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                // Load image using getResource since we're using a relative path
                Image image = new Image(getClass().getResourceAsStream(movie.getPosterPath()));
                posterImageView.setImage(image);
                currentPosterPath = movie.getPosterPath();
            } catch (Exception e) {
                e.printStackTrace();
                // Optionally set a default image if loading fails
                posterImageView.setImage(null);
            }
        } else {
            posterImageView.setImage(null);
            currentPosterPath = null;
        }
    }

    @FXML
    private void handleSelectPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.setInitialDirectory(new File("src/main/resources/images/movies")); // Set initial directory
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Just use the selected file directly
                posterImageView.setImage(new Image(selectedFile.toURI().toString()));

                // Store the relative path
                currentPosterPath = "/images/movies/" + selectedFile.getName();
            } catch (Exception e) {
                showAlert("Error", "Failed to select poster: " + e.getMessage());
            }
        }
    }

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

    @FXML
    private void handleUpdateMovie() {
        if (selectedMovie == null) {
            showAlert("Error", "No movie selected");
            return;
        }

        try {
            selectedMovie.setTitle(titleField.getText().trim());
            selectedMovie.setGenre(genreComboBox.getValue());
            selectedMovie.setSummary(summaryField.getText().trim());
            selectedMovie.setDuration(Integer.parseInt(durationField.getText().trim()));
            selectedMovie.setPosterPath(currentPosterPath);

            if (movieDAO.updateMovie(selectedMovie)) {
                loadMovies();
                clearFields();
                showAlert("Success", "Movie updated successfully");
            } else {
                showAlert("Error", "Failed to update movie");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid duration format");
        }
    }

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        titleField.clear();
        genreComboBox.setValue(null);
        summaryField.clear();
        durationField.clear();
        posterImageView.setImage(null);
        selectedMovie = null;
        currentPosterPath = null;
    }
}