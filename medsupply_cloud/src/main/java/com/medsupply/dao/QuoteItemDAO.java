package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.QuoteItem;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * QuoteItemDAO - Data Access Object for QuoteItem table
 * Handles all database operations for quote items
 */
public class QuoteItemDAO {

    /**
     * Find quote item by ID
     * @param itemId Quote Item ID (UUID)
     * @return QuoteItem object if found, null otherwise
     */
    public QuoteItem findById(String itemId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(itemId);
        String response = SupabaseClient.get("quote_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToQuoteItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all items for a quote
     * @param quoteId Quote ID
     * @return List of quote items
     */
    public List<QuoteItem> findByQuoteId(String quoteId) throws Exception {
        String filters = "quote_id=eq." + SupabaseClient.enc(quoteId) + "&order=created_at.asc";
        String response = SupabaseClient.get("quote_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<QuoteItem> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            items.add(mapJsonToQuoteItem(jsonArray.get(i).getAsJsonObject()));
        }
        return items;
    }

    /**
     * Create a new quote item
     * @param item QuoteItem object to create
     * @return Created quote item
     */
    public QuoteItem create(QuoteItem item) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", item.getItemId());
        body.addProperty("quote_id", item.getQuoteId());
        body.addProperty("product_id", item.getProductId());
        body.addProperty("quantity", item.getQuantity());
        body.addProperty("proposed_price", item.getProposedPrice());
        body.addProperty("admin_price", item.getAdminPrice());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("quote_items", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToQuoteItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update quote item admin price
     * @param itemId Quote Item ID
     * @param adminPrice Admin's proposed price
     * @return true if update successful
     */
    public boolean updateAdminPrice(String itemId, BigDecimal adminPrice) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("admin_price", adminPrice);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(itemId);
        String response = SupabaseClient.patchWithFilters("quote_items", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update quote item quantity
     * @param itemId Quote Item ID
     * @param quantity New quantity
     * @return true if update successful
     */
    public boolean updateQuantity(String itemId, Integer quantity) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("quantity", quantity);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(itemId);
        String response = SupabaseClient.patchWithFilters("quote_items", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete quote item
     * @param itemId Quote Item ID
     * @return true if deletion successful
     */
    public boolean delete(String itemId) throws Exception {
        String response = SupabaseClient.delete("quote_items", itemId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete all items for a quote
     * @param quoteId Quote ID
     * @return true if deletion successful
     */
    public boolean deleteByQuoteId(String quoteId) throws Exception {
        String response = SupabaseClient.deleteWithFilters("quote_items",
            "quote_id=eq." + SupabaseClient.enc(quoteId));
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to QuoteItem object
     */
    private QuoteItem mapJsonToQuoteItem(JsonObject json) {
        QuoteItem item = new QuoteItem();
        if (json.has("id") && !json.get("id").isJsonNull())
            item.setItemId(json.get("id").getAsString());
        if (json.has("quote_id") && !json.get("quote_id").isJsonNull())
            item.setQuoteId(json.get("quote_id").getAsString());
        if (json.has("product_id") && !json.get("product_id").isJsonNull())
            item.setProductId(json.get("product_id").getAsString());
        if (json.has("quantity") && !json.get("quantity").isJsonNull())
            item.setQuantity(json.get("quantity").getAsInt());
        if (json.has("proposed_price") && !json.get("proposed_price").isJsonNull())
            item.setProposedPrice(new BigDecimal(json.get("proposed_price").getAsString()));
        if (json.has("admin_price") && !json.get("admin_price").isJsonNull())
            item.setAdminPrice(new BigDecimal(json.get("admin_price").getAsString()));
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                item.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                item.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return item;
    }
}
