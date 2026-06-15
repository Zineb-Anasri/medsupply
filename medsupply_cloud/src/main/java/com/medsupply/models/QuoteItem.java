package com.medsupply.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QuoteItem model - Represents a product in a quote request
 */
public class QuoteItem {
    private String itemId;
    private String quoteId;
    private String productId;
    private Integer quantity;
    private BigDecimal proposedPrice;
    private BigDecimal adminPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuoteItem() {}

    public QuoteItem(String quoteId, String productId, Integer quantity, BigDecimal proposedPrice) {
        this.itemId = UUID.randomUUID().toString();
        this.quoteId = quoteId;
        this.productId = productId;
        this.quantity = quantity;
        this.proposedPrice = proposedPrice;
        this.adminPrice = null;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
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

    public BigDecimal getProposedPrice() {
        return proposedPrice;
    }

    public void setProposedPrice(BigDecimal proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public BigDecimal getAdminPrice() {
        return adminPrice;
    }

    public void setAdminPrice(BigDecimal adminPrice) {
        this.adminPrice = adminPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "QuoteItem{" +
                "itemId=" + itemId +
                ", quoteId=" + quoteId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", proposedPrice=" + proposedPrice +
                ", adminPrice=" + adminPrice +
                '}';
    }
}
