package com.group18.model;

import java.time.LocalDateTime;

/**
 * The PriceHistory class represents a record of a price change for an item.
 * It captures details of the change including the timestamp, item name, old price,
 * new price, and the user who updated the price.
 */
public class PriceHistory {
    /**
     * Represents the timestamp of when a price change occurred.
     * This field stores the date and time for tracking modifications.
     */
    private LocalDateTime changeTimestamp;
    /**
     * Represents the name or identifier of an item whose price history is being tracked.
     */
    private String item;
    /**
     * Stores the price of an item before a change is made.
     * Represents the previous price in a price change record.
     */
    private Double oldPrice;
    /**
     * Represents the new price for an item after a price change.
     * This value is recorded as part of the price change history.
     */
    private Double newPrice;
    /**
     * The username or identifier of the user who performed the update
     * on the item's price. This field tracks the individual responsible
     * for modifying the price record in the price history.
     */
    private String updatedBy;

    /**
     * Constructs a new PriceHistory object representing a record of a price change.
     *
     * @param changeTimestamp the timestamp when the price change occurred
     * @param item the name of the item whose price was changed
     * @param oldPrice the previous price of the item before the change
     * @param newPrice the updated price of the item after the change
     * @param updatedBy the name of the user who made the price change
     */
    public PriceHistory(LocalDateTime changeTimestamp, String item, Double oldPrice, Double newPrice, String updatedBy) {
        this.changeTimestamp = changeTimestamp;
        this.item = item;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.updatedBy = updatedBy;
    }

    /**
     * Retrieves the timestamp of a price change.
     *
     * @return the timestamp of the price change as a LocalDateTime object
     */
    public LocalDateTime getChangeTimestamp() {
        return changeTimestamp;
    }

    /**
     * Updates the timestamp indicating when the price change occurred.
     *
     * @param changeTimestamp the timestamp of the price change
     */
    public void setChangeTimestamp(LocalDateTime changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    /**
     * Retrieves the name of the item associated with the price change record.
     *
     * @return the item name as a String
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the name of the item associated with the price history.
     *
     * @param item the name of the item to be set
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Retrieves the old price of the item before the price change occurred.
     *
     * @return the old price of the item as a Double
     */
    public Double getOldPrice() {
        return oldPrice;
    }

    /**
     * Sets the old price of the item.
     *
     * @param oldPrice the previous price of the item
     */
    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    /**
     * Retrieves the new price that has been set or updated for an item.
     *
     * @return the new price of the item
     */
    public Double getNewPrice() {
        return newPrice;
    }

    /**
     * Updates the new price for the item.
     *
     * @param newPrice the updated price of the item
     */
    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }

    /**
     * Retrieves the username or identifier of the user who last updated the price.
     *
     * @return the username or identifier of the user who made the most recent update
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Updates the identifier of the user who modified the price record.
     *
     * @param updatedBy the name or identifier of the user making the change.
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}