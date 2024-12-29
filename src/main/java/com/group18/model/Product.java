package com.group18.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String productName;
    private String productType; // enum('beverage','biscuit','toy')
    private BigDecimal price;
    private int stock;

    public Product() {}

    public Product(String productName, String productType, BigDecimal price, int stock) {
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.stock = stock;
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