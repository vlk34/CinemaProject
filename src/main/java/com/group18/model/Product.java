package com.group18.model;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

/**
 * Represents a product with details such as ID, name, type, price, stock, and image data.
 * This class provides constructors and methods for creating, modifying, and retrieving product information.
 */
public class Product {
    /**
     * Represents the unique identifier for a product.
     * This identifier is used to distinguish individual products within the system.
     */
    private int productId;
    /**
     * Represents the name of the product.
     * This attribute is used to uniquely identify or provide a descriptive name for the product.
     */
    private String productName;
    /**
     * Represents the type of the product.
     * This field is restricted to specific predefined values: 'beverage', 'biscuit', or 'toy'.
     * It is used to categorize the product within these defined types.
     */
    private String productType; // enum('beverage','biscuit','toy')
    /**
     * Represents the price of the product.
     * The price is stored as a BigDecimal to maintain precision, especially for currency values.
     */
    private BigDecimal price;
    /**
     * Represents the quantity of the product available in stock.
     * This value is used to track inventory levels for the product.
     */
    private int stock;
    /**
     * Represents the image data related to the product.
     * This field stores the binary data of an image associated with the product.
     * It is useful for rendering the product image in applications or storing it for later use.
     */
    private byte[] imageData;
    /**
     * Default constructor for the Product class.
     * Creates an instance of Product with no initial values set.
     * Useful in scenarios where properties are assigned post-creation.
     */
    public Product() {}

    /**
     * Constructs a new Product instance with the specified details.
     *
     * @param productName the name of the product
     * @param productType the type or category of the product
     * @param price the price of the product
     * @param stock the stock quantity of the product
     * @param imageData the image data of the product represented as a byte array
     */
    public Product(String productName, String productType, BigDecimal price, int stock, byte[] imageData) {
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.stock = stock;
        this.imageData = imageData;
    }

    /**
     * Constructor for creating a Product with all attributes, including an existing product ID.
     *
     * @param productId the unique identifier of the product
     * @param productName the name of the product
     * @param productType the category or type of the product
     * @param price the price of the product as a BigDecimal
     * @param stock the quantity of the product in stock
     * @param imageData the image data of the product as a byte array
     */
    public Product(int productId, String productName, String productType, BigDecimal price, int stock, byte[] imageData) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.stock = stock;
        this.imageData = imageData;
    }

    /**
     * Retrieves the image data associated with the product.
     *
     * @return a byte array representing the image data of the product, or null if no image data is set.
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * Sets the image data for this product.
     *
     * @param imageData the byte array representing the image data of the product
     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    /**
     * Retrieves the image associated with a product, if image data is available.
     * Converts the stored byte array of image data into an Image object.
     *
     * @return An Image object created from the existing image data, or null if no image data is available.
     */
    public Image getImage() {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null;
    }

    /**
     * Constructs a new Product instance without an image path for compatibility with older implementations.
     *
     * @param productName the name of the product
     * @param productType the type or category of the product
     * @param price the price of the product
     * @param stock the stock quantity of the product
     */
    public Product(String productName, String productType, BigDecimal price, int stock) {
        this(productName, productType, price, stock, null);
    }

    /**
     * Constructs a Product object with the specified product ID, name, type, price, and stock.
     * This constructor is intended for usage when an existing product ID needs to be specified.
     *
     * @param productId the unique identifier for the product
     * @param productName the name of the product
     * @param productType the type or category of the product
     * @param price the price of the product
     * @param stock the stock quantity available for the product
     */
    public Product(int productId, String productName, String productType, BigDecimal price, int stock) {
        this(productId, productName, productType, price, stock, null);
    }

    /**
     * Retrieves the unique identifier of the product.
     *
     * @return the product ID
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Sets the product ID for the product.
     *
     * @param productId the ID to set for the product
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Retrieves the name of the product.
     *
     * @return the name of the product as a String
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the name of the product.
     *
     * @param productName the name of the product to be set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Retrieves the product type of this product.
     *
     * @return the product type as a String
     */
    public String getProductType() {
        return productType;
    }

    /**
     * Sets the type of the product.
     *
     * @param productType the type of the product to be set
     */
    public void setProductType(String productType) {
        this.productType = productType;
    }

    /**
     * Retrieves the price of the product.
     *
     * @return the price of the product as a BigDecimal
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the product.
     *
     * @param price the price to set for the product
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Retrieves the current stock quantity of the product.
     *
     * @return the stock quantity of the product
     */
    public int getStock() {
        return stock;
    }

    /**
     * Updates the stock quantity of the product.
     *
     * @param stock the new stock quantity to be set
     */
    public void setStock(int stock) {
        this.stock = stock;
    }
}