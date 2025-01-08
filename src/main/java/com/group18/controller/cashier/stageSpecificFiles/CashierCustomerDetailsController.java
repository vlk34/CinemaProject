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

public class CashierCustomerDetailsController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label ageDiscountedTicketsCount;
    @FXML private Button minusDiscounted;
    @FXML private Button plusDiscounted;
    @FXML private Button applyDiscountsButton;
    @FXML private ImageView infoIcon;

    @FXML private TextField ageField;
    @FXML private Button verifyAgeButton;
    @FXML private TabPane productsTabPane;
    @FXML private FlowPane beveragesContainer;
    @FXML private FlowPane biscuitsContainer;
    @FXML private FlowPane toysContainer;
    private CashierController cashierController;
    private Set<String> selectedSeats;
    private PriceDAO priceDAO;
    private ProductDAO productDAO;
    private ShoppingCart cart;
    private boolean isDiscountApplicable = false;
    private boolean customerDetailsValidated = false;

    private int discountedTickets = 0;
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

    private static CustomerDetails persistentCustomerDetails = null;

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

    private void setupTooltip() {
        // Find the HBox container
        HBox tooltipContainer = (HBox) infoIcon.getParent();

        // Increase icon size
        infoIcon.setFitWidth(24);  // Slightly larger
        infoIcon.setFitHeight(24);

        Tooltip tooltip = new Tooltip("Customers under 18 or over 60 years old qualify for age-based discounts");
        tooltip.setShowDelay(Duration.millis(100));

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

    private void adjustDiscountedTickets(boolean increase) {
        if (increase && discountedTickets < totalSeats) {
            discountedTickets++;
        } else if (!increase && discountedTickets > 0) {
            discountedTickets--;
        }

        updateTicketCounts();
        updateButtonStates();
    }

    private void updateTicketCounts() {
        ageDiscountedTicketsCount.setText(String.valueOf(discountedTickets));
    }

    private void updateButtonStates() {
        minusDiscounted.setDisable(discountedTickets == 0);
        plusDiscounted.setDisable(discountedTickets >= totalSeats);

        boolean detailsValid = !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty();
        applyDiscountsButton.setDisable(!detailsValid || totalSeats == 0);
    }

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

        // Disable discount controls after applying
//        minusDiscounted.setDisable(true);
//        plusDiscounted.setDisable(true);
//        applyDiscountsButton.setDisable(true);

        // Show success message
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText(null);
        success.setContentText("Age discounts have been applied successfully.");
        success.showAndWait();
    }

    public boolean hasValidDetailsAndVerification() {
        return !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                customerDetailsValidated;
    }

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

    public void resetUI() {
        firstNameField.clear();
        lastNameField.clear();
        discountedTickets = 0;
        updateTicketCounts();
        updateButtonStates();
        customerDetailsValidated = false;
    }

    public static void clearPersistentDetailsStatic() {
        persistentCustomerDetails = null;
    }

    @FXML
    private void handleVerifyAge() {
        String ageText = ageField.getText().trim();
        if (ageText.isEmpty()) {
            showError("Invalid Input", "Please enter customer's age.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            if (age < 1 || age > 90) {
                throw new NumberFormatException();
            }

            isDiscountApplicable = age < 18 || age > 60;

            // Show result alert
            Alert alert = new Alert(
                    isDiscountApplicable ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING,
                    isDiscountApplicable ? "Age discount will be applied!" : "No age discount applicable."
            );
            alert.setHeaderText(null);
            alert.showAndWait();

            // Update all ticket items in cart with customer details and discount
            updateTicketsInCart();

            // Mark customer details as validated
            customerDetailsValidated = true;

            // Save details for persistence
            savePersistentDetails();

            // Update the action bar buttons
            updateActionBarState();
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Age must be a valid number between 1 and 90.");
        }
    }

    public boolean isCustomerDetailsValidated() {
        return customerDetailsValidated;
    }

    private void setupCustomerDetailsValidation() {
        firstNameField.textProperty().addListener((obs, old, newVal) -> validateCustomerDetails());
        lastNameField.textProperty().addListener((obs, old, newVal) -> validateCustomerDetails());
    }

    private void validateCustomerDetails() {
        boolean detailsValid = !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty();
        applyDiscountsButton.setDisable(!detailsValid);

        if (customerDetailsValidated) {
            updateActionBarState();
        }
    }

    private void updateActionBarState() {
        if (cashierController != null && cashierController.getActionBarController() != null) {
            cashierController.getActionBarController().updateButtonStates(
                    cashierController.getCurrentStageIndex()
            );
        }
    }

    private void loadProducts() {
        loadProductCategory("beverage", beveragesContainer);
        loadProductCategory("biscuit", biscuitsContainer);
        loadProductCategory("toy", toysContainer);
    }

    private void loadProductCategory(String category, FlowPane container) {
        List<Product> products = productDAO.getProductsByType(category);
        container.getChildren().clear();

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            container.getChildren().add(productCard);
        }
    }

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

    public boolean hasItems() {
        return !cart.isEmpty();
    }

    public boolean hasValidatedCustomer() {
        return customerDetailsValidated;
    }

    private int convertSeatIdToNumber(String seatId) {
        char row = seatId.charAt(0);
        int col = Integer.parseInt(seatId.substring(1));
        int cols = cashierController.getSelectedSession().getHall().equals("Hall_A") ? 4 : 8;
        return ((row - 'A') * cols) + col;
    }

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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupTabSelectionStyling() {
        // Add a listener to update tab styles when selection changes
        productsTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTabStyles();
        });

        // Initial styling
        updateTabStyles();
    }

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

    private void setupTabSlidingAnimation() {
        // Add listener to track tab selection
        productsTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            animateTabTransition(oldVal.intValue(), newVal.intValue());
        });
    }

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