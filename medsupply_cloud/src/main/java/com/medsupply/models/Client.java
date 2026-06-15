package com.medsupply.models;

import java.time.LocalDateTime;

/**
 * Client model - Profile table linked to users via user_id
 * Client types: HOSPITAL, CLINIC, DOCTOR, MEDICAL_OFFICE, LABORATORY, RESELLER
 */
public class Client {
    private String clientId;
    private String userId;
    private String name;
    private String type; // HOSPITAL, CLINIC, DOCTOR, MEDICAL_OFFICE, LABORATORY, RESELLER
    private String address;
    private String phone;
    private String email;
    private String taxId;
    private Boolean creditEligible; // Hospitals/labs have 60 days credit
    private Integer creditDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client() {}

    public Client(String userId, String name, String type, String address, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.address = address;
        this.phone = phone;
        this.email = email;
        // Set credit eligibility based on client type
        this.creditEligible = "HOSPITAL".equals(type) || "LABORATORY".equals(type);
        this.creditDays = this.creditEligible ? 60 : 0;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public Boolean getCreditEligible() {
        return creditEligible;
    }

    public void setCreditEligible(Boolean creditEligible) {
        this.creditEligible = creditEligible;
    }

    public Integer getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(Integer creditDays) {
        this.creditDays = creditDays;
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
        return "Client{" +
                "clientId=" + clientId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", creditEligible=" + creditEligible +
                ", creditDays=" + creditDays +
                '}';
    }
}
