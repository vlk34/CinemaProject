package com.group18.model;

import java.time.LocalDateTime;

public class PriceHistory {
    private LocalDateTime changeTimestamp;
    private String item;
    private Double oldPrice;
    private Double newPrice;
    private String updatedBy;

    public PriceHistory(LocalDateTime changeTimestamp, String item, Double oldPrice, Double newPrice, String updatedBy) {
        this.changeTimestamp = changeTimestamp;
        this.item = item;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(LocalDateTime changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
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