package com.group18.model;  // or whatever package you want to put it in

public class PriceHistory {
    private String date;
    private String item;
    private Double oldPrice;
    private Double newPrice;
    private String updatedBy;

    // Constructor
    public PriceHistory(String date, String item, Double oldPrice, Double newPrice, String updatedBy) {
        this.date = date;
        this.item = item;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.updatedBy = updatedBy;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}