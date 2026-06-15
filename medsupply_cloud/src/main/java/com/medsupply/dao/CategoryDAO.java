package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Category;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO - Data Access Object for Category table
 * Handles all database operations for categories
 */
public class CategoryDAO {

    /**
     * Find category by ID
     * @param categoryId Category ID
     * @return Category object if found, null otherwise
     */
    public Category findById(String categoryId) throws Exception {
        String filters = "category_id=eq." + categoryId;
        String response = SupabaseClient.get("categories", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToCategory(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all categories
     * @return List of all categories
     */
    public List<Category> findAll() throws Exception {
        String response = SupabaseClient.get("categories", "order=name.asc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            categories.add(mapJsonToCategory(jsonArray.get(i).getAsJsonObject()));
        }
        return categories;
    }

    /**
     * Create a new category
     * @param category Category object to create
     * @return Created category with generated ID
     */
    public Category create(Category category) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", category.getName());
        body.addProperty("description", category.getDescription());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("categories", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToCategory(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update category
     * @param category Category object to update
     * @return true if update successful
     */
    public boolean update(Category category) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", category.getName());
        body.addProperty("description", category.getDescription());
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String response = SupabaseClient.patch("categories", category.getCategoryId().toString(), body.toString());
        return !response.isEmpty();
    }

    /**
     * Delete category
     * @param categoryId Category ID
     * @return true if deletion successful
     */
    public boolean delete(String categoryId) throws Exception {
        String response = SupabaseClient.delete("categories", categoryId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to Category object
     */
    private Category mapJsonToCategory(JsonObject json) {
        Category category = new Category();
        if (json.has("category_id") && !json.get("category_id").isJsonNull())
            category.setCategoryId(json.get("category_id").getAsString());
        if (json.has("name") && !json.get("name").isJsonNull())
            category.setName(json.get("name").getAsString());
        if (json.has("description") && !json.get("description").isJsonNull())
            category.setDescription(json.get("description").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                category.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                category.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return category;
    }
}
