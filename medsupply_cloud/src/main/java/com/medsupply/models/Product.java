package com.medsupply.models;

import java.math.BigDecimal;

/**
 * Product model - Represents a medical product in the catalog
 * Maps to the "products" table in Supabase (UUID primary key "id")
 */
public class Product {
    private String id;
    private String name;
    private String reference;
    private String description;
    private BigDecimal unitPrice;
    private Integer warrantyMonths;
    private String imageUrl;
    private Boolean isActive;
    private String categoryId;
    private String brandId;
    private String supplierId;

    public Product() {}

    public Product(String name, String reference, String description, BigDecimal unitPrice,
                   Integer warrantyMonths, String imageUrl, Boolean isActive,
                   String categoryId, String brandId, String supplierId) {
        this.name = name;
        this.reference = reference;
        this.description = description;
        this.unitPrice = unitPrice;
        this.warrantyMonths = warrantyMonths;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.supplierId = supplierId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(Integer warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", reference='" + reference + '\'' +
                ", unitPrice=" + unitPrice +
                ", categoryId='" + categoryId + '\'' +
                ", brandId='" + brandId + '\'' +
                ", supplierId='" + supplierId + '\'' +
                '}';
    }
}