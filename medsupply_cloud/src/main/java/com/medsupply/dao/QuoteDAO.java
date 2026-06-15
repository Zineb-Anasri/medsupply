package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Quote;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * QuoteDAO - Data Access Object for Quote table
 * Handles all database operations for quotes
 */
public class QuoteDAO {

    /**
     * Find quote by ID
     * @param quoteId Quote ID (UUID)
     * @return Quote object if found, null otherwise
     */
    public Quote findById(String quoteId) throws Exception {
        String filters = "id=eq." + quoteId;
        String response = SupabaseClient.get("quotes", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToQuote(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all quotes
     * @return List of all quotes
     */
    public List<Quote> findAll() throws Exception {
        String response = SupabaseClient.get("quotes", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            quotes.add(mapJsonToQuote(jsonArray.get(i).getAsJsonObject()));
        }
        return quotes;
    }

    /**
     * Find quotes by client ID
     * @param clientId Client ID
     * @return List of quotes for the client
     */
    public List<Quote> findByClientId(String clientId) throws Exception {
        String filters = "client_id=eq." + clientId + "&order=created_at.desc";
        String response = SupabaseClient.get("quotes", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            quotes.add(mapJsonToQuote(jsonArray.get(i).getAsJsonObject()));
        }
        return quotes;
    }

    /**
     * Find quotes by status
     * @param status Quote status
     * @return List of quotes with the status
     */
    public List<Quote> findByStatus(String status) throws Exception {
        String filters = "status=eq." + status + "&order=created_at.desc";
        String response = SupabaseClient.get("quotes", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            quotes.add(mapJsonToQuote(jsonArray.get(i).getAsJsonObject()));
        }
        return quotes;
    }

    /**
     * Create a new quote
     * @param quote Quote object to create
     * @return Created quote
     */
    public Quote create(Quote quote) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", quote.getQuoteId());
        body.addProperty("client_id", quote.getClientId());
        body.addProperty("status", quote.getStatus());
        body.addProperty("total_amount", quote.getTotalAmount());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("quotes", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToQuote(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update quote status
     * @param quoteId Quote ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String quoteId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + quoteId;
        String response = SupabaseClient.patchWithFilters("quotes", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Update quote total amount
     * @param quoteId Quote ID
     * @param totalAmount New total amount
     * @return true if update successful
     */
    public boolean updateTotalAmount(String quoteId, BigDecimal totalAmount) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("total_amount", totalAmount);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + quoteId;
        String response = SupabaseClient.patchWithFilters("quotes", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Delete quote
     * @param quoteId Quote ID
     * @return true if deletion successful
     */
    public boolean delete(String quoteId) throws Exception {
        String filters = "id=eq." + quoteId;
        String response = SupabaseClient.delete("quotes", quoteId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to Quote object
     */
    private Quote mapJsonToQuote(JsonObject json) {
        Quote quote = new Quote();
        if (json.has("id") && !json.get("id").isJsonNull())
            quote.setQuoteId(json.get("id").getAsString());
        if (json.has("client_id") && !json.get("client_id").isJsonNull())
            quote.setClientId(json.get("client_id").getAsString());
        if (json.has("status") && !json.get("status").isJsonNull())
            quote.setStatus(json.get("status").getAsString());
        if (json.has("total_amount") && !json.get("total_amount").isJsonNull())
            quote.setTotalAmount(new BigDecimal(json.get("total_amount").getAsString()));
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                quote.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                quote.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return quote;
    }
}
