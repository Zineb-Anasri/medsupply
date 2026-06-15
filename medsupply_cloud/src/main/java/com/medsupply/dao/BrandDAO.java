package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Brand;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BrandDAO - Data Access Object for Brand table
 * Handles all database operations for brands
 */
public class BrandDAO {

    /**
     * Find brand by ID
     * @param brandId Brand ID
     * @return Brand object if found, null otherwise
     */
    public Brand findById(String brandId) throws Exception {
        String filters = "brand_id=eq." + brandId;
        String response = SupabaseClient.get("brands", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBrand(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all brands
     * @return List of all brands
     */
    public List<Brand> findAll() throws Exception {
        String response = SupabaseClient.get("brands", "order=name.asc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Brand> brands = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            brands.add(mapJsonToBrand(jsonArray.get(i).getAsJsonObject()));
        }
        return brands;
    }

    /**
     * Create a new brand
     * @param brand Brand object to create
     * @return Created brand with generated ID
     */
    public Brand create(Brand brand) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", brand.getName());
        body.addProperty("description", brand.getDescription());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("brands", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBrand(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update brand
     * @param brand Brand object to update
     * @return true if update successful
     */
    public boolean update(Brand brand) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", brand.getName());
        body.addProperty("description", brand.getDescription());
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String response = SupabaseClient.patch("brands", brand.getBrandId().toString(), body.toString());
        return !response.isEmpty();
    }

    /**
     * Delete brand
     * @param brandId Brand ID
     * @return true if deletion successful
     */
    public boolean delete(String brandId) throws Exception {
        String response = SupabaseClient.delete("brands", brandId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to Brand object
     */
    private Brand mapJsonToBrand(JsonObject json) {
        Brand brand = new Brand();
        if (json.has("brand_id") && !json.get("brand_id").isJsonNull())
            brand.setBrandId(json.get("brand_id").getAsString());
        if (json.has("name") && !json.get("name").isJsonNull())
            brand.setName(json.get("name").getAsString());
        if (json.has("description") && !json.get("description").isJsonNull())
            brand.setDescription(json.get("description").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                brand.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                brand.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return brand;
    }
}
