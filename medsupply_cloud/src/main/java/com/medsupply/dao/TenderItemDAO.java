package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.TenderItem;
import com.medsupply.utils.SupabaseClient;

import java.util.ArrayList;
import java.util.List;
/**
 * TenderItemDAO - Data Access Object for TenderItem table
 * Handles all database operations for tender items
 */
public class TenderItemDAO {

    /**
     * Find tender item by ID
     * @param tenderItemId Tender Item ID (UUID)
     * @return TenderItem object if found, null otherwise
     */
    public TenderItem findById(String tenderItemId) throws Exception {
        String filters = "tender_item_id=eq." + tenderItemId;
        String response = SupabaseClient.get("tender_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToTenderItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find tender items by tender ID
     * @param tenderId Tender ID
     * @return List of items for the tender
     */
    public List<TenderItem> findByTenderId(String tenderId) throws Exception {
        String filters = "tender_id=eq." + tenderId;
        String response = SupabaseClient.get("tender_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<TenderItem> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            items.add(mapJsonToTenderItem(jsonArray.get(i).getAsJsonObject()));
        }
        return items;
    }

    /**
     * Create a new tender item
     * @param item TenderItem object to create
     * @return Created tender item
     */
    public TenderItem create(TenderItem item) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("tender_item_id", item.getTenderItemId());
        body.addProperty("tender_id", item.getTenderId());
        body.addProperty("product_id", item.getProductId());
        body.addProperty("quantity", item.getQuantity());
        
        String response = SupabaseClient.post("tender_items", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToTenderItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Create multiple tender items for a tender
     * @param items List of tender items to create
     * @return true if all items created successfully
     */
    public boolean createBatch(List<TenderItem> items) throws Exception {
        for (TenderItem item : items) {
            TenderItem created = create(item);
            if (created == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Delete tender items by tender ID
     * @param tenderId Tender ID
     * @return true if deletion successful
     */
    public boolean deleteByTenderId(String tenderId) throws Exception {
        String response = SupabaseClient.delete("tender_items", tenderId);
        return !response.isEmpty();
    }

    /**
     * Delete tender item
     * @param tenderItemId Tender Item ID
     * @return true if deletion successful
     */
    public boolean delete(String tenderItemId) throws Exception {
        String response = SupabaseClient.delete("tender_items", tenderItemId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to TenderItem object
     */
    private TenderItem mapJsonToTenderItem(JsonObject json) {
        TenderItem item = new TenderItem();
        if (json.has("tender_item_id") && !json.get("tender_item_id").isJsonNull())
            item.setTenderItemId(json.get("tender_item_id").getAsString());
        if (json.has("tender_id") && !json.get("tender_id").isJsonNull())
            item.setTenderId(json.get("tender_id").getAsString());
        if (json.has("product_id") && !json.get("product_id").isJsonNull())
            item.setProductId(json.get("product_id").getAsString());
        if (json.has("quantity") && !json.get("quantity").isJsonNull())
            item.setQuantity(json.get("quantity").getAsInt());
        return item;
    }
}
