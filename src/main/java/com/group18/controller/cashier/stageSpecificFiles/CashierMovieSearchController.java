package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.modals.CashierMovieDetailsController;
import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.util.Duration;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CashierMovieSearchController {
    @FXML private TextField titleSearchField;
    @FXML private MenuButton genreMenuButton;
    @FXML private FlowPane movieGrid;
    @FXML private Label resultCountLabel;

    private CashierController cashierController;
    private MovieDAO movieDAO;
    private ObservableList<Movie> allMovies;
    private Movie selectedMovie;

    private static final String[] GENRE_OPTIONS = {
            "Action", "Comedy", "Drama", "Horror",
            "Science Fiction", "Fantasy"
    };

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        allMovies = FXCollections.observableArrayList();

        // Setup genre menu items
        setupGenreMenuItems();

        titleSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() != 1) {
                performSearch();
            }
        });

        loadMovies();
    }

    private void setupGenreMenuItems() {
        // Clear existing items
        genreMenuButton.getItems().clear();

        // Add individual genre checkboxes
        for (String genre : GENRE_OPTIONS) {
            CheckMenuItem genreItem = new CheckMenuItem(genre);
            genreItem.setOnAction(event -> {
                updateGenreButtonText();
                performSearch();
            });
            genreMenuButton.getItems().add(genreItem);
        }
    }

    private void updateAllGenresCheckbox() {
        CheckMenuItem allGenresItem = (CheckMenuItem) genreMenuButton.getItems().get(0);

        // Check if all genre checkboxes are selected
        boolean allSelected = true;
        for (int i = 1; i < genreMenuButton.getItems().size(); i++) {
            CheckMenuItem item = (CheckMenuItem) genreMenuButton.getItems().get(i);
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }

        // Update "All Genres" checkbox
        allGenresItem.setSelected(allSelected);
    }

    private void updateGenreButtonText() {
        List<String> selectedGenres = getSelectedGenres();

        if (selectedGenres.isEmpty()) {
            genreMenuButton.setText("Select Genres");
        } else {
            genreMenuButton.setText(String.join(", ", selectedGenres));
        }
    }

    private List<String> getSelectedGenres() {
        List<String> selectedGenres = new ArrayList<>();

        for (MenuItem item : genreMenuButton.getItems()) {
            if (item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected()) {
                selectedGenres.add(item.getText());
            }
        }

        return selectedGenres;
    }

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    @FXML
    private void handleSearch() {
        performSearch();
    }

    private void performSearch() {
        // Normalize search text - convert all variations of i/I to a single character
        String searchText = titleSearchField.getText()
                .replace('i', 'i')
                .replace('I', 'i')
                .replace('ı', 'i')
                .replace('İ', 'i')
                .toLowerCase();

        List<String> selectedGenres = getSelectedGenres();

        List<Movie> filteredMovies = allMovies.stream()
                .filter(movie -> {
                    // Normalize movie title - convert all variations of i/I to a single character
                    String movieTitle = movie.getTitle()
                            .replace('i', 'i')
                            .replace('I', 'i')
                            .replace('ı', 'i')
                            .replace('İ', 'i')
                            .toLowerCase();

                    boolean titleMatch = (searchText.isEmpty() || movieTitle.contains(searchText));
                    boolean genreMatch = selectedGenres.isEmpty() ||
                            selectedGenres.stream().anyMatch(movie.getGenres()::contains);

                    return titleMatch && genreMatch;
                })
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
        card.setStyle("-fx-background-color: white; " +
                "-fx-padding: 12; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #f0f0f0; " +
                "-fx-border-radius: 8;");

        // Create a property for shadow animation
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.05));
        shadow.setRadius(16);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        // Animation for mouse enter
        card.setOnMouseEntered(e -> {
            Timeline enterAnimation = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(shadow.radiusProperty(), 20),
                            new KeyValue(shadow.colorProperty(), javafx.scene.paint.Color.rgb(0, 0, 0, 0.08)),
                            new KeyValue(shadow.offsetYProperty(), 6)
                    )
            );
            enterAnimation.play();
        });

        // Animation for mouse exit
        card.setOnMouseExited(e -> {
            Timeline exitAnimation = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(shadow.radiusProperty(), 16),
                            new KeyValue(shadow.colorProperty(), javafx.scene.paint.Color.rgb(0, 0, 0, 0.05)),
                            new KeyValue(shadow.offsetYProperty(), 4)
                    )
            );
            exitAnimation.play();
        });

        ImageView posterView = new ImageView();

        // Handle poster image using byte array data
        if (movie.getPosterData() != null && movie.getPosterData().length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(movie.getPosterData()));
                if (!image.isError()) {
                    posterView.setImage(image);
                } else {
                    // Set default image if poster data is invalid
                    setDefaultPoster(posterView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultPoster(posterView);
            }
        } else {
            setDefaultPoster(posterView);
        }

        posterView.setFitWidth(180);
        posterView.setFitHeight(270);

        // Create a clipping rectangle with rounded corners
        Rectangle clip = new Rectangle(180, 270);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        clip.setX(0);
        clip.setY(0);
        posterView.setClip(clip);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        titleLabel.setWrapText(true);

        Label genreLabel = new Label(movie.getGenresAsString());
        genreLabel.setStyle("-fx-text-fill: #666666;");
        genreLabel.setWrapText(true);

        Label durationLabel = new Label(movie.getDuration() + " minutes");
        durationLabel.setStyle("-fx-text-fill: #666666;");

        Button detailsButton = new Button("View Details");
        detailsButton.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white;");
        detailsButton.setOnAction(e -> showMovieDetails(movie));

        // Add hover animation for the button
        detailsButton.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), detailsButton);
            scaleUp.setToX(1.03); // Slightly increase the button size
            scaleUp.setToY(1.03);
            scaleUp.play();
        });

        detailsButton.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), detailsButton);
            scaleDown.setToX(1.0); // Reset the size to original
            scaleDown.setToY(1.0);
            scaleDown.play();
        });

        card.getChildren().addAll(posterView, titleLabel, genreLabel, durationLabel, detailsButton);
        return card;
    }

    private void setDefaultPoster(ImageView posterView) {
        try {
            byte[] defaultImageData = getClass().getResourceAsStream("/images/movies/dark_knight.jpg").readAllBytes();
            Image defaultImage = new Image(new ByteArrayInputStream(defaultImageData));
            posterView.setImage(defaultImage);
        } catch (IOException e) {
            e.printStackTrace();
            // If even default image fails, leave the ImageView empty
            posterView.setImage(null);
        }
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