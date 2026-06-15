package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Tender;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * TenderDAO - Data Access Object for Tender table
 * Handles all database operations for tenders
 */
public class TenderDAO {

    /**
     * Find tender by ID
     * @param tenderId Tender ID (UUID)
     * @return Tender object if found, null otherwise
     */
    public Tender findById(String tenderId) throws Exception {
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId);
        String response = SupabaseClient.get("tenders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToTender(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find tenders by client ID
     * @param clientId Client ID
     * @return List of tenders for the client
     */
    public List<Tender> findByClientId(String clientId) throws Exception {
        String filters = "client_id=eq." + SupabaseClient.enc(clientId) + "&order=created_at.desc";
        String response = SupabaseClient.get("tenders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Tender> tenders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            tenders.add(mapJsonToTender(jsonArray.get(i).getAsJsonObject()));
        }
        return tenders;
    }

    /**
     * Find tenders by status
     * @param status Tender status
     * @return List of tenders with the status
     */
    public List<Tender> findByStatus(String status) throws Exception {
        String filters = "status=eq." + SupabaseClient.enc(status) + "&order=created_at.desc";
        String response = SupabaseClient.get("tenders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Tender> tenders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            tenders.add(mapJsonToTender(jsonArray.get(i).getAsJsonObject()));
        }
        return tenders;
    }

    /**
     * Find open tenders
     * @return List of open tenders
     */
    public List<Tender> findOpen() throws Exception {
        String filters = "status=eq.OPEN&deadline=gt." + LocalDateTime.now().toString() + "&order=created_at.desc";
        String response = SupabaseClient.get("tenders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Tender> tenders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            tenders.add(mapJsonToTender(jsonArray.get(i).getAsJsonObject()));
        }
        return tenders;
    }

    /**
     * Find all tenders
     * @return List of all tenders
     */
    public List<Tender> findAll() throws Exception {
        String response = SupabaseClient.get("tenders", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Tender> tenders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            tenders.add(mapJsonToTender(jsonArray.get(i).getAsJsonObject()));
        }
        return tenders;
    }

    /**
     * Create a new tender
     * @param tender Tender object to create
     * @return Created tender
     */
    public Tender create(Tender tender) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("tender_id", tender.getTenderId());
        body.addProperty("client_id", tender.getClientId());
        body.addProperty("title", tender.getTitle());
        body.addProperty("description", tender.getDescription());
        body.addProperty("budget_max", tender.getBudgetMax());
        body.addProperty("deadline", tender.getDeadline().toString());
        body.addProperty("status", tender.getStatus());
        body.addProperty("created_at", tender.getCreatedAt().toString());
        body.addProperty("updated_at", tender.getUpdatedAt().toString());
        
        String response = SupabaseClient.post("tenders", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToTender(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update tender status
     * @param tenderId Tender ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String tenderId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId);
        String response = SupabaseClient.patchWithFilters("tenders", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update tender
     * @param tender Tender object to update
     * @return true if update successful
     */
    public boolean update(Tender tender) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("title", tender.getTitle());
        body.addProperty("description", tender.getDescription());
        body.addProperty("budget_max", tender.getBudgetMax());
        body.addProperty("deadline", tender.getDeadline().toString());
        body.addProperty("status", tender.getStatus());
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "tender_id=eq." + SupabaseClient.enc(tender.getTenderId());
        String response = SupabaseClient.patchWithFilters("tenders", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete tender
     * @param tenderId Tender ID
     * @return true if deletion successful
     */
    public boolean delete(String tenderId) throws Exception {
        String response = SupabaseClient.deleteWithFilters("tenders",
            "tender_id=eq." + SupabaseClient.enc(tenderId));
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to Tender object
     */
    private Tender mapJsonToTender(JsonObject json) {
        Tender tender = new Tender();
        if (json.has("tender_id") && !json.get("tender_id").isJsonNull())
            tender.setTenderId(json.get("tender_id").getAsString());
        if (json.has("client_id") && !json.get("client_id").isJsonNull())
            tender.setClientId(json.get("client_id").getAsString());
        if (json.has("title") && !json.get("title").isJsonNull())
            tender.setTitle(json.get("title").getAsString());
        if (json.has("description") && !json.get("description").isJsonNull())
            tender.setDescription(json.get("description").getAsString());
        if (json.has("budget_max") && !json.get("budget_max").isJsonNull())
            tender.setBudgetMax(new BigDecimal(json.get("budget_max").getAsString()));
        if (json.has("deadline") && !json.get("deadline").isJsonNull()) {
            try {
                String dateStr = json.get("deadline").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                tender.setDeadline(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("status") && !json.get("status").isJsonNull())
            tender.setStatus(json.get("status").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                tender.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                tender.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return tender;
    }
}
