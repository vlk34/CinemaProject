package com.group18.model;

import java.time.LocalDate;

public class PriceHistory {
    private LocalDate changeDate;
    private String item;
    private Double oldPrice;
    private Double newPrice;
    private String updatedBy;

    // Constructor
    public PriceHistory(LocalDate changeDate, String item, Double oldPrice, Double newPrice, String updatedBy) {
        this.changeDate = changeDate;
        this.item = item;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.updatedBy = updatedBy;
    }

    // Getters and setters
    public LocalDate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDate changeDate) {
        this.changeDate = changeDate;
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