package com.medsupply.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Intervention model - Represents a technical intervention for a maintenance request
 * Linked to a maintenance request
 * 
 * Business Rules:
 * - All interventions must be recorded for history
 * - Contains technician name, intervention date, diagnosis, actions performed, replaced parts, cost, completion status
 */
public class Intervention {
    private String interventionId;
    private String requestId;
    private String technicianName;
    private LocalDateTime interventionDate;
    private String diagnosis;
    private String actionsPerformed;
    private String replacedParts;
    private BigDecimal interventionCost;
    private String completionStatus; // PENDING, COMPLETED, CANCELLED
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Intervention() {}

    public Intervention(String requestId, String technicianName, String diagnosis) {
        this.interventionId = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.technicianName = technicianName;
        this.diagnosis = diagnosis;
        this.interventionDate = LocalDateTime.now();
        this.completionStatus = "PENDING";
    }

    // Getters and Setters
    public String getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(String interventionId) {
        this.interventionId = interventionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public LocalDateTime getInterventionDate() {
        return interventionDate;
    }

    public void setInterventionDate(LocalDateTime interventionDate) {
        this.interventionDate = interventionDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getActionsPerformed() {
        return actionsPerformed;
    }

    public void setActionsPerformed(String actionsPerformed) {
        this.actionsPerformed = actionsPerformed;
    }

    public String getReplacedParts() {
        return replacedParts;
    }

    public void setReplacedParts(String replacedParts) {
        this.replacedParts = replacedParts;
    }

    public BigDecimal getInterventionCost() {
        return interventionCost;
    }

    public void setInterventionCost(BigDecimal interventionCost) {
        this.interventionCost = interventionCost;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
     * Check if intervention is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(completionStatus);
    }

    /**
     * Check if intervention is pending
     */
    public boolean isPending() {
        return "PENDING".equals(completionStatus);
    }

    @Override
    public String toString() {
        return "Intervention{" +
                "interventionId=" + interventionId +
                ", requestId=" + requestId +
                ", technicianName='" + technicianName + '\'' +
                ", interventionDate=" + interventionDate +
                ", diagnosis='" + diagnosis + '\'' +
                ", actionsPerformed='" + actionsPerformed + '\'' +
                ", replacedParts='" + replacedParts + '\'' +
                ", interventionCost=" + interventionCost +
                ", completionStatus='" + completionStatus + '\'' +
                '}';
    }
}
