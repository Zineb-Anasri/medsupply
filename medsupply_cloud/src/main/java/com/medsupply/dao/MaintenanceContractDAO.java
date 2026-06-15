package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.MaintenanceContract;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * MaintenanceContractDAO - Data Access Object for MaintenanceContract table
 * Handles all database operations for maintenance contracts
 */
public class MaintenanceContractDAO {

    /**
     * Find contract by ID
     * @param contractId Contract ID (UUID)
     * @return MaintenanceContract object if found, null otherwise
     */
    public MaintenanceContract findById(String contractId) throws Exception {
        String filters = "id=eq." + contractId;
        String response = SupabaseClient.get("maintenance_contracts", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToContract(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find contracts by client ID
     * @param clientId Client ID
     * @return List of contracts for the client
     */
    public List<MaintenanceContract> findByClientId(String clientId) throws Exception {
        String filters = "client_id=eq." + clientId + "&order=created_at.desc";
        String response = SupabaseClient.get("maintenance_contracts", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceContract> contracts = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            contracts.add(mapJsonToContract(jsonArray.get(i).getAsJsonObject()));
        }
        return contracts;
    }

    /**
     * Find contracts by product ID
     * @param productId Product ID
     * @return List of contracts for the product
     */
    public List<MaintenanceContract> findByProductId(String productId) throws Exception {
        String filters = "product_id=eq." + productId + "&order=created_at.desc";
        String response = SupabaseClient.get("maintenance_contracts", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceContract> contracts = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            contracts.add(mapJsonToContract(jsonArray.get(i).getAsJsonObject()));
        }
        return contracts;
    }

    /**
     * Find contracts by status
     * @param status Contract status
     * @return List of contracts with the status
     */
    public List<MaintenanceContract> findByStatus(String status) throws Exception {
        String filters = "status=eq." + status + "&order=created_at.desc";
        String response = SupabaseClient.get("maintenance_contracts", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceContract> contracts = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            contracts.add(mapJsonToContract(jsonArray.get(i).getAsJsonObject()));
        }
        return contracts;
    }

    /**
     * Find active contracts
     * @return List of active contracts
     */
    public List<MaintenanceContract> findActive() throws Exception {
        String filters = "status=eq.ACTIVE&(end_date=is.null|end_date=gte." + LocalDate.now().toString() + ")&order=created_at.desc";
        String response = SupabaseClient.get("maintenance_contracts", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceContract> contracts = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            contracts.add(mapJsonToContract(jsonArray.get(i).getAsJsonObject()));
        }
        return contracts;
    }

    /**
     * Find all contracts
     * @return List of all contracts
     */
    public List<MaintenanceContract> findAll() throws Exception {
        String response = SupabaseClient.get("maintenance_contracts", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<MaintenanceContract> contracts = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            contracts.add(mapJsonToContract(jsonArray.get(i).getAsJsonObject()));
        }
        return contracts;
    }

    /**
     * Create a new contract
     * @param contract MaintenanceContract object to create
     * @return Created contract
     */
    public MaintenanceContract create(MaintenanceContract contract) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", contract.getContractId());
        body.addProperty("client_id", contract.getClientId());
        body.addProperty("product_id", contract.getProductId());
        body.addProperty("warranty_duration", contract.getWarrantyDuration());
        body.addProperty("start_date", contract.getStartDate() != null ? contract.getStartDate().toString() : null);
        body.addProperty("end_date", contract.getEndDate() != null ? contract.getEndDate().toString() : null);
        body.addProperty("status", contract.getStatus());
        body.addProperty("contract_reference", contract.getContractReference());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("maintenance_contracts", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToContract(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update contract status
     * @param contractId Contract ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String contractId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + contractId;
        String response = SupabaseClient.patchWithFilters("maintenance_contracts", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Update contract end date
     * @param contractId Contract ID
     * @param endDate New end date
     * @return true if update successful
     */
    public boolean updateEndDate(String contractId, LocalDate endDate) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("end_date", endDate.toString());
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + contractId;
        String response = SupabaseClient.patchWithFilters("maintenance_contracts", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Delete contract
     * @param contractId Contract ID
     * @return true if deletion successful
     */
    public boolean delete(String contractId) throws Exception {
        String response = SupabaseClient.delete("maintenance_contracts", contractId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to MaintenanceContract object
     */
    private MaintenanceContract mapJsonToContract(JsonObject json) {
        MaintenanceContract contract = new MaintenanceContract();
        if (json.has("id") && !json.get("id").isJsonNull())
            contract.setContractId(json.get("id").getAsString());
        if (json.has("client_id") && !json.get("client_id").isJsonNull())
            contract.setClientId(json.get("client_id").getAsString());
        if (json.has("product_id") && !json.get("product_id").isJsonNull())
            contract.setProductId(json.get("product_id").getAsString());
        if (json.has("warranty_duration") && !json.get("warranty_duration").isJsonNull())
            contract.setWarrantyDuration(json.get("warranty_duration").getAsInt());
        if (json.has("start_date") && !json.get("start_date").isJsonNull()) {
            try {
                contract.setStartDate(LocalDate.parse(json.get("start_date").getAsString()));
            } catch (Exception ignored) {}
        }
        if (json.has("end_date") && !json.get("end_date").isJsonNull()) {
            try {
                contract.setEndDate(LocalDate.parse(json.get("end_date").getAsString()));
            } catch (Exception ignored) {}
        }
        if (json.has("status") && !json.get("status").isJsonNull())
            contract.setStatus(json.get("status").getAsString());
        if (json.has("contract_reference") && !json.get("contract_reference").isJsonNull())
            contract.setContractReference(json.get("contract_reference").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                contract.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                contract.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return contract;
    }
}
