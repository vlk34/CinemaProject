package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.nio.file.Files;

/**
 * Controller class for the "Add Movie" dialog. This class manages the user interface and
 * business logic for adding a new movie to the system. It provides fields for entering details
 * about the movie, such as title, summary, and genres, as well as selecting a poster image.
 *
 * The dialog ensures that all required inputs are validated before allowing the movie to be
 * added. Once valid data is provided, the movie details are saved using the MovieDAO class.
 */
public class AddMovieDialogController {
    /**
     * TextField for entering the title of the movie.
     * This field is mandatory for adding a movie and is validated to ensure
     * it is not empty during the movie submission process.
     * It is linked to the user interface via the FXML annotation.
     */
    @FXML private TextField titleField;
    /**
     * A TextArea component used for inputting or displaying the summary of a movie.
     * This field captures a brief description or overview of the movie.
     * It is also used in validation processes to ensure its content is not empty.
     */
    @FXML private TextArea summaryField;
    /**
     * A TextField used to display and manage the duration of a movie.
     * The duration is preset to a fixed value and is non-editable.
     * This field is part of the user inputs in the Add Movie Dialog.
     */
    @FXML private TextField durationField;
    /**
     * The ImageView component used for displaying the movie poster.
     * This field is linked to the corresponding UI element in the FXML layout.
     * It is utilized to present the visual representation of the movie poster
     * selected by the user. The image displayed in this component is updated
     * when the user selects a new poster via the "Select Poster" button.
     */
    @FXML private ImageView posterImageView;
    /**
     * A button within the AddMovieDialogController class that allows users to select a poster image
     * for the movie being added or updated. When clicked, it triggers an event to open a file chooser
     * or similar interface to upload an image, which is then used as the movie's poster.
     */
    @FXML private Button selectPosterButton;
    /**
     * Represents the button control used to trigger the addition of a new movie to the system.
     * This button is part of the user interface and is associated with user interactions
     * within the Add Movie dialog. It will enable or disable dynamically based on the
     * validation of input fields, ensuring that the required conditions are met before
     * allowing the user to proceed with adding a movie.
     *
     * It is linked to the `validateInputs` method to determine its enabled state
     * and can invoke associated handlers like `handleAddMovie` upon being pressed.
     */
    @FXML private Button addMovieButton;
    /**
     * The cancelButton serves as a control element in the user interface for dismissing the dialog.
     * When triggered, it closes the Add Movie dialog without saving any changes.
     * This button is typically used to exit the dialog, discarding any input or progress.
     */
    @FXML private Button cancelButton;
    /**
     * The container for displaying the list of genres.
     * This FlowPane dynamically arranges the genre elements (e.g., checkboxes, labels)
     * depending on the available genres, allowing users to select or view genres.
     * Typically used within the context of a movie addition/editing dialog.
     */
    @FXML private FlowPane genreContainer;

    /**
     * Represents the Data Access Object (DAO) responsible for managing
     * movie-related CRUD operations (Create, Read, Update, Delete).
     *
     * This instance facilitates interaction between the AddMovieDialogController
     * class and the MovieDAO class, allowing the controller to add, update,
     * or fetch movie data.
     */
    private MovieDAO movieDAO;
    /**
     * Holds the binary data for the currently selected poster image.
     * This data is typically represented as a byte array and is used
     * when adding or updating a movie's poster.
     *
     * The variable is utilized in operations such as validating inputs
     * and ensuring a poster image is provided before enabling certain actions.
     */
    private byte[] currentPosterData;
    /**
     * Represents the primary dialog stage for the AddMovieDialogController.
     * This stage defines the window in which the dialog and its related UI components
     * are displayed to the user.
     */
    private Stage dialogStage;
    /**
     * Represents the set of genres selected by the user for a movie.
     * This variable is used to store and manage the chosen genres in the dialog for adding a new movie.
     * It is validated to ensure at least one genre is selected before allowing the movie to be added.
     */
    private Set<String> selectedGenres;

    /**
     * Initializes the AddMovieDialogController by setting up the necessary UI components, event handlers,
     * and validation mechanisms. This method is automatically called after the FXML file has been loaded.
     *
     * Responsibilities include:
     * - Initializing `MovieDAO` for handling database operations.
     * - Creating genre checkboxes dynamically and managing their selection state.
     * - Setting default states for UI components, such as disabling the "Add Movie" button initially.
     * - Setting up input validation for movie details to ensure valid data entry.
     * - Configuring event handlers for controlling actions, such as selecting a poster, adding a movie,
     *   and closing the dialog.
     */
    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        selectedGenres = new HashSet<>();

        // Create checkboxes for genres
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Science Fiction", "Fantasy"};
        for (String genre : genres) {
            CheckBox checkBox = new CheckBox(genre);
            checkBox.setPadding(new Insets(0, 10, 0, 0));
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
                validateInputs();
            });
            genreContainer.getChildren().add(checkBox);
        }

        // Disable add movie button initially
        addMovieButton.setDisable(true);

        // Setup validation listeners
        setupValidation();

        selectPosterButton.setOnAction(event -> handleSelectPoster());
        addMovieButton.setOnAction(event -> handleAddMovie());
        cancelButton.setOnAction(event -> dialogStage.close());
    }

    /**
     * Sets up input field validation for the Add Movie Dialog.
     * This method adds listeners to the text properties of the title and summary fields to trigger
     * validation logic whenever their content changes. It also initializes and locks the value of the
     * duration field to 120 minutes.
     *
     * The validation logic ensures that the add movie button is only enabled when all input fields
     * meet the required criteria, which is managed by the validateInputs method.
     *
     * Functionality:
     * - Listens for changes in the title field and triggers input validation.
     * - Listens for changes in the summary field and triggers input validation.
     * - Sets the duration field to a constant value of 120 and disables user input for this field.
     */
    private void setupValidation() {
        // Title validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        // Summary validation
        summaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        durationField.setText("120");
        durationField.setEditable(false);
    }

    /**
     * Validates the input fields in the Add Movie dialog. This method ensures that
     * all required fields for adding a movie are properly filled before enabling the
     * "Add Movie" button. Validation checks include:
     * <ul>
     * - The title field is not empty.
     * - At least one genre is selected.
     * - The summary field is not empty.
     * - The duration is fixed at 120 and is always valid.
     * - A valid poster image has been selected (not null and contains data).
     * </ul>
     * Based on the validation status of these fields, the "Add Movie" button
     * is enabled or disabled.
     */
    private void validateInputs() {
        boolean isTitleValid = !titleField.getText().trim().isEmpty();
        boolean isGenreValid = !selectedGenres.isEmpty();
        boolean isSummaryValid = !summaryField.getText().trim().isEmpty();
        boolean isDurationValid = true; // Always true since it's fixed at 120

        boolean isPosterValid = currentPosterData != null && currentPosterData.length > 0;

        addMovieButton.setDisable(!(isTitleValid && isGenreValid &&
                isSummaryValid && isDurationValid && isPosterValid));
    }

    /**
     * Handles the action of adding a movie to the data source.
     * Retrieves user input from the form fields, creates a new Movie object,
     * sets its properties, and attempts to save it via the movieDAO.
     * If successful, displays a success message and closes the dialog.
     * Otherwise, displays an error message.
     *
     * This method also handles unexpected exceptions by showing an error message to the user.
     */
    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String summary = summaryField.getText().trim();
            int duration = 120;

            Movie newMovie = new Movie();
            newMovie.setTitle(title);
            newMovie.setGenres(selectedGenres);
            newMovie.setSummary(summary);
            newMovie.setPosterData(currentPosterData);
            newMovie.setDuration(duration);

            if (movieDAO.addMovie(newMovie)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added successfully.");
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add movie.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the action of selecting a movie poster via a file chooser dialog.
     * Opens a FileChooser allowing the user to select an image file with supported formats
     * (PNG, JPG, JPEG, GIF). If a valid image file is selected, it processes the file, validates
     * its content, updates the poster preview in the UI, and stores the image data.
     *
     * If the image file is invalid or an error occurs during processing, an error alert is displayed.
     *
     * Additionally, this method triggers input validation for the movie details form to enable or
     * disable the "Add Movie" button based on the form's current state.
     *
     * Exceptions are handled gracefully, ensuring the user is notified in case of failure.
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

                // If valid, update UI and store the data
                Image image = new Image(new ByteArrayInputStream(imageData));
                posterImageView.setImage(image);
                currentPosterData = imageData;

                // Validate inputs after successful poster selection
                validateInputs();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image file: " + e.getMessage());
            }
        }
    }

    /**
     * Displays an alert dialog with the specified type, title, and content.
     *
     * @param type   the type of alert to display (e.g., INFORMATION, WARNING, ERROR)
     * @param title  the title of the alert dialog
     * @param content the content message to display in the alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets the stage for the dialog window.
     *
     * @param dialogStage the {@code Stage} object representing the dialog's stage. This is used to manage
     *                    the lifecycle and properties of the dialog window (e.g., title, modality, owner).
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}