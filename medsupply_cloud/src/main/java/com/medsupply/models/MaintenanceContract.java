package com.medsupply.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MaintenanceContract model - Represents a maintenance contract for a sold product
 * Linked to a product and a client
 * 
 * Business Rules:
 * - Only active maintenance contracts can receive maintenance requests
 * - Contracts have warranty duration, start date, end date, and active/inactive status
 */
public class MaintenanceContract {
    private String contractId;
    private String clientId;
    private String productId;
    private Integer warrantyDuration; // in months
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, INACTIVE, EXPIRED
    private String contractReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaintenanceContract() {}

    public MaintenanceContract(String clientId, String productId, Integer warrantyDuration, 
                               LocalDate startDate, LocalDate endDate, String status) {
        this.contractId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.productId = productId;
        this.warrantyDuration = warrantyDuration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters
    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getWarrantyDuration() {
        return warrantyDuration;
    }

    public void setWarrantyDuration(Integer warrantyDuration) {
        this.warrantyDuration = warrantyDuration;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContractReference() {
        return contractReference;
    }

    public void setContractReference(String contractReference) {
        this.contractReference = contractReference;
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
     * Check if contract is currently active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && 
               (endDate == null || !endDate.isBefore(LocalDate.now()));
    }

    /**
     * Check if contract is expired
     */
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "MaintenanceContract{" +
                "contractId=" + contractId +
                ", clientId=" + clientId +
                ", productId=" + productId +
                ", warrantyDuration=" + warrantyDuration +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", contractReference='" + contractReference + '\'' +
                '}';
    }
}
