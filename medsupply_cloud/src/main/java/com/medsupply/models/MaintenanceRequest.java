package com.medsupply.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MaintenanceRequest model - Represents a maintenance request from a client
 * Linked to a maintenance contract and a product
 * 
 * Business Rules:
 * - Only active maintenance contracts can receive maintenance requests
 * - Priority values: LOW, MEDIUM, HIGH, URGENT
 * - Request status: OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED
 */
public class MaintenanceRequest {
    private String requestId;
    private String contractId;
    private String clientId;
    private String productId;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED
    private LocalDateTime requestDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaintenanceRequest() {}

    public MaintenanceRequest(String contractId, String clientId, String productId, 
                             String description, String priority) {
        this.requestId = UUID.randomUUID().toString();
        this.contractId = contractId;
        this.clientId = clientId;
        this.productId = productId;
        this.description = description;
        this.priority = priority;
        this.status = "OPEN";
        this.requestDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
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
     * Check if request is open
     */
    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    /**
     * Check if request is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * Check if request is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status) || "CLOSED".equals(status);
    }

    @Override
    public String toString() {
        return "MaintenanceRequest{" +
                "requestId=" + requestId +
                ", contractId=" + contractId +
                ", clientId=" + clientId +
                ", productId=" + productId +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", requestDate=" + requestDate +
                '}';
    }
}
