package com.medsupply.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tender model - Represents a B2B reverse auction tender
 * Created by clients (hospitals/labs) to solicit competitive bids from suppliers
 * 
 * Business Rules:
 * - Only CLIENT (hospital/lab) can create tenders
 * - Tender has a budget maximum
 * - Tender has a deadline for bid submission
 * - Status: OPEN, CLOSED, EXPIRED, AWARDED
 * - Lowest bid wins
 */
public class Tender {
    private String tenderId;
    private String clientId;
    private String title;
    private String description;
    private BigDecimal budgetMax;
    private LocalDateTime deadline;
    private String status; // OPEN, CLOSED, EXPIRED, AWARDED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Tender() {}

    public Tender(String clientId, String title, String description, 
                  BigDecimal budgetMax, LocalDateTime deadline) {
        this.tenderId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.budgetMax = budgetMax;
        this.deadline = deadline;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTenderId() {
        return tenderId;
    }

    public void setTenderId(String tenderId) {
        this.tenderId = tenderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    /**
     * Check if tender is open for bidding
     */
    public boolean isOpen() {
        return "OPEN".equals(status) && deadline != null && !deadline.isBefore(LocalDateTime.now());
    }

    /**
     * Check if tender is expired
     */
    public boolean isExpired() {
        return deadline != null && deadline.isBefore(LocalDateTime.now());
    }

    /**
     * Check if tender is closed
     */
    public boolean isClosed() {
        return "CLOSED".equals(status) || "EXPIRED".equals(status) || "AWARDED".equals(status);
    }

    @Override
    public String toString() {
        return "Tender{" +
                "tenderId=" + tenderId +
                ", clientId=" + clientId +
                ", title='" + title + '\'' +
                ", budgetMax=" + budgetMax +
                ", deadline=" + deadline +
                ", status='" + status + '\'' +
                '}';
    }
}
