package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.MaintenanceRequest;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * MaintenanceRequestDAO - Data Access Object for MaintenanceRequest table
 * Handles all database operations for maintenance requests
 */
public class MaintenanceRequestDAO {

    /**
     * Find request by ID
     * @param requestId Request ID (UUID)
     * @return MaintenanceRequest object if found, null otherwise
     */
    public MaintenanceRequest findById(String requestId) throws Exception {
        String filters = "request_id=eq." + SupabaseClient.enc(requestId);
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToRequest(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find requests by contract ID
     * @param contractId Contract ID
     * @return List of requests for the contract
     */
    public List<MaintenanceRequest> findByContractId(String contractId) throws Exception {
        String filters = "contract_id=eq." + SupabaseClient.enc(contractId) + "&order=request_date.desc";
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Find requests by client ID
     * @param clientId Client ID
     * @return List of requests for the client
     */
    public List<MaintenanceRequest> findByClientId(String clientId) throws Exception {
        String filters = "client_id=eq." + SupabaseClient.enc(clientId) + "&order=request_date.desc";
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Find requests by product ID
     * @param productId Product ID
     * @return List of requests for the product
     */
    public List<MaintenanceRequest> findByProductId(String productId) throws Exception {
        String filters = "product_id=eq." + SupabaseClient.enc(productId) + "&order=request_date.desc";
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Find requests by status
     * @param status Request status
     * @return List of requests with the status
     */
    public List<MaintenanceRequest> findByStatus(String status) throws Exception {
        String filters = "status=eq." + SupabaseClient.enc(status) + "&order=request_date.desc";
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Find requests by priority
     * @param priority Request priority
     * @return List of requests with the priority
     */
    public List<MaintenanceRequest> findByPriority(String priority) throws Exception {
        String filters = "priority=eq." + SupabaseClient.enc(priority) + "&order=request_date.desc";
        String response = SupabaseClient.get("maintenance_requests", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Find all requests
     * @return List of all requests
     */
    public List<MaintenanceRequest> findAll() throws Exception {
        String response = SupabaseClient.get("maintenance_requests", "order=request_date.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceRequest> requests = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            requests.add(mapJsonToRequest(jsonArray.get(i).getAsJsonObject()));
        }
        return requests;
    }

    /**
     * Create a new request
     * @param request MaintenanceRequest object to create
     * @return Created request
     */
    public MaintenanceRequest create(MaintenanceRequest request) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("request_id", request.getRequestId());
        body.addProperty("contract_id", request.getContractId());
        body.addProperty("client_id", request.getClientId());
        body.addProperty("product_id", request.getProductId());
        body.addProperty("description", request.getDescription());
        body.addProperty("priority", request.getPriority());
        body.addProperty("status", request.getStatus());
        body.addProperty("request_date", request.getRequestDate().toString());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("maintenance_requests", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToRequest(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update request status
     * @param requestId Request ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String requestId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "request_id=eq." + SupabaseClient.enc(requestId);
        String response = SupabaseClient.patchWithFilters("maintenance_requests", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update request priority
     * @param requestId Request ID
     * @param priority New priority
     * @return true if update successful
     */
    public boolean updatePriority(String requestId, String priority) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("priority", priority);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "request_id=eq." + SupabaseClient.enc(requestId);
        String response = SupabaseClient.patchWithFilters("maintenance_requests", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete request
     * @param requestId Request ID
     * @return true if deletion successful
     */
    public boolean delete(String requestId) throws Exception {
        // PK column for maintenance_requests is request_id (not id)
        String response = SupabaseClient.deleteWithFilters("maintenance_requests",
            "request_id=eq." + SupabaseClient.enc(requestId));
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to MaintenanceRequest object
     */
    private MaintenanceRequest mapJsonToRequest(JsonObject json) {
        MaintenanceRequest request = new MaintenanceRequest();
        if (json.has("request_id") && !json.get("request_id").isJsonNull())
            request.setRequestId(json.get("request_id").getAsString());
        if (json.has("contract_id") && !json.get("contract_id").isJsonNull())
            request.setContractId(json.get("contract_id").getAsString());
        if (json.has("client_id") && !json.get("client_id").isJsonNull())
            request.setClientId(json.get("client_id").getAsString());
        if (json.has("product_id") && !json.get("product_id").isJsonNull())
            request.setProductId(json.get("product_id").getAsString());
        if (json.has("description") && !json.get("description").isJsonNull())
            request.setDescription(json.get("description").getAsString());
        if (json.has("priority") && !json.get("priority").isJsonNull())
            request.setPriority(json.get("priority").getAsString());
        if (json.has("status") && !json.get("status").isJsonNull())
            request.setStatus(json.get("status").getAsString());
        if (json.has("request_date") && !json.get("request_date").isJsonNull()) {
            try {
                String dateStr = json.get("request_date").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                request.setRequestDate(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                request.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                request.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return request;
    }
}
