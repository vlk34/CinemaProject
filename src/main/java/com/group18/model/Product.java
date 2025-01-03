package com.group18.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String productName;
    private String productType; // enum('beverage','biscuit','toy')
    private BigDecimal price;
    private int stock;
    private String imagePath;
    public Product() {}

    public Product(String productName, String productType, BigDecimal price, int stock, String imagePath) {
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    // Constructor with product ID (for when you want to create a product with an existing ID)
    public Product(int productId, String productName, String productType, BigDecimal price, int stock, String imagePath) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    // Constructor without image path (for backwards compatibility)
    public Product(String productName, String productType, BigDecimal price, int stock) {
        this(productName, productType, price, stock, null);
    }

    // Constructor with ID but without image path (for backwards compatibility)
    public Product(int productId, String productName, String productType, BigDecimal price, int stock) {
        this(productId, productName, productType, price, stock, null);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}