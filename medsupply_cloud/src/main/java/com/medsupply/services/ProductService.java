package com.medsupply.services;

import java.util.List;

import com.medsupply.dao.ProductDAO;
import com.medsupply.models.Product;

/**
 * ProductService - Business logic for product management
 * Handles product operations with validation and business rules
 */
public class ProductService {

    private ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    /**
     * Get product by ID
     * @param productId Product ID (UUID)
     * @return Product object
     */
    public Product getProduct(String productId) throws Exception {
        return productDAO.findById(productId);
    }

    /**
     * Get all products
     * @return List of all products
     */
    public List<Product> getAllProducts() throws Exception {
        return productDAO.findAll();
    }

    /**
     * Get products by category
     * @param categoryId Category ID (UUID)
     * @return List of products in category
     */
    public List<Product> getProductsByCategory(String categoryId) throws Exception {
        return productDAO.findByCategory(categoryId);
    }

    /**
     * Get products by brand
     * @param brandId Brand ID (UUID)
     * @return List of products from brand
     */
    public List<Product> getProductsByBrand(String brandId) throws Exception {
        return productDAO.findByBrand(brandId);
    }

    /**
     * Get products by supplier
     * @param supplierId Supplier ID (UUID)
     * @return List of products from supplier
     */
    public List<Product> getProductsBySupplier(String supplierId) throws Exception {
        return productDAO.findBySupplier(supplierId);
    }

    /**
     * Create a new product
     * @param product Product object to create
     * @return Created product
     */
    public Product createProduct(Product product) throws Exception {
        // Validate product data
        validateProduct(product);

        // Validate price is positive
        if (product.getUnitPrice() == null || product.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new Exception("Unit price must be positive");
        }

        return productDAO.create(product);
    }

    /**
     * Update product
     * @param product Product object to update
     * @return true if update successful
     */
    public boolean updateProduct(Product product) throws Exception {
        // Validate product exists
        Product existingProduct = productDAO.findById(product.getId());
        if (existingProduct == null) {
            throw new Exception("Product not found");
        }

        // Validate product data
        validateProduct(product);

        // Validate price is positive
        if (product.getUnitPrice() == null || product.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new Exception("Unit price must be positive");
        }

        return productDAO.update(product);
    }

    /**
     * Delete product
     * @param productId Product ID (UUID)
     * @return true if deletion successful
     */
    public boolean deleteProduct(String productId) throws Exception {
        // Validate product exists
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new Exception("Product not found");
        }

        return productDAO.delete(productId);
    }

    /**
     * Validate product data
     * @param product Product to validate
     */
    private void validateProduct(Product product) throws Exception {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new Exception("Product name is required");
        }
        if (product.getReference() == null || product.getReference().trim().isEmpty()) {
            throw new Exception("Product reference is required");
        }
        if (product.getUnitPrice() == null) {
            throw new Exception("Unit price is required");
        }
    }
}