package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.SupplierBid;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * SupplierBidDAO - Data Access Object for SupplierBid table
 * Handles all database operations for supplier bids
 */
public class SupplierBidDAO {

    /**
     * Find bid by ID
     * @param bidId Bid ID (UUID)
     * @return SupplierBid object if found, null otherwise
     */
    public SupplierBid findById(String bidId) throws Exception {
        String filters = "bid_id=eq." + SupabaseClient.enc(bidId);
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBid(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find bids by tender ID
     * @param tenderId Tender ID
     * @return List of bids for the tender
     */
    public List<SupplierBid> findByTenderId(String tenderId) throws Exception {
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId) + "&order=price.asc,created_at.asc";
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<SupplierBid> bids = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            bids.add(mapJsonToBid(jsonArray.get(i).getAsJsonObject()));
        }
        return bids;
    }

    /**
     * Find bids by supplier ID
     * @param supplierId Supplier ID
     * @return List of bids from the supplier
     */
    public List<SupplierBid> findBySupplierId(String supplierId) throws Exception {
        String filters = "supplier_id=eq." + SupabaseClient.enc(supplierId) + "&order=created_at.desc";
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<SupplierBid> bids = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            bids.add(mapJsonToBid(jsonArray.get(i).getAsJsonObject()));
        }
        return bids;
    }

    /**
     * Find best bid for a tender (lowest price)
     * @param tenderId Tender ID
     * @return Best bid if found, null otherwise
     */
    public SupplierBid findBestBid(String tenderId) throws Exception {
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId) + "&order=price.asc&limit=1";
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBid(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find latest bid from a supplier for a tender
     * @param tenderId Tender ID
     * @param supplierId Supplier ID
     * @return Latest bid if found, null otherwise
     */
    public SupplierBid findLatestBid(String tenderId, String supplierId) throws Exception {
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId) + "&supplier_id=eq." + SupabaseClient.enc(supplierId) + "&order=created_at.desc&limit=1";
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBid(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Create a new bid
     * @param bid SupplierBid object to create
     * @return Created bid
     */
    public SupplierBid create(SupplierBid bid) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("bid_id", bid.getBidId());
        body.addProperty("tender_id", bid.getTenderId());
        body.addProperty("supplier_id", bid.getSupplierId());
        body.addProperty("price", bid.getPrice());
        body.addProperty("created_at", bid.getCreatedAt().toString());
        
        String response = SupabaseClient.post("supplier_bids", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToBid(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Delete bid
     * @param bidId Bid ID
     * @return true if deletion successful
     */
    public boolean delete(String bidId) throws Exception {
        String response = SupabaseClient.delete("supplier_bids", bidId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete bids by tender ID
     * @param tenderId Tender ID
     * @return true if deletion successful
     */
    public boolean deleteByTenderId(String tenderId) throws Exception {
        String response = SupabaseClient.delete("supplier_bids", tenderId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Get bid count for a tender
     * @param tenderId Tender ID
     * @return Number of bids
     */
    public int countByTenderId(String tenderId) throws Exception {
        String filters = "tender_id=eq." + SupabaseClient.enc(tenderId);
        String response = SupabaseClient.get("supplier_bids", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Map JSON to SupplierBid object
     */
    private SupplierBid mapJsonToBid(JsonObject json) {
        SupplierBid bid = new SupplierBid();
        if (json.has("bid_id") && !json.get("bid_id").isJsonNull())
            bid.setBidId(json.get("bid_id").getAsString());
        if (json.has("tender_id") && !json.get("tender_id").isJsonNull())
            bid.setTenderId(json.get("tender_id").getAsString());
        if (json.has("supplier_id") && !json.get("supplier_id").isJsonNull())
            bid.setSupplierId(json.get("supplier_id").getAsString());
        if (json.has("price") && !json.get("price").isJsonNull())
            bid.setPrice(new BigDecimal(json.get("price").getAsString()));
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                bid.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return bid;
    }
}
