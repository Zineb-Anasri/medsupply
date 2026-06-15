package com.medsupply.models;

import java.time.LocalDateTime;

/**
 * Supplier model - Profile table linked to users via user_id
 * Represents suppliers who provide medical products
 */
public class Supplier {
    private String supplierId;
    private String userId;
    private String companyName;
    private String contactPerson;
    private String address;
    private String phone;
    private String email;
    private String taxId;
    private String licenseNumber;
    private Boolean isVerified;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supplier() {}

    public Supplier(String userId, String companyName, String contactPerson, String address, String phone, String email) {
        this.userId = userId;
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.isVerified = false;
        this.rating = 0.0;
    }

    // Getters and Setters
    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
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
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", userId=" + userId +
                ", companyName='" + companyName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", isVerified=" + isVerified +
                ", rating=" + rating +
                '}';
    }
}
