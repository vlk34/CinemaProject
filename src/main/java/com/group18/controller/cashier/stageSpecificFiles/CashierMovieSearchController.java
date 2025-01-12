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

/**
 * This class represents the controller responsible for handling movie search functionality
 * in the cashier's interface of the application. It allows searching movies based on their title and/or genres.
 * It manages the dynamic interactions for movie search and display, such as filtering, displaying results,
 * and initiating detailed views of movies.
 *
 * Responsibilities include:
 * - Initializing movie-related UI components and data
 * - Performing search operations based on user input (title and genre selection)
 * - Displaying search results dynamically in a grid layout
 * - Providing animations for better visual interaction with movie items
 * - Handling genre selection and title input for movie filtering
 * - Integrating with other components like `CashierController` and `MovieDAO`
 */
public class CashierMovieSearchController {
    /**
     * A text field used for searching movies by their title.
     * This field is part of the user interface managed by the CashierMovieSearchController.
     * The input provided in this field is used to filter the movie search results.
     */
    @FXML private TextField titleSearchField;
    /**
     * Represents a dropdown menu button in the CashierMovieSearchController
     * that provides genre selection functionality for searching movies.
     * The button allows users to choose specific genres to filter movie search results.
     * This MenuButton is populated dynamically with genre options.
     */
    @FXML private MenuButton genreMenuButton;
    /**
     * Represents the container for displaying movie information dynamically as a
     * grid of movie elements. The FlowPane layout allows for arranging child nodes
     * in a flow that adjusts automatically based on the window's size and content.
     * Used primarily in the CashierMovieSearchController to present search results
     * and movie listings to the user.
     */
    @FXML private FlowPane movieGrid;
    /**
     * A Label UI component used to display the count of search results.
     * It is managed by the CashierMovieSearchController class and updated as search results change.
     */
    @FXML private Label resultCountLabel;

    /**
     * Represents a reference to the CashierController instance used for handling
     * functionality or operations related to cashier management within the
     * CashierMovieSearchController.
     *
     * This variable facilitates interaction between the movie search functionality
     * and cashier-specific operations, ensuring integration between the two
     * components of the system.
     */
    private CashierController cashierController;
    /**
     * A reference to the MovieDAO instance used for managing and retrieving movie data.
     * This variable allows the controller to execute database operations such as
     * fetching movies, updating movie information, adding new movies, or removing movies.
     * It acts as the data access layer between the controller and the underlying database.
     */
    private MovieDAO movieDAO;
    /**
     * Holds an observable list of Movie objects. This list is used as the primary data source
     * for managing and displaying a collection of movies within the application. Changes made
     * to the list are automatically reflected in UI components bound to it, enabling real-time
     * updates.
     */
    private ObservableList<Movie> allMovies;
    /**
     * Represents the currently selected movie in the cashier movie search controller.
     * This variable holds the Movie object that is actively chosen or being interacted with
     * from the available search results or displayed movie list.
     */
    private Movie selectedMovie;

    /**
     * An array of predefined movie genre options available for selection
     * in the genre filter menu.
     * This array is utilized to populate the filtering options in the
     * associated UI element, enabling users to search for movies by genre.
     */
    private static final String[] GENRE_OPTIONS = {
            "Action", "Comedy", "Drama", "Horror",
            "Science Fiction", "Fantasy"
    };

    /**
     * Initializes the controller immediately after its root element has been processed.
     * This method sets up required data structures, event listeners, and UI components.
     *
     * Functionality includes:
     * - Initialization of the {@code MovieDAO} object for database access.
     * - Creation of an observable list to manage movie data.
     * - Configuration of genre menu items by delegating to {@code setupGenreMenuItems()}.
     * - Adding a text change listener to the title search field to trigger searches
     *   when the input changes, excluding single-character inputs.
     * - Loading the initial set of movies by calling {@code loadMovies()}.
     */
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

    /**
     * Configures the genre menu items by populating the genre dropdown with
     * checkable menu items corresponding to predefined genre options.
     * Each menu item is assigned an action listener to update the genre button text
     * and perform a search whenever a genre is selected or deselected.
     *
     * The genre menu is cleared of existing items before adding the new items.
     */
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

    /**
     * Updates the checkbox state of the "All Genres" option in the genre menu.
     *
     * This method evaluates whether all individual genre checkboxes within the
     * `genreMenuButton` are selected. If all the checkboxes are selected, the
     * "All Genres" checkbox (assumed to be the first item in the menu) will also
     * be marked as selected. Otherwise, the "All Genres" checkbox is unchecked.
     *
     * The method loops through all items in the `genreMenuButton` except the first
     * one, which represents the "All Genres" option, to determine their selected
     * state.
     */
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

    /**
     * Updates the text of the genre menu button based on the selected genres.
     * If no genres are selected, the button's text will display "Select Genres".
     * Otherwise, the button's text will list the selected genres, separated by commas.
     *
     * This method relies on the {@link #getSelectedGenres()} method to retrieve
     * the list of currently selected genres from the genre menu.
     */
    private void updateGenreButtonText() {
        List<String> selectedGenres = getSelectedGenres();

        if (selectedGenres.isEmpty()) {
            genreMenuButton.setText("Select Genres");
        } else {
            genreMenuButton.setText(String.join(", ", selectedGenres));
        }
    }

    /**
     * Retrieves a list of selected genres from the genre menu button.
     * Checks each menu item to determine if it is a checked menu item and
     * if it is selected, adds its text to the list of selected genres.
     *
     * @return a list of strings representing the currently selected genres
     */
    private List<String> getSelectedGenres() {
        List<String> selectedGenres = new ArrayList<>();

        for (MenuItem item : genreMenuButton.getItems()) {
            if (item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected()) {
                selectedGenres.add(item.getText());
            }
        }

        return selectedGenres;
    }

    /**
     * Sets the CashierController instance for this controller.
     *
     * @param controller the CashierController to be associated with this instance
     */
    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    /**
     * Handles the search action triggered by the associated user interface element.
     * This method invokes the searching process by calling the performSearch method.
     * It is bound to the UI using the @FXML annotation.
     */
    @FXML
    private void handleSearch() {
        performSearch();
    }

    /**
     * Filters and displays a list of movies based on the search text and selected genres.
     *
     * This method processes the input from the title search field and normalizes the search
     * text by converting various forms of the letter 'i' to a consistent representation.
     * It then retrieves the selected genres from the genre menu. Using this information,
     * movies are filtered from the complete list of available movies based on whether their
     * titles match the normalized search text and their genres match the selected genres.
     * The filtered movies are then displayed using the appropriate UI method.
     *
     * The normalization is performed to ensure that variations of the letter 'i'
     * (uppercase, lowercase, or diacritical forms) do not affect the search matching.
     *
     * The filtering logic ensures that:
     * - If the search text is empty, no title filtering is applied.
     * - If no genres are selected, no genre filtering is applied.
     * - A movie is included if it matches the search text and belongs to at least one of the selected genres.
     *
     * This method internally calls `getSelectedGenres` to retrieve the selected genres
     * and `displayMovies` to update the UI with the filtered results.
     */
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

    /**
     * Loads all movies from the data source, updates the internal list of movies,
     * and displays them in the user interface.
     *
     * This method retrieves a list of all movies using the `movieDAO.getAllMovies` method,
     * updates the `allMovies` observable list with the retrieved data, and subsequently
     * displays these movies in the relevant UI component by invoking `displayMovies`.
     *
     * It ensures that the movie grid and other UI elements stay synchronized with the
     * persisted data store.
     */
    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        allMovies.setAll(movies);
        displayMovies(movies);
    }

    /**
     * Displays a list of movies in the movie grid and updates the result count label.
     * This method clears any existing content in the movie grid, sets the result count
     * label to the number of movies provided, and then creates and adds a visual movie
     * card for each movie in the list.
     *
     * @param movies The list of Movie objects to be displayed. Each movie will be
     *               represented as a visual card in the movie grid.
     */
    private void displayMovies(List<Movie> movies) {
        movieGrid.getChildren().clear();
        resultCountLabel.setText(movies.size() + " movies found");

        for (Movie movie : movies) {
            movieGrid.getChildren().add(createMovieCard(movie));
        }
    }

    /**
     * Creates a movie card UI component that displays movie details including the poster, title, genre, duration,
     * and a button to view additional details. The card includes various styling and animations for enhanced user experience.
     *
     * @param movie the movie object containing the data to populate the card, such as title, duration, genres, and poster data
     * @return a VBox containing the structured and styled components representing the movie card
     */
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

    /**
     * Sets a default poster image to the provided ImageView. If an error occurs while loading the
     * default image, the ImageView will be left empty.
     *
     * @param posterView the ImageView that will display the default poster image if no valid image
     *                   is available or if an error occurs loading the specified poster.
     */
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

    /**
     * Displays the details of a given movie in a modal dialog.
     * The movie details are loaded into a specific modal layout, where the user can
     * view the information and interact with the dialog. If the user confirms (e.g., presses 'OK'),
     * the selected movie is set and any associated controller actions are executed.
     *
     * @param movie The movie whose details are to be displayed.
     */
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

    /**
     * Displays an error dialog with the provided title and content message.
     *
     * @param title  the title of the error dialog
     * @param content  the content message describing the error
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}