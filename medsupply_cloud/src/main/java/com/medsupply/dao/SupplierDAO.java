package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Supplier;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;

/**
 * SupplierDAO - Data Access Object for Supplier table
 * Handles all database operations for supplier profiles
 */
public class SupplierDAO {

    /**
     * Find supplier by user ID
     * @param userId User ID
     * @return Supplier object if found, null otherwise
     */
    public Supplier findByUserId(String userId) throws Exception {
        String filters = "user_id=eq." + SupabaseClient.enc(userId);
        String response = SupabaseClient.get("suppliers", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToSupplier(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Compatibility overload for legacy callers that still pass numeric IDs.
     * New auth code must use the UUID string overload.
     */
    @Deprecated
    public Supplier findByUserId(Integer userId) throws Exception {
        return findByUserId(String.valueOf(userId));
    }

    /**
     * Find supplier by supplier ID
     * @param supplierId Supplier ID
     * @return Supplier object if found, null otherwise
     */
    public Supplier findById(String supplierId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(supplierId);
        String response = SupabaseClient.get("suppliers", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToSupplier(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all supplier profiles (ADMIN listing / name resolution).
     * @return List of all suppliers
     */
    public java.util.List<Supplier> findAll() throws Exception {
        String response = SupabaseClient.get("suppliers", "order=company_name.asc");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        java.util.List<Supplier> suppliers = new java.util.ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            suppliers.add(mapJsonToSupplier(jsonArray.get(i).getAsJsonObject()));
        }
        return suppliers;
    }

    /**
     * Create a new supplier profile
     * @param supplier Supplier object to create
     * @return Created supplier with generated ID
     */
    public Supplier create(Supplier supplier) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", supplier.getUserId());
        body.addProperty("company_name", supplier.getCompanyName());
        body.addProperty("contact_name", supplier.getContactPerson());
        body.addProperty("email", supplier.getEmail());
        body.addProperty("phone", supplier.getPhone());
        body.addProperty("address", supplier.getAddress());
        body.addProperty("is_active", true);
        body.addProperty("created_at", LocalDateTime.now().toString());

        String response = SupabaseClient.post("suppliers", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToSupplier(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update supplier profile
     * @param supplier Supplier object to update
     * @return true if update successful
     */
    public boolean update(Supplier supplier) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("company_name", supplier.getCompanyName());
        body.addProperty("contact_name", supplier.getContactPerson());
        body.addProperty("email", supplier.getEmail());
        body.addProperty("phone", supplier.getPhone());
        body.addProperty("address", supplier.getAddress());

        String response = SupabaseClient.patch("suppliers", supplier.getSupplierId().toString(), body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Verify supplier account
     * @param supplierId Supplier ID
     * @return true if verification successful
     */
    public boolean verify(String supplierId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_verified", true);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String response = SupabaseClient.patch("suppliers", supplierId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update supplier rating
     * @param supplierId Supplier ID
     * @param rating New rating
     * @return true if update successful
     */
    public boolean updateRating(String supplierId, Double rating) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("rating", rating);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String response = SupabaseClient.patch("suppliers", supplierId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete supplier profile
     * @param supplierId Supplier ID
     * @return true if deletion successful
     */
    public boolean delete(String supplierId) throws Exception {
        String response = SupabaseClient.delete("suppliers", supplierId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to Supplier object
     */
    private Supplier mapJsonToSupplier(JsonObject json) {
        Supplier supplier = new Supplier();
        if (json.has("id") && !json.get("id").isJsonNull())
            supplier.setSupplierId(json.get("id").getAsString());
        if (json.has("user_id") && !json.get("user_id").isJsonNull())
            supplier.setUserId(json.get("user_id").getAsString());
        if (json.has("company_name") && !json.get("company_name").isJsonNull())
            supplier.setCompanyName(json.get("company_name").getAsString());
        if (json.has("contact_name") && !json.get("contact_name").isJsonNull())
            supplier.setContactPerson(json.get("contact_name").getAsString());
        if (json.has("address") && !json.get("address").isJsonNull())
            supplier.setAddress(json.get("address").getAsString());
        if (json.has("phone") && !json.get("phone").isJsonNull())
            supplier.setPhone(json.get("phone").getAsString());
        if (json.has("email") && !json.get("email").isJsonNull())
            supplier.setEmail(json.get("email").getAsString());
        if (json.has("tax_id") && !json.get("tax_id").isJsonNull())
            supplier.setTaxId(json.get("tax_id").getAsString());
        if (json.has("license_number") && !json.get("license_number").isJsonNull())
            supplier.setLicenseNumber(json.get("license_number").getAsString());
        if (json.has("is_active") && !json.get("is_active").isJsonNull())
            supplier.setIsVerified(json.get("is_active").getAsBoolean());
        if (json.has("rating") && !json.get("rating").isJsonNull())
            supplier.setRating(json.get("rating").getAsDouble());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                supplier.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                supplier.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return supplier;
    }
}
