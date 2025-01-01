package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.modals.CashierMovieDetailsController;
import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CashierMovieSearchController {
    @FXML private TextField titleSearchField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private FlowPane movieGrid;
    @FXML private Label resultCountLabel;

    private CashierController cashierController;
    private MovieDAO movieDAO;
    private ObservableList<Movie> allMovies;
    private Movie selectedMovie;

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        allMovies = FXCollections.observableArrayList();

        List<String> genres = Arrays.asList(
                "All Genres",
                "Action",
                "Comedy",
                "Drama",
                "Horror",
                "Science Fiction"
        );
        genreComboBox.setItems(FXCollections.observableArrayList(genres));
        genreComboBox.setValue("All Genres");

        titleSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() != 1) {
                performSearch();
            }
        });

        genreComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                performSearch();
            }
        });

        loadMovies();
    }

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    @FXML
    private void handleSearch() {
        performSearch();
    }

    private void performSearch() {
        String searchText = titleSearchField.getText().toLowerCase();
        String selectedGenre = genreComboBox.getValue();

        List<Movie> filteredMovies = allMovies.stream()
                .filter(movie ->
                        (searchText.isEmpty() || movie.getTitle().toLowerCase().contains(searchText)) &&
                                (selectedGenre.equals("All Genres") || movie.getGenre().contains(selectedGenre)))
                .collect(Collectors.toList());

        displayMovies(filteredMovies);
    }

    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        allMovies.setAll(movies);
        displayMovies(movies);
    }

    private void displayMovies(List<Movie> movies) {
        movieGrid.getChildren().clear();
        resultCountLabel.setText(movies.size() + " movies found");

        for (Movie movie : movies) {
            movieGrid.getChildren().add(createMovieCard(movie));
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");

        ImageView posterView = new ImageView();
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                File posterFile = new File(movie.getPosterPath());
                if (posterFile.exists()) {
                    // Use the same logic as populateMovieDetails
                    posterView.setImage(new Image(posterFile.toURI().toString()));
                } else {
                    System.err.println("File not found: " + movie.getPosterPath());
                    posterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
            }
        } else {
            posterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
        }
        posterView.setFitWidth(180);
        posterView.setFitHeight(270);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        titleLabel.setWrapText(true);

        Label genreLabel = new Label(movie.getGenre());
        genreLabel.setStyle("-fx-text-fill: #666666;");
        genreLabel.setWrapText(true);

        Label durationLabel = new Label(movie.getDuration() + " minutes");
        durationLabel.setStyle("-fx-text-fill: #666666;");

        Button detailsButton = new Button("View Details");
        detailsButton.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white;");
        detailsButton.setOnAction(e -> showMovieDetails(movie));

        card.getChildren().addAll(posterView, titleLabel, genreLabel, durationLabel, detailsButton);
        return card;
    }

    private void showMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/cashier/modals/CashierMovieDetails.fxml")
            );
            DialogPane dialogPane = loader.load();

            CashierMovieDetailsController controller = loader.getController();
            controller.setMovie(movie);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Movie Details");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedMovie = movie;
                if (cashierController != null) {
                    cashierController.navigateWithData(selectedMovie);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load movie details.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}