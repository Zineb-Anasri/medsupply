package com.medsupply.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Product;
import com.medsupply.services.ProductService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ProductServlet - Handles product catalog operations
 *
 * Endpoints:
 * - GET /api/products - List all products (with optional filters)
 * - GET /api/products/{id} - Get product by ID (UUID)
 * - POST /api/products - Create product (ADMIN only)
 * - PUT /api/products/{id} - Update product (ADMIN only)
 * - DELETE /api/products/{id} - Delete product (ADMIN only)
 *
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - GET operations: Available to all authenticated users
 * - POST/PUT/DELETE: ADMIN role only
 */
@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private Gson gson;

    @Override
    public void init() {
        this.productService = new ProductService();
        this.gson = JsonUtil.GSON;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            String pathInfo = req.getPathInfo();

            // GET /api/products/{id} - Get product by UUID
            if (pathInfo != null && pathInfo.length() > 1) {
                String productId = pathInfo.substring(1);
                Product product = productService.getProduct(productId);

                if (product != null) {
                    response.addProperty("success", true);
                    response.add("product", gson.toJsonTree(product));
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Product not found");
                    resp.setStatus(404);
                }
            }
            // GET /api/products - List all products (with optional filters)
            else {
                String categoryIdParam = req.getParameter("categoryId");
                String brandIdParam = req.getParameter("brandId");
                String supplierIdParam = req.getParameter("supplierId");

                List<Product> products;

                if (categoryIdParam != null && !categoryIdParam.isEmpty()) {
                    products = productService.getProductsByCategory(categoryIdParam);
                } else if (brandIdParam != null && !brandIdParam.isEmpty()) {
                    products = productService.getProductsByBrand(brandIdParam);
                } else if (supplierIdParam != null && !supplierIdParam.isEmpty()) {
                    products = productService.getProductsBySupplier(supplierIdParam);
                } else {
                    products = productService.getAllProducts();
                }

                response.addProperty("success", true);
                response.addProperty("count", products.size());
                response.add("products", gson.toJsonTree(products));
                resp.setStatus(200);
            }

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            // Check if user is ADMIN
            HttpSession session = req.getSession(false);
            if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Admin role required");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Read request body
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                requestBody.append(line);
            }

            JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            // Parse product data
            Product product = new Product();
            product.setName(requestJson.get("name").getAsString());
            product.setReference(requestJson.get("reference").getAsString());
            product.setDescription(requestJson.has("description") && !requestJson.get("description").isJsonNull() ? requestJson.get("description").getAsString() : null);
            product.setUnitPrice(new BigDecimal(requestJson.get("unitPrice").getAsString()));
            product.setWarrantyMonths(requestJson.has("warrantyMonths") && !requestJson.get("warrantyMonths").isJsonNull() ? requestJson.get("warrantyMonths").getAsInt() : 0);
            product.setImageUrl(requestJson.has("imageUrl") && !requestJson.get("imageUrl").isJsonNull() ? requestJson.get("imageUrl").getAsString() : null);
            product.setCategoryId(requestJson.has("categoryId") && !requestJson.get("categoryId").isJsonNull() ? requestJson.get("categoryId").getAsString() : null);
            product.setBrandId(requestJson.has("brandId") && !requestJson.get("brandId").isJsonNull() ? requestJson.get("brandId").getAsString() : null);
            product.setSupplierId(requestJson.has("supplierId") && !requestJson.get("supplierId").isJsonNull() ? requestJson.get("supplierId").getAsString() : null);

            // Create product
            Product createdProduct = productService.createProduct(product);

            response.addProperty("success", true);
            response.addProperty("message", "Product created successfully");
            response.add("product", gson.toJsonTree(createdProduct));
            resp.setStatus(201);

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            // Check if user is ADMIN
            HttpSession session = req.getSession(false);
            if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Admin role required");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract product UUID from path
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid product ID");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String productId = pathInfo.substring(1);

            // Read request body
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                requestBody.append(line);
            }

            JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            // Parse product data
            Product product = new Product();
            product.setId(productId);
            product.setName(requestJson.get("name").getAsString());
            product.setReference(requestJson.get("reference").getAsString());
            product.setDescription(requestJson.has("description") && !requestJson.get("description").isJsonNull() ? requestJson.get("description").getAsString() : null);
            product.setUnitPrice(new BigDecimal(requestJson.get("unitPrice").getAsString()));
            product.setWarrantyMonths(requestJson.has("warrantyMonths") && !requestJson.get("warrantyMonths").isJsonNull() ? requestJson.get("warrantyMonths").getAsInt() : 0);
            product.setImageUrl(requestJson.has("imageUrl") && !requestJson.get("imageUrl").isJsonNull() ? requestJson.get("imageUrl").getAsString() : null);
            product.setCategoryId(requestJson.has("categoryId") && !requestJson.get("categoryId").isJsonNull() ? requestJson.get("categoryId").getAsString() : null);
            product.setBrandId(requestJson.has("brandId") && !requestJson.get("brandId").isJsonNull() ? requestJson.get("brandId").getAsString() : null);
            product.setSupplierId(requestJson.has("supplierId") && !requestJson.get("supplierId").isJsonNull() ? requestJson.get("supplierId").getAsString() : null);

            // Update product
            boolean updated = productService.updateProduct(product);

            if (updated) {
                response.addProperty("success", true);
                response.addProperty("message", "Product updated successfully");
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Failed to update product");
                resp.setStatus(400);
            }

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            // Check if user is ADMIN
            HttpSession session = req.getSession(false);
            if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Admin role required");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract product UUID from path
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid product ID");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String productId = pathInfo.substring(1);

            // Delete product
            boolean deleted = productService.deleteProduct(productId);

            if (deleted) {
                response.addProperty("success", true);
                response.addProperty("message", "Product deleted successfully");
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Failed to delete product");
                resp.setStatus(400);
            }

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}