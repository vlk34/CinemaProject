package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
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
import javafx.scene.layout.VBox;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String currentPosterPath;
    private Set<String> selectedGenres;
    private static final String[] AVAILABLE_GENRES = {"Action", "Comedy", "Drama", "Horror", "Science Fiction", "Fantasy"};
    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        selectedGenres = new HashSet<>();

        setupGenreCheckboxes();
        setupFilterGenreMenuButton();

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

    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterMovies();
        });
    }

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

    private void setupGenreComboBoxes() {
        filterGenreComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterMovies();
        });
    }

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

    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        ObservableList<Movie> movieList = FXCollections.observableArrayList(movies);
        moviesTable.setItems(movieList);
    }

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

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {

                Image image = null;

                // 1. Try loading from file system directly
                try {
                    File posterFile = new File("src/main/resources" + movie.getPosterPath());
                    if (posterFile.exists()) {
                        image = new Image(posterFile.toURI().toString());
                    }
                } catch (Exception e) {
                    System.err.println("File system loading failed: " + e.getMessage());
                }

                // 2. Try resource stream
                if (image == null || image.isError()) {
                    try {
                        String fileName = movie.getPosterPath().substring(movie.getPosterPath().lastIndexOf('/') + 1);
                        String fullResourcePath = "/images/movies/" + fileName;

                        image = new Image(getClass().getResourceAsStream(fullResourcePath));

                        if (image.isError()) {
                            System.err.println("Resource stream image is error");
                        }
                    } catch (Exception e) {
                        System.err.println("Resource stream failed: " + e.getMessage());
                    }
                }

                // 3. Absolute file path
                if (image == null || image.isError()) {
                    try {
                        File absoluteFile = new File(movie.getPosterPath());
                        if (absoluteFile.exists()) {
                            image = new Image(absoluteFile.toURI().toString());
                        }
                    } catch (Exception e) {
                        System.err.println("Absolute path loading failed: " + e.getMessage());
                    }
                }

                // Set the image if successfully loaded
                if (image != null && !image.isError()) {
                    posterImageView.setImage(image);
                    currentPosterPath = movie.getPosterPath();
                } else {
                    System.err.println("Failed to load image from all sources");
                    posterImageView.setImage(null);
                    currentPosterPath = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterImageView.setImage(null);
                currentPosterPath = null;
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create movies directory if it doesn't exist
                Path moviesDir = Paths.get("src/main/resources/images/movies");
                Files.createDirectories(moviesDir);

                // Use the original filename
                String fileName = selectedFile.getName();
                Path targetPath = moviesDir.resolve(fileName);

                // Check if file already exists
                if (Files.exists(targetPath)) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("File Already Exists");
                    confirmAlert.setHeaderText("A file with the name '" + fileName + "' already exists.");
                    confirmAlert.setContentText("Do you want to replace the existing file?");

                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        // User chose not to replace, so return
                        return;
                    }
                }

                // Use Platform.runLater to move file copying to background thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Copy file
                        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                        // Set the relative path for database storage
                        currentPosterPath = "/images/movies/" + fileName;

                        // Update UI on JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> {
                            // Load and display the copied image
                            Image image = new Image(targetPath.toUri().toString());
                            posterImageView.setImage(image);
                        });
                    } catch (Exception e) {
                        // Show error on JavaFX Application Thread
                        javafx.application.Platform.runLater(() ->
                                showAlert("Error", "Failed to copy image file: " + e.getMessage())
                        );
                    }
                });

            } catch (Exception e) {
                showAlert("Error", "Failed to prepare image file: " + e.getMessage());
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

        selectedMovie.setTitle(titleField.getText().trim());
        selectedMovie.setGenres(selectedGenres);
        selectedMovie.setSummary(summaryField.getText().trim());
        selectedMovie.setDuration(120);  // Hardcoded to 120 minutes
        selectedMovie.setPosterPath(currentPosterPath);

        if (movieDAO.updateMovie(selectedMovie)) {
            loadMovies();
            clearFields();
            showAlert("Success", "Movie updated successfully");
        } else {
            showAlert("Error", "Failed to update movie");
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
        summaryField.clear();
        durationField.clear();
        posterImageView.setImage(null);
        selectedMovie = null;
        currentPosterPath = null;
        selectedGenres.clear();

        // Uncheck all genre checkboxes
        genreContainer.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        });
    }
}