package com.medsupply.models;

import java.util.UUID;

/**
 * TenderItem model - Represents an item in a tender
 * Links a tender to a product with required quantity
 * 
 * Business Rules:
 * - Each tender can have multiple items
 * - Suppliers bid on the total tender (all items)
 */
public class TenderItem {
    private String tenderItemId;
    private String tenderId;
    private String productId;
    private Integer quantity;

    public TenderItem() {}

    public TenderItem(String tenderId, String productId, Integer quantity) {
        this.tenderItemId = UUID.randomUUID().toString();
        this.tenderId = tenderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getTenderItemId() {
        return tenderItemId;
    }

    public void setTenderItemId(String tenderItemId) {
        this.tenderItemId = tenderItemId;
    }

    public String getTenderId() {
        return tenderId;
    }

    public void setTenderId(String tenderId) {
        this.tenderId = tenderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "TenderItem{" +
                "tenderItemId=" + tenderItemId +
                ", tenderId=" + tenderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
