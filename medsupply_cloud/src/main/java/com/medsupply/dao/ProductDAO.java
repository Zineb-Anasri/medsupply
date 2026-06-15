package com.medsupply.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Product;
import com.medsupply.utils.SupabaseClient;

public class ProductDAO {

    public Product findById(String productId) throws Exception {
        String response = SupabaseClient.get("products", "id=eq." + SupabaseClient.enc(productId));
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        if (arr.size() > 0) return mapJsonToProduct(arr.get(0).getAsJsonObject());
        return null;
    }

    public List<Product> findAll() throws Exception {
        String response = SupabaseClient.get("products", "is_active=eq.true");
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try { list.add(mapJsonToProduct(arr.get(i).getAsJsonObject())); } catch (Exception ignored) {}
        }
        return list;
    }

    public List<Product> findByCategory(String categoryId) throws Exception {
        String response = SupabaseClient.get("products", "category_id=eq." + SupabaseClient.enc(categoryId) + "&is_active=eq.true");
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try { list.add(mapJsonToProduct(arr.get(i).getAsJsonObject())); } catch (Exception ignored) {}
        }
        return list;
    }

    public List<Product> findByBrand(String brandId) throws Exception {
        String response = SupabaseClient.get("products", "brand_id=eq." + SupabaseClient.enc(brandId) + "&is_active=eq.true");
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try { list.add(mapJsonToProduct(arr.get(i).getAsJsonObject())); } catch (Exception ignored) {}
        }
        return list;
    }

    public List<Product> findBySupplier(String supplierId) throws Exception {
        String response = SupabaseClient.get("products", "supplier_id=eq." + SupabaseClient.enc(supplierId) + "&is_active=eq.true");
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try { list.add(mapJsonToProduct(arr.get(i).getAsJsonObject())); } catch (Exception ignored) {}
        }
        return list;
    }

    public Product create(Product product) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", product.getName());
        body.addProperty("reference", product.getReference());
        body.addProperty("description", product.getDescription());
        body.addProperty("unit_price", product.getUnitPrice());
        body.addProperty("warranty_months", product.getWarrantyMonths());
        body.addProperty("image_url", product.getImageUrl());
        body.addProperty("category_id", product.getCategoryId());
        body.addProperty("brand_id", product.getBrandId());
        body.addProperty("supplier_id", product.getSupplierId());
        body.addProperty("is_active", true);
        String response = SupabaseClient.post("products", body.toString());
        JsonArray arr = SupabaseClient.parseJsonArray(response);
        if (arr.size() > 0) return mapJsonToProduct(arr.get(0).getAsJsonObject());
        return null;
    }

    public boolean update(Product product) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", product.getName());
        body.addProperty("reference", product.getReference());
        body.addProperty("description", product.getDescription());
        body.addProperty("unit_price", product.getUnitPrice());
        body.addProperty("warranty_months", product.getWarrantyMonths());
        body.addProperty("image_url", product.getImageUrl());
        body.addProperty("category_id", product.getCategoryId());
        body.addProperty("brand_id", product.getBrandId());
        body.addProperty("supplier_id", product.getSupplierId());
        String response = SupabaseClient.patch("products", product.getId(), body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    public boolean delete(String productId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_active", false);
        String response = SupabaseClient.patch("products", productId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    private Product mapJsonToProduct(JsonObject json) {
        Product product = new Product();
        if (json.has("id") && !json.get("id").isJsonNull()) product.setId(json.get("id").getAsString());
        if (json.has("name") && !json.get("name").isJsonNull()) product.setName(json.get("name").getAsString());
        if (json.has("reference") && !json.get("reference").isJsonNull()) product.setReference(json.get("reference").getAsString());
        if (json.has("description") && !json.get("description").isJsonNull()) product.setDescription(json.get("description").getAsString());
        if (json.has("unit_price") && !json.get("unit_price").isJsonNull()) product.setUnitPrice(new BigDecimal(json.get("unit_price").getAsString()));
        if (json.has("warranty_months") && !json.get("warranty_months").isJsonNull()) product.setWarrantyMonths(json.get("warranty_months").getAsInt());
        if (json.has("image_url") && !json.get("image_url").isJsonNull()) product.setImageUrl(json.get("image_url").getAsString());
        if (json.has("category_id") && !json.get("category_id").isJsonNull()) product.setCategoryId(json.get("category_id").getAsString());
        if (json.has("brand_id") && !json.get("brand_id").isJsonNull()) product.setBrandId(json.get("brand_id").getAsString());
        if (json.has("supplier_id") && !json.get("supplier_id").isJsonNull()) product.setSupplierId(json.get("supplier_id").getAsString());
        if (json.has("is_active") && !json.get("is_active").isJsonNull()) product.setIsActive(json.get("is_active").getAsBoolean());
        return product;
    }
}