package com.medsupply.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SupplierBid model - Represents a bid from a supplier for a tender
 * Suppliers compete by submitting lower bids
 * 
 * Business Rules:
 * - Only SUPPLIER can submit bids
 * - Each bid must be LOWER than current best bid
 * - One supplier cannot spam bids higher than previous bid
 * - Lowest bid wins
 */
public class SupplierBid {
    private String bidId;
    private String tenderId;
    private String supplierId;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public SupplierBid() {}

    public SupplierBid(String tenderId, String supplierId, BigDecimal price) {
        this.bidId = UUID.randomUUID().toString();
        this.tenderId = tenderId;
        this.supplierId = supplierId;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getTenderId() {
        return tenderId;
    }

    public void setTenderId(String tenderId) {
        this.tenderId = tenderId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Check if this bid is better (lower) than another bid
     */
    public boolean isBetterThan(SupplierBid other) {
        return this.price.compareTo(other.getPrice()) < 0;
    }

    @Override
    public String toString() {
        return "SupplierBid{" +
                "bidId=" + bidId +
                ", tenderId=" + tenderId +
                ", supplierId=" + supplierId +
                ", price=" + price +
                ", createdAt=" + createdAt +
                '}';
    }
}
