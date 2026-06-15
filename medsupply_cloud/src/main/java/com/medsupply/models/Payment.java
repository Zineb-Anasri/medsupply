package com.medsupply.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment model - Represents a payment for an order
 * Status: PENDING, PARTIAL, PAID, OVERDUE, CANCELLED
 * 
 * Business Rules:
 * - HOSPITAL and LABORATORY clients: up to 60 days credit, partial payments allowed
 * - Other client types: immediate payment required
 */
public class Payment {
    private String paymentId;
    private String orderId;
    private String clientId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status; // PENDING, PARTIAL, PAID, OVERDUE, CANCELLED
    private LocalDate dueDate;
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {}

    public Payment(String orderId, String clientId, BigDecimal amount, String status, 
                   LocalDate dueDate, String paymentMethod) {
        this.paymentId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.clientId = clientId;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.status = status;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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
     * Get remaining amount to be paid
     */
    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount);
    }

    /**
     * Check if payment is complete
     */
    public boolean isFullyPaid() {
        return paidAmount.compareTo(amount) >= 0;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", clientId=" + clientId +
                ", amount=" + amount +
                ", paidAmount=" + paidAmount +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
