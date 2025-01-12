package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.dao.PriceDAO;
import com.group18.dao.ProductDAO;
import com.group18.model.OrderItem;
import com.group18.model.Product;
import com.group18.model.ShoppingCart;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * The CashierCustomerDetailsController class manages the user interface and logic
 * for handling customer details in a cashier context. It facilitates age-based
 * discounts, customer detail validation, and ticket management, integrating with
 * the shopping cart, pricing, and product databases.
 *
 * This class is responsible for:
 * - Initializing and configuring UI components, such as input fields, buttons,
 *   and tabs for managing various product categories.
 * - Handling customer details validation, including names and eligibility for
 *   age-based discounts.
 * - Managing discounted ticket counts and applying discounts based on customer
 *   inputs and eligibility.
 * - Persisting and restoring customer details across sessions for convenience.
 *
 * Key Responsibilities:
 * - Provide a seamless user interface for cashiers to input and validate customer details.
 * - Apply age-based discounts and update the shopping cart with the appropriate ticket pricing.
 * - Ensure input validation rules for names are enforceable and persist customer data where needed.
 * - Handle UI state updates dynamically based on user interactions and system constraints.
 *
 * Dependencies:
 * - The class relies on DAOs for managing product and pricing data integration.
 * - It utilizes JavaFX for rich UI interactions, including animations and dynamic styling.
 *
 * Components Managed by the Class:
 * - Customer input fields for first name, last name, and ticket counts.
 * - Product selection tabs and containers such as beverages, biscuits, and toys.
 * - Buttons for discount application and ticket count adjustments.
 * - Validation and persistence of customer details.
 */
public class CashierCustomerDetailsController {
    /**
     * Represents a text field component in the UI where the user can input
     * the first name of a customer. This field is part of the customer
     * details section in the CashierCustomerDetailsController.
     *
     * This field is connected to the FXML file using the @FXML annotation
     * for proper initialization and interaction within the JavaFX application.
     * It is utilized to capture and process customer information related to
     * their first name.
     */
    @FXML private TextField firstNameField;
    /**
     * The lastNameField is a UI component bound to a TextField in the associated FXML.
     * It is used for capturing or displaying the last name of a customer within
     * the CashierCustomerDetailsController.
     *
     * This field is likely tied to customer input validation or interaction with
     * other components for form handling and submission processes.
     */
    @FXML private TextField lastNameField;
    /**
     * Represents a label in the user interface that displays the count of tickets
     * that have been purchased with an age-based discount. This label is updated
     * dynamically based on the customer's eligibility for discounts and their ticket
     * selections.
     */
    @FXML private Label ageDiscountedTicketsCount;
    /**
     * Represents a button in the user interface that decreases the count of discounted tickets
     * when interacted with. It is linked to the functionality for managing ticket discounts in
     * the cashier customer's details view.
     */
    @FXML private Button minusDiscounted;
    /**
     * A button control used within the CashierCustomerDetailsController to increase
     * the count of discounted tickets. This button is typically enabled or disabled
     * based on certain conditions, such as the customer's eligibility for discounts
     * or the availability of tickets.
     */
    @FXML private Button plusDiscounted;
    /**
     * Represents the button element in the user interface for applying discounts.
     * This button is part of the `CashierCustomerDetailsController` class and is
     * tied to functionalities for managing discounts in the application.
     * The button is associated with user actions and triggers related event handlers
     * to apply discounts to selected items or tickets during the cashiering process.
     */
    @FXML private Button applyDiscountsButton;
    /**
     * Represents an ImageView component in the UI that serves as an informational icon.
     * This is typically used to display visual cues or additional context within the
     * user interface of the CashierCustomerDetailsController.
     * It may also be associated with tooltips or other related functionalities.
     *
     * This field is linked with the corresponding FXML file using the @FXML annotation.
     */
    @FXML private ImageView infoIcon;

    /**
     * Represents a button in the UI used to verify the age of the customer.
     * This control is part of the customer details interface and is likely
     * involved in ensuring age-related requirements for processing discounts
     * or validating customer eligibility for certain products or services.
     *
     * It is intended to be interactive and will trigger a specific event handler
     * or validation logic within the controller when clicked.
     */
    @FXML private Button verifyAgeButton;
    /**
     * Represents a TabPane UI component used to display and manage product categories within the cashier
     * customer details interface. This component allows navigation between different product tabs, such
     * as beverages, biscuits, and toys, facilitating the selection and interaction with products.
     *
     * This TabPane is part of the user interface and is dynamically populated and styled depending on
     * the loaded product categories. It is typically used in conjunction with related containers,
     * such as FlowPanes, for displaying product cards in each category.
     *
     * The associated functionality includes tab selection, styling updates, and animations during tab
     * transitions, ensuring a smooth user experience.
     */
    @FXML private TabPane productsTabPane;
    /**
     * Represents a FlowPane container within the UI that holds beverage-related items.
     * This container is dynamically populated with beverage products based on the available inventory.
     * Used to display and manage beverage options within the cashier interface.
     */
    @FXML private FlowPane beveragesContainer;
    /**
     * A FlowPane container used to display and organize biscuit products
     * in the UI. Functions as part of the products tab in the customer
     * details view.
     *
     * This container is typically populated dynamically based on
     * the available biscuit items, using the product-loading methods
     * defined within the CashierCustomerDetailsController.
     *
     * Associated with the FXML file for defining the layout of the
     * cashier's customer details interface.
     */
    @FXML private FlowPane biscuitsContainer;
    /**
     * Represents a FlowPane used to display toy products within the customer details interface.
     * This container is populated dynamically with toy-related product cards,
     * providing a layout for visual product representation and user interaction.
     * It is linked to the FXML file through the @FXML annotation for proper UI binding.
     */
    @FXML private FlowPane toysContainer;
    /**
     * Reference to the {@code CashierController} instance used for managing cashier operations
     * within the context of the customer details and product interactions.
     *
     * This variable is utilized to coordinate cashier-related actions such as handling cart updates,
     * processing transactions, and managing interactions that require communication with the
     * cashier's interface or logic.
     */
    private CashierController cashierController;
    /**
     * Represents a collection of seat identifiers selected by the customer.
     * Each seat is uniquely identified by a string, typically corresponding
     * to a specific seating configuration in a theater or venue.
     * This variable is utilized for tracking the customer's seat selections
     * during the ticket purchasing process.
     */
    private Set<String> selectedSeats;
    /**
     * Represents the Data Access Object (DAO) responsible for price-related operations.
     * This variable is used to manage ticket pricing, discount rates, and related database interactions
     * in the application logic of the cashier controller.
     */
    private PriceDAO priceDAO;
    /**
     * Represents the Data Access Object (DAO) for managing product-related data operations.
     * This variable facilitates interaction with the underlying data storage or repository
     * for retrieving and manipulating product data.
     */
    private ProductDAO productDAO;
    /**
     * Holds the current state of the shopping cart for managing items selected
     * by the customer during a transaction. The cart is used to track the
     * products, tickets, and their respective quantities while also enabling
     * updates, additions, and removals of items as needed.
     */
    private ShoppingCart cart;
    /**
     * Indicates whether a discount is applicable to the current transaction or operation.
     *
     * This variable is used to track if discounts should be applied, which
     * can influence various components of the system, such as ticket pricing or product costs.
     * Its state is managed as part of the discount control and validation logic.
     */
    private boolean isDiscountApplicable = false;
    /**
     * Represents the validation status of customer details.
     * This variable indicates whether the customer has
     * provided valid details and completed necessary validation steps.
     * It is used within the context of verifying customer information
     * and enabling subsequent actions related to checkout or discounts.
     */
    private boolean customerDetailsValidated = false;

    /**
     * Represents the count of discounted tickets associated with a customer's order or session.
     * This variable is used to track the number of tickets that have been marked as eligible
     * for a discount, based on applicable conditions such as customer age or promotions.
     */
    private int discountedTickets = 0;
    /**
     * Represents the total number of seats available or selected in the current context.
     * This variable is used to track and manage seat-related operations, such as
     * determining availability, seat allocation, or processing bookings.
     */
    private int totalSeats = 0;

    // Persistent customer details storage
    private static class CustomerDetails {
        String firstName;
        String lastName;
        int discountedTickets;
        boolean validated;

        CustomerDetails(String firstName, String lastName, int discountedTickets, boolean validated) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.discountedTickets = discountedTickets;
            this.validated = validated;
        }
    }

    /**
     * A static variable that holds the persistent details of a customer.
     * This is used to store customer-related data such as first name,
     * last name, the count of discounted tickets, and validation status
     * across different sessions or executions. It allows the system to
     * maintain continuity in data for a customer even after certain
     * actions or resets within the application.
     */
    private static CustomerDetails persistentCustomerDetails = null;

    /**
     * Initializes the controller and its associated components. This method sets up
     * essential data elements, user interface elements, and animations for managing
     * customer details in the cashier's interface.
     *
     * The following operations are performed during initialization:
     * - Instantiates DAO objects for managing product and pricing data.
     * - Retrieves the singleton instance of the shopping cart.
     * - Configures validation rules for customer details input.
     * - Sets up discount controls and tooltip display behavior.
     * - Adds visual styling and animations to the tab selection mechanism.
     *
     * Ensures that the user interface reflects a clean state upon loading and restores
     * any relevant persistent customer details.
     */
    @FXML
    private void initialize() {
        priceDAO = new PriceDAO();
        productDAO = new ProductDAO();
        cart = ShoppingCart.getInstance();

        setupCustomerDetailsValidation();
        setupDiscountControls();
        setupTooltip();
        setupTabSlidingAnimation();
        setupTabSelectionStyling();

        resetUI();
        restorePersistentDetails();
    }

    /**
     * Configures and initializes the tooltip on the UI for providing additional information
     * about age-based discounts.
     *
     * This method sets up a tooltip with a custom style and text which explains discount eligibility
     * for customers based on their age. It also applies hover animations for smooth visual effects
     * on the tooltip container and increases the size of the associated icon for better visibility.
     *
     * Implementation details:
     * - A hover effect is added to the container (`HBox`) that contains the information icon.
     *   This is achieved by animating the opacity of the container using JavaFX `Timeline`.
     * - A `Tooltip` is created with a specific style, text message, and configuration to enhance
     *   usability and presentation. The style includes features such as background color, padding,
     *   font size, rounded corners, and text-wrapping for longer messages.
     * - The tooltip is installed on the container to display information to the user.
     */
    private void setupTooltip() {
        // Find the HBox container
        HBox tooltipContainer = (HBox) infoIcon.getParent();

        // Use Timeline for smooth opacity animation
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(tooltipContainer.opacityProperty(), 0.6, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(150), new KeyValue(tooltipContainer.opacityProperty(), 1.0, Interpolator.EASE_OUT))
        );

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(tooltipContainer.opacityProperty(), 1.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(200), new KeyValue(tooltipContainer.opacityProperty(), 0.6, Interpolator.EASE_OUT))
        );

        tooltipContainer.setOnMouseEntered(e -> fadeIn.play());
        tooltipContainer.setOnMouseExited(e -> fadeOut.play());

        // Increase icon size
        infoIcon.setFitWidth(20);  // Slightly larger
        infoIcon.setFitHeight(20);

        Tooltip tooltip = new Tooltip("Customers under 18 or over 60 years old qualify for age-based discounts");
        tooltip.setShowDelay(Duration.millis(10));

        // Customize tooltip style
        tooltip.setStyle(
                "-fx-background-color: #f8f9fa;" +     // Light gray background
                        "-fx-text-fill: #4a4a4a;" +            // Softer dark gray text
                        "-fx-background-radius: 8px;" +        // Rounded corners
                        "-fx-padding: 8px;" +                  // More padding
                        "-fx-font-size: 12px;" +               // Slightly larger font
                        "-fx-max-width: 250px;" +              // Limit width
                        "-fx-wrap-text: true;"                 // Enable text wrapping
        );

        // Install tooltip on the container
        Tooltip.install(tooltipContainer, tooltip);
    }

    /**
     * Configures the controls associated with managing discounts for tickets.
     *
     * This method sets up the actions for decrementing and incrementing the count
     * of discounted tickets via designated buttons. It ensures that the appropriate
     * logic is executed to adjust the count of discounted tickets and updates the
     * state of buttons accordingly.
     *
     * The following behavior is implemented:
     * 1. Assigns event handlers to the decrement (-) and increment (+) buttons, allowing them
     *    to call the {@code adjustDiscountedTickets} method to modify the count of discounted tickets.
     * 2. Consumes the button action events to prevent unintended propagation.
     * 3. Disables focus traversal for both buttons to streamline user interaction.
     * 4. Updates the states of the buttons after initialization to ensure they reflect
     *    the current count of discounted tickets and other related constraints implemented
     *    in {@code updateButtonStates}.
     */
    private void setupDiscountControls() {
        minusDiscounted.setOnAction(e -> {
            adjustDiscountedTickets(false);
            e.consume();
        });
        plusDiscounted.setOnAction(e -> {
            adjustDiscountedTickets(true);
            e.consume();
        });

        minusDiscounted.setFocusTraversable(false);
        plusDiscounted.setFocusTraversable(false);

        updateButtonStates();
    }

    /**
     * Adjusts the count of discounted tickets based on the specified action
     * (increase or decrease). If the count is increased, it ensures that the count
     * does not exceed the total number of available seats. If decreased, it ensures
     * that the count does not drop below zero. Additionally, it updates ticket
     * counts and button states after adjustment.
     *
     * @param increase a boolean indicating whether the discounted ticket count
     * should be increased (true) or decreased (false)
     */
    private void adjustDiscountedTickets(boolean increase) {
        if (increase && discountedTickets < totalSeats) {
            discountedTickets++;
        } else if (!increase && discountedTickets > 0) {
            discountedTickets--;
        }

        updateTicketCounts();
        updateButtonStates();
    }

    /**
     * Updates the displayed count of discounted tickets in the user interface.
     * This method sets the text of the relevant UI label to the current number
     * of discounted tickets. It is typically called whenever changes to the
     * ticket counts affect the discounted tickets, ensuring the UI remains
     * consistent with the internal state.
     */
    private void updateTicketCounts() {
        ageDiscountedTicketsCount.setText(String.valueOf(discountedTickets));
    }

    /**
     * Updates the states of various buttons based on the current state of inputs and internal variables.
     *
     * This method performs the following actions:
     * - Enables or disables the 'minusDiscounted' button based on whether there are currently any discounted tickets.
     * - Enables or disables the 'plusDiscounted' button based on whether the number of discounted tickets
     *   has reached the total number of seats.
     * - Validates the input fields for customer details (first name and last name), ensuring they are not empty
     *   after trimming whitespace, and enables or disables the 'applyDiscountsButton' based on whether the
     *   customer details are valid and there are available seats.
     */
    private void updateButtonStates() {
        minusDiscounted.setDisable(discountedTickets == 0);
        plusDiscounted.setDisable(discountedTickets >= totalSeats);

        boolean detailsValid = !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty();
        applyDiscountsButton.setDisable(!detailsValid || totalSeats == 0);
    }

    /**
     * Handles the application of age-based discounts to customer tickets.
     *
     * This method performs the following tasks:
     * - Validates that the customer name details (first name and last name fields) are provided.
     *   If either field is empty, it displays an error alert to the user and halts further processing.
     * - Updates the tickets in the cart to apply the relevant discounts.
     * - Marks customer details as validated and persists the details for future use.
     * - Updates the action bar state to reflect any changes.
     * - Optionally, disables certain UI controls related to discounts after applying them (currently commented out).
     * - Displays a success alert to confirm that the discounts were applied successfully.
     */
    @FXML
    private void handleApplyDiscounts() {
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            showError("Invalid Input", "Please fill in customer name details.");
            return;
        }

        // Update tickets in cart
        updateTicketsInCart();
        customerDetailsValidated = true;
        savePersistentDetails();
        updateActionBarState();

        // Show success message
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText(null);
        success.setContentText("Age discounts have been applied successfully.");
        success.showAndWait();
    }

    /**
     * Checks if the customer has provided valid details and if their details
     * have been successfully validated.
     *
     * @return true if all required fields (first name and last name)
     *         are non-empty and customer details are validated, false otherwise.
     */
    public boolean hasValidDetailsAndVerification() {
        return !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                customerDetailsValidated;
    }

    /**
     * Restores previously saved customer details and updates the UI accordingly.
     * This method retrieves data from the `persistentCustomerDetails` object and
     * updates various fields and UI components within the controller.
     *
     * Key functionalities performed by this method:
     * 1. Updates the text fields for customer's first name and last name.
     * 2. Restores the number of discounted tickets, ensuring the value does not exceed
     *    the total available seats.
     * 3. Revalidates the customer's details and adjusts UI component states like
     *    ticket counts and button availability.
     * 4. If necessary controllers and selected seats are available, updates the
     *    tickets in the cart and updates the action bar state.
     *
     * Only executes if the `persistentCustomerDetails` object is not null.
     */
    private void restorePersistentDetails() {
        if (persistentCustomerDetails != null) {
            firstNameField.setText(persistentCustomerDetails.firstName);
            lastNameField.setText(persistentCustomerDetails.lastName);

            // Explicitly set discounted tickets and update UI
            this.discountedTickets = Math.min(persistentCustomerDetails.discountedTickets, totalSeats);

            // Update UI elements
            updateTicketCounts();
            updateButtonStates();

            customerDetailsValidated = persistentCustomerDetails.validated;

            // Only update tickets if controller is set and we have seats
            if (cashierController != null && selectedSeats != null && !selectedSeats.isEmpty()) {
                updateTicketsInCart();
                updateActionBarState();
            }
        }
    }

    /**
     * Saves validated customer details persistently by creating a {@code CustomerDetails} object
     * and initializing it with the provided data from input fields and application state.
     *
     * The method retrieves and trims the input values for the first and last name
     * from text fields. If both fields are non-empty, it constructs a new
     * {@code CustomerDetails} instance containing the first name, last name,
     * number of discounted tickets, and the customer validation status.
     *
     * The constructed {@code CustomerDetails} instance is then stored
     * in a persistent variable for reuse across other operations.
     *
     * Preconditions:
     * - The inputs in the {@code firstNameField} and {@code lastNameField} must be non-empty
     *   to proceed with saving the details.
     *
     * Postconditions:
     * - A new {@code CustomerDetails} instance is created and stored if the preconditions are met.
     * - No action is taken if either of the input fields is empty.
     */
    private void savePersistentDetails() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            persistentCustomerDetails = new CustomerDetails(
                    firstName,
                    lastName,
                    discountedTickets,
                    customerDetailsValidated
            );
        }
    }

    /**
     * Resets the user interface for customer details to its initial state.
     *
     * This method performs the following actions:
     * - Clears the fields for the customer's first and last name.
     * - Resets the count of discounted tickets to zero.
     * - Updates the visual representation of the ticket counts.
     * - Updates the enable/disable states of buttons based on the current inputs.
     * - Resets the validation flag for customer details to false.
     *
     * This method is typically called to prepare the user interface for a new customer
     * or when resetting a transaction.
     */
    public void resetUI() {
        firstNameField.clear();
        lastNameField.clear();
        discountedTickets = 0;
        updateTicketCounts();
        updateButtonStates();
        customerDetailsValidated = false;
    }

    /**
     * Resets the static field `persistentCustomerDetails` to null.
     * This method is typically used to clear any retained customer details
     * across instances or stages, ensuring a clean state for subsequent operations.
     */
    public static void clearPersistentDetailsStatic() {
        persistentCustomerDetails = null;
    }

    /**
     * Checks whether the customer's details have been successfully validated.
     *
     * @return true if the customer details are validated, false otherwise.
     */
    public boolean isCustomerDetailsValidated() {
        return customerDetailsValidated;
    }

    /**
     * Configures validation logic for customer details input fields such as
     * first name and last name. The method ensures proper input formatting
     * and triggers additional validation whenever the input changes.
     *
     * The validation includes:
     * - Restricting input to Unicode letters only for both the first name
     *   and last name fields.
     * - Ensuring the old value is restored if invalid input is detected.
     * - Invoking the `validateCustomerDetails` method to verify the state
     *   of all customer details and update relevant UI components accordingly.
     */
    private void setupCustomerDetailsValidation() {
        // First Name input validation using Unicode letter support
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only Unicode letters from any language
            if (!newValue.matches("\\p{L}*")) {
                firstNameField.setText(oldValue);
            }
            validateCustomerDetails();
        });

        // Last Name input validation using Unicode letter support
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only Unicode letters from any language
            if (!newValue.matches("\\p{L}*")) {
                lastNameField.setText(oldValue);
            }
            validateCustomerDetails();
        });
    }

    /**
     * Validates the customer details by ensuring that both the first name and last name fields are not empty.
     * If the fields contain valid inputs, the "Apply Discounts" button is enabled; otherwise, it remains disabled.
     * Also triggers an update to the action bar state if customer details have already been marked as validated.
     */
    private void validateCustomerDetails() {
        boolean detailsValid = !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty();
        applyDiscountsButton.setDisable(!detailsValid);

        if (customerDetailsValidated) {
            updateActionBarState();
        }
    }

    /**
     * Updates the state of the action bar by controlling the enabled or disabled state
     * of the action buttons based on the current stage of the cashier process.
     *
     * The method ensures that the action bar visibility and button states are updated
     * only when the `CashierController` and its associated `ActionBarController` are present.
     * It utilizes the `CashierController` to determine the current stage index and passes
     * this index to the `ActionBarController` to configure the appropriate button states.
     *
     * This method indirectly influences user interaction with the application by enabling
     * or disabling navigation through stages in the cashier process.
     */
    private void updateActionBarState() {
        if (cashierController != null && cashierController.getActionBarController() != null) {
            cashierController.getActionBarController().updateButtonStates(
                    cashierController.getCurrentStageIndex()
            );
        }
    }

    /**
     * Loads product categories and initializes their respective UI containers.
     *
     * This method leverages the `loadProductCategory` method to populate predefined UI
     * containers with products grouped by category. The method specifically handles
     * the loading of the "beverage", "biscuit", and "toy" product categories, populating
     * the associated containers, namely `beveragesContainer`, `biscuitsContainer`, and
     * `toysContainer`.
     *
     * This is a utility method that ensures product-related UI is set up correctly,
     * with each container displaying the relevant products for its designated category.
     */
    private void loadProducts() {
        loadProductCategory("beverage", beveragesContainer);
        loadProductCategory("biscuit", biscuitsContainer);
        loadProductCategory("toy", toysContainer);
    }

    /**
     * Loads products of a specified category into a given FlowPane container.
     * Retrieves a list of products of the specified category, clears the container,
     * and populates it with product cards generated from the retrieved products.
     *
     * @param category  the category of products to load into the container
     * @param container the FlowPane where the product cards will be displayed
     */
    private void loadProductCategory(String category, FlowPane container) {
        List<Product> products = productDAO.getProductsByType(category);
        container.getChildren().clear();

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            container.getChildren().add(productCard);
        }
    }

    /**
     * Creates and returns a product card containing product details, such as name, price, stock status, and
     * quantity controls, formatted within a VBox layout.
     *
     * The card features an image container, labels for the product name and price, a stock status indicator,
     * and buttons to adjust quantity. It also includes hover effects for aesthetic purposes.
     *
     * @param product The product object containing details such as name, price, stock, and image data
     *                to be displayed on the card.
     * @return A VBox instance representing the product card containing all elements and controls.
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // Handle image loading with BLOB data
        if (product.getImageData() != null && product.getImageData().length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(product.getImageData()));
                if (!image.isError()) {
                    imageView.setImage(image);
                } else {
                    setDefaultProductImage(imageView);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image for product: " + product.getProductName());
                e.printStackTrace();
                setDefaultProductImage(imageView);
            }
        } else {
            setDefaultProductImage(imageView);
        }

        imageContainer.getChildren().add(imageView);

        // Rest of the card creation code remains the same
        Label nameLabel = new Label(product.getProductName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        Label stockLabel = new Label();
        stockLabel.getStyleClass().addAll("stock-status");
        if (product.getStock() == 0) {
            stockLabel.getStyleClass().add("out-of-stock");
            stockLabel.setText("Out of Stock");
        } else if (product.getStock() < 10) {
            stockLabel.getStyleClass().add("low-stock");
            stockLabel.setText("Low Stock");
        } else {
            stockLabel.getStyleClass().add("in-stock");
            stockLabel.setText("In Stock");
        }

        // Quantity controls
        HBox quantityBox = new HBox(10);
        quantityBox.getStyleClass().add("quantity-control");
        quantityBox.setAlignment(Pos.CENTER);

        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("quantity-button");

        Label quantityLabel = new Label("0");
        quantityLabel.getStyleClass().add("quantity-label");

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("quantity-button");

        // Check if this product already exists in cart and set initial quantity
        Map<String, HBox> cartItems = cashierController.getCartController().getCartItems();
        int initialQuantity = 0;

        // Look for this product in cart items
        for (HBox cartItem : cartItems.values()) {
            VBox details = (VBox) cartItem.getChildren().get(0);
            Label itemNameLabel = (Label) details.getChildren().get(0);
            if (itemNameLabel.getText().equals(product.getProductName())) {
                Label quantityInCart = (Label) cartItem.getChildren().get(1);
                initialQuantity = Integer.parseInt(quantityInCart.getText().substring(1)); // Remove 'x' prefix
                break;
            }
        }

        // Set initial quantity and button states
        quantityLabel.setText(String.valueOf(initialQuantity));
        minusButton.setDisable(initialQuantity == 0);
        plusButton.setDisable(initialQuantity == product.getStock() || product.getStock() == 0);

        // Minus button handler
        minusButton.setOnAction(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            if (quantity > 0) {
                quantity--;
                quantityLabel.setText(String.valueOf(quantity));
                updateCartProduct(product, quantity);
                plusButton.setDisable(false);
            }
            if (quantity == 0) {
                minusButton.setDisable(true);
            }
        });

        // Plus button handler
        plusButton.setOnAction(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            if (quantity < product.getStock()) {
                quantity++;
                quantityLabel.setText(String.valueOf(quantity));
                updateCartProduct(product, quantity);
                minusButton.setDisable(false);
            }
            if (quantity == product.getStock()) {
                plusButton.setDisable(true);
            }
        });

        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);

        // Add all elements to card
        card.getChildren().addAll(
                imageContainer,
                nameLabel,
                priceLabel,
                stockLabel,
                quantityBox
        );

        // Add hover effects
        card.setOnMouseEntered(e -> {
            if (product.getStock() > 0) {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
                scaleUp.setToX(1.01);
                scaleUp.setToY(1.01);
                scaleUp.play();
            }
        });
        card.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(450), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });

        return card;
    }

    /**
     * Sets a default product image to the specified ImageView. This method is used
     * when a product does not have an associated image or when loading the image fails.
     *
     * @param imageView the ImageView where the default product image should be displayed
     */
    private void setDefaultProductImage(ImageView imageView) {
        try {
            byte[] defaultImageData = getClass().getResourceAsStream("/images/no-image.png").readAllBytes();
            Image defaultImage = new Image(new ByteArrayInputStream(defaultImageData));
            imageView.setImage(defaultImage);
        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImage(null);
        }
    }

    /**
     * Updates the cart with the given product and quantity. If the product already exists
     * in the cart, it removes the existing entry and adds a new one with the updated quantity,
     * provided the quantity is greater than zero. If the quantity is zero, the product is removed
     * from the cart. The cart UI is updated accordingly.
     *
     * @param product the product to add or update in the cart.
     * @param quantity the quantity of the product. Must be greater than zero to add or update the product in the cart.
     */
    private void updateCartProduct(Product product, int quantity) {
        // Find and remove existing item if it exists
        OrderItem existingItem = cart.getItems().stream()
                .filter(item ->
                        "product".equals(item.getItemType()) &&
                                item.getProductId() != null &&
                                item.getProductId().equals(product.getProductId())
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            cart.removeItem(existingItem);
            cashierController.getCartController().removeCartItem(product.getProductName());
        }

        // Only add new item if quantity > 0
        if (quantity > 0) {
            OrderItem newItem = new OrderItem();
            newItem.setItemType("product");
            newItem.setProductId(product.getProductId());
            newItem.setQuantity(quantity);
            newItem.setItemPrice(product.getPrice());
            cart.addItem(newItem);

            // Update cart UI with new quantity
            if (cashierController != null && cashierController.getCartController() != null) {
                cashierController.getCartController().addCartItem(
                        product.getProductName(),
                        product.getPrice().doubleValue(),
                        quantity,
                        "product"
                );
            }
        }
    }

    /**
     * Updates the tickets in the shopping cart based on the selected seats and discounts.
     *
     * This method performs the following operations:
     * 1. Ensures all required controllers and data (e.g., selected session, selected seats)
     *    are available and properly initialized.
     * 2. Retrieves ticket pricing and applicable age-based discount rate using data
     *    from {@code priceDAO}.
     * 3. Clears existing ticket items from the cart while preserving other product items.
     * 4. Processes the selected seats to add discounted and standard tickets into the cart:
     *    - Adds discounted tickets for eligible seats, calculating the total discount.
     *    - Adds non-discounted tickets for the remaining seats.
     * 5. Updates the cart's discount summary to reflect the total discount.
     * 6. Catches and logs any exceptions during the update process.
     *
     * Preconditions:
     * - The {@code cashierController} and its {@code getSelectedSession()}, {@code priceDAO},
     *   and {@code selectedSeats} must be correctly initialized.
     * - The {@code discountedTickets} field determines the number of tickets eligible for discounts.
     *
     * Postconditions:
     * - The cart is updated to include tickets for all selected seats, with discounts
     *   applied where applicable.
     * - The cart UI is refreshed, and any discounts are updated in its summary.
     */
    private void updateTicketsInCart() {
        // Check if necessary controllers and data are available
        if (cashierController == null ||
                cashierController.getSelectedSession() == null ||
                selectedSeats == null ||
                selectedSeats.isEmpty()) {
            return;
        }

        try {
            double basePrice = priceDAO.getTicketPrice(cashierController.getSelectedSession().getHall());
            double discountRate = priceDAO.getAgeDiscount() / 100.0;
            double totalDiscount = 0.0;

            clearExistingTickets();

            List<String> sortedSeats = new ArrayList<>(selectedSeats);
            Collections.sort(sortedSeats);

            int seatIndex = 0;

            // Add discounted tickets
            for (int i = 0; i < discountedTickets; i++) {
                addTicketToCart(sortedSeats.get(seatIndex++), true, basePrice, discountRate);
                totalDiscount += basePrice * discountRate;
            }

            // Add remaining standard tickets
            while (seatIndex < sortedSeats.size()) {
                addTicketToCart(sortedSeats.get(seatIndex++), false, basePrice, 0);
            }

            if (cashierController != null && cashierController.getCartController() != null) {
                cashierController.getCartController().updateDiscount(totalDiscount);
            }
        } catch (Exception e) {
            System.err.println("Error updating tickets in cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all existing tickets in the shopping cart while retaining product items.
     *
     * This method filters out ticket items from the cart, preserving only items with an
     * item type of "product". It then clears the cart entirely and re-adds the product items.
     * Finally, the cart's UI is refreshed to reflect the changes, ensuring that no tickets
     * are present while maintaining product items.
     *
     * The method also handles the cart's graphical user interface (GUI) elements, ensuring
     * the cart visuals are updated through the associated cart controller.
     */
    private void clearExistingTickets() {
        // Remove existing ticket items but keep product items
        List<OrderItem> productItems = cart.getItems().stream()
                .filter(item -> "product".equals(item.getItemType()))
                .collect(java.util.stream.Collectors.toList());

        cart.clear();

        // Re-add product items
        productItems.forEach(cart::addItem);

        // Clear cart UI but maintain product items
        if (cashierController != null && cashierController.getCartController() != null) {
            cashierController.getCartController().refreshCart();
        }
    }

    /**
     * Adds a ticket to the shopping cart and updates the cart UI.
     *
     * @param seatId       The identifier of the seat being added to the cart.
     * @param discounted   Indicates whether a discount is applied to the ticket.
     * @param basePrice    The base price of the ticket before any discounts.
     * @param discountRate The discount rate to be applied if the ticket is discounted.
     */
    private void addTicketToCart(String seatId, boolean discounted, double basePrice, double discountRate) {
        OrderItem ticketItem = new OrderItem();
        ticketItem.setItemType("ticket");
        ticketItem.setScheduleId(cashierController.getSelectedSession().getScheduleId());
        ticketItem.setSeatNumber(convertSeatIdToNumber(seatId));
        ticketItem.setQuantity(1);

        double finalPrice = discounted ? basePrice * (1 - discountRate) : basePrice;

        ticketItem.setItemPrice(BigDecimal.valueOf(finalPrice));
        ticketItem.setDiscountApplied(discounted);
        ticketItem.setOccupantFirstName(firstNameField.getText().trim());
        ticketItem.setOccupantLastName(lastNameField.getText().trim());

        cart.addItem(ticketItem);

        // Update cart UI
        String itemName = String.format("Seat %s - %s", seatId,
                cashierController.getSelectedMovie().getTitle());
        cashierController.getCartController().addCartItem(
                itemName,
                basePrice,
                1,
                "ticket",
                discounted ? basePrice * discountRate : 0
        );
    }

    /**
     * Determines if there are items in the cart.
     *
     * @return true if the cart contains one or more items, false if the cart is empty.
     */
    public boolean hasItems() {
        return !cart.isEmpty();
    }

    /**
     * Determines whether the customer details have been validated successfully.
     *
     * @return true if the customer details are validated, false otherwise.
     */
    public boolean hasValidatedCustomer() {
        return customerDetailsValidated;
    }

    /**
     * Converts a seat identifier into a numeric representation based on its row and column.
     * The numeric representation is computed based on the seating configuration of the hall
     * associated with the currently selected movie session. For "Hall_A", the number of
     * columns is 4; for other halls, it defaults to 8 columns.
     *
     * @param seatId the seat identifier in the format of a single letter followed by a number
     *               (e.g., "A1", "B3"), where the letter represents the row and the number
     *               represents the column.
     * @return the numeric representation of the seat based on its position in the seating
     *         arrangement, calculated using the row index, column index, and the specific hall's
     *         configuration.
     */
    private int convertSeatIdToNumber(String seatId) {
        char row = seatId.charAt(0);
        int col = Integer.parseInt(seatId.substring(1));
        int cols = cashierController.getSelectedSession().getHall().equals("Hall_A") ? 4 : 8;
        return ((row - 'A') * cols) + col;
    }

    /**
     * Sets the CashierController instance for this controller. This method also initializes
     * related data such as the selected seats and updates relevant fields like total seats.
     * Additionally, it loads products into the UI and restores persistent customer details
     * if available.
     *
     * @param controller the CashierController instance to be associated with this controller.
     */
    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
        if (this.selectedSeats == null) {
            this.selectedSeats = controller.getSelectedSeats();
        }

        // Update total seats when controller is set
        if (selectedSeats != null) {
            this.totalSeats = selectedSeats.size();
        }

        loadProducts();

        // Restore persistent details AFTER setting total seats
        if (persistentCustomerDetails != null) {
            // Explicitly restore all details
            restorePersistentDetails();
        }
    }

    /**
     * Updates the selected seats for the customer and adjusts the state of
     * related ticket counts, discounted ticket limits, and UI components.
     * If persistent customer details are available, attempts to restore them.
     *
     * @param seats A set of strings representing the seat identifiers that
     *              are newly selected by the customer.
     */
    public void setSelectedSeats(Set<String> seats) {
        this.selectedSeats = seats;
        this.totalSeats = seats.size();

        // Reset or adjust discounted tickets based on new seat count
        discountedTickets = Math.min(discountedTickets, totalSeats);

        updateTicketCounts();
        updateButtonStates();

        // If we have persistent details, try to restore
        if (persistentCustomerDetails != null) {
            restorePersistentDetails();
        }
    }

    /**
     * Displays an error alert with a specified title and content message.
     *
     * @param title the title of the error alert
     * @param content the content message of the error alert
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Configures the styling behavior for the tabs within the productsTabPane
     * component. This method ensures that the visual appearance of each tab
     * reflects its selection state.
     *
     * The method performs the following:
     * 1. Adds a listener to the selection model of the productsTabPane to
     *    invoke the updateTabStyles method whenever the selected tab changes.
     * 2. Applies initial styling by calling updateTabStyles upon invocation.
     *
     * This ensures that tab-specific styles are dynamically updated whenever
     * the user selects a different tab within the productsTabPane.
     */
    private void setupTabSelectionStyling() {
        // Add a listener to update tab styles when selection changes
        productsTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTabStyles();
        });

        // Initial styling
        updateTabStyles();
    }

    /**
     * Updates the visual styles of the tabs in the productsTabPane to reflect their selection state.
     *
     * For each tab in the productsTabPane:
     * - If the tab is selected, it applies the "tab-selected" style class to both the tab and its graphic node.
     * - If the tab is not selected, it removes the "tab-selected" style class from the tab and its graphic node.
     *
     * If a tab does not already have a graphic node associated with it, a new Label is created
     * with the tab's text and used as the graphic node for styling.
     */
    private void updateTabStyles() {
        for (Tab tab : productsTabPane.getTabs()) {
            // Create a Label with the tab text if no existing graphic
            Node tabNode = tab.getGraphic();
            if (tabNode == null) {
                tabNode = new Label(tab.getText());
            }

            if (tab.isSelected()) {
                // Add selected style
                tabNode.getStyleClass().add("tab-selected");
                tab.getStyleClass().add("tab-selected");
            } else {
                // Remove selected style
                tabNode.getStyleClass().remove("tab-selected");
                tab.getStyleClass().remove("tab-selected");
            }
        }
    }

    /**
     * Configures a sliding animation for transitions between tabs in the {@code productsTabPane}.
     *
     * When the selected tab changes, this method adds a listener to the selection model
     * of the {@code productsTabPane}. The listener triggers an animation that visually transitions
     * from the old tab to the newly selected tab, enhancing the user experience.
     *
     * The animation makes use of the {@link #animateTabTransition(int, int)} method,
     * which handles the actual mechanics of the sliding effect between the tabs,
     * based on their indexes in the tab pane.
     */
    private void setupTabSlidingAnimation() {
        // Add listener to track tab selection
        productsTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            animateTabTransition(oldVal.intValue(), newVal.intValue());
        });
    }

    /**
     * Animates the transition between two tabs in the productsTabPane using a sliding and fading effect.
     *
     * @param oldIndex The index of the currently active tab.
     * @param newIndex The index of the tab to transition to.
     */
    private void animateTabTransition(int oldIndex, int newIndex) {
        // Get the content of the old and new tabs
        ScrollPane oldContent = (ScrollPane) productsTabPane.getTabs().get(oldIndex).getContent();
        ScrollPane newContent = (ScrollPane) productsTabPane.getTabs().get(newIndex).getContent();

        // Determine slide direction
        boolean slideLeft = newIndex > oldIndex;

        // Create a subtle animation
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(oldContent.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(newContent.opacityProperty(), 0.5, Interpolator.EASE_OUT),
                        new KeyValue(newContent.translateXProperty(), slideLeft ? 10 : -10, Interpolator.EASE_OUT)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(oldContent.opacityProperty(), 0.5, Interpolator.EASE_OUT),
                        new KeyValue(newContent.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(newContent.translateXProperty(), 0, Interpolator.EASE_OUT)
                )
        );

        timeline.play();
    }
}