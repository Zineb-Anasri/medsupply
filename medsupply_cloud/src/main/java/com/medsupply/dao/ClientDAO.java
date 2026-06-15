package com.medsupply.dao;

import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Client;
import com.medsupply.utils.SupabaseClient;

/**
 * ClientDAO - Data Access Object for Client table
 * Handles all database operations for client profiles
 */
public class ClientDAO {

    /**
     * Find client by user ID
     * @param userId User ID
     * @return Client object if found, null otherwise
     */
    public Client findByUserId(String userId) throws Exception {
        String filters = "user_id=eq." + SupabaseClient.enc(userId);
        String response = SupabaseClient.get("clients", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToClient(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Compatibility overload for legacy callers that still pass numeric IDs.
     * New auth code must use the UUID string overload.
     */
    @Deprecated
    public Client findByUserId(Integer userId) throws Exception {
        return findByUserId(String.valueOf(userId));
    }

    /**
     * Find client by client ID
     * @param clientId Client ID
     * @return Client object if found, null otherwise
     */
    public Client findById(String clientId) throws Exception {
        String filters = "client_id=eq." + SupabaseClient.enc(clientId);
        String response = SupabaseClient.get("clients", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            JsonObject json = jsonArray.get(0).getAsJsonObject();
            Client client = new Client();
            if (json.has("id") && !json.get("id").isJsonNull()) {
                client.setClientId(json.get("id").getAsString());
            }
            if (json.has("user_id") && !json.get("user_id").isJsonNull()) {
                client.setUserId(json.get("user_id").getAsString());
            }
            if (json.has("organization_name") && !json.get("organization_name").isJsonNull()) {
                client.setName(json.get("organization_name").getAsString());
            }
            if (json.has("type") && !json.get("type").isJsonNull()) {
                client.setType(json.get("type").getAsString());
            }
            if (json.has("address") && !json.get("address").isJsonNull()) {
                client.setAddress(json.get("address").getAsString());
            }
            if (json.has("phone") && !json.get("phone").isJsonNull()) {
                client.setPhone(json.get("phone").getAsString());
            }
            if (json.has("email") && !json.get("email").isJsonNull()) {
                client.setEmail(json.get("email").getAsString());
            }
            if (json.has("ice") && !json.get("ice").isJsonNull()) {
                client.setTaxId(json.get("ice").getAsString());
            }
            if (json.has("credit_eligible") && !json.get("credit_eligible").isJsonNull()) {
                client.setCreditEligible(json.get("credit_eligible").getAsBoolean());
            }
            if (json.has("credit_days") && !json.get("credit_days").isJsonNull()) {
                client.setCreditDays(json.get("credit_days").getAsInt());
            }
            if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
                try {
                    String dateStr = json.get("created_at").getAsString()
                        .replace("Z", "")
                        .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                        .replaceAll("\\.[0-9]+$", "");
                    client.setCreatedAt(LocalDateTime.parse(dateStr));
                } catch (Exception ignored) {}
            }
            return client;
        }
        return null;
    }

    /**
     * Find all client profiles (ADMIN listing / name resolution).
     * @return List of all clients
     */
    public java.util.List<Client> findAll() throws Exception {
        String response = SupabaseClient.get("clients", "order=organization_name.asc");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        java.util.List<Client> clients = new java.util.ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            clients.add(mapJsonToClient(jsonArray.get(i).getAsJsonObject()));
        }
        return clients;
    }

    /**
     * Create a new client profile
     * @param client Client object to create
     * @return Created client with generated ID
     */
    public Client create(Client client) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", client.getUserId());
        body.addProperty("organization_name", client.getName());
        body.addProperty("type", client.getType());
        body.addProperty("address", client.getAddress());
        body.addProperty("phone", client.getPhone());
        body.addProperty("email", client.getEmail());
        body.addProperty("ice", client.getTaxId());
        body.addProperty("credit_eligible", client.getCreditEligible());
        body.addProperty("credit_days", client.getCreditDays());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        
        String response = SupabaseClient.post("clients", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToClient(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update client profile
     * @param client Client object to update
     * @return true if update successful
     */
    public boolean update(Client client) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("organization_name", client.getName());
        body.addProperty("type", client.getType());
        body.addProperty("address", client.getAddress());
        body.addProperty("phone", client.getPhone());
        body.addProperty("email", client.getEmail());
        body.addProperty("ice", client.getTaxId());
        body.addProperty("credit_eligible", client.getCreditEligible());
        body.addProperty("credit_days", client.getCreditDays());
        
        String response = SupabaseClient.patch("clients", client.getClientId().toString(), body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete client profile
     * @param clientId Client ID
     * @return true if deletion successful
     */
    public boolean delete(String clientId) throws Exception {
        String response = SupabaseClient.delete("clients", clientId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to Client object
     */
    private Client mapJsonToClient(JsonObject json) {
        Client client = new Client();
        if (json.has("id") && !json.get("id").isJsonNull()) {
            client.setClientId(json.get("id").getAsString());
        }
        if (json.has("user_id") && !json.get("user_id").isJsonNull()) {
            client.setUserId(json.get("user_id").getAsString());
        }
        if (json.has("organization_name") && !json.get("organization_name").isJsonNull()) {
            client.setName(json.get("organization_name").getAsString());
        }
        if (json.has("type") && !json.get("type").isJsonNull()) {
            client.setType(json.get("type").getAsString());
        }
        if (json.has("address") && !json.get("address").isJsonNull()) {
            client.setAddress(json.get("address").getAsString());
        }
        if (json.has("phone") && !json.get("phone").isJsonNull()) {
            client.setPhone(json.get("phone").getAsString());
        }
        if (json.has("email") && !json.get("email").isJsonNull()) {
            client.setEmail(json.get("email").getAsString());
        }
        if (json.has("ice") && !json.get("ice").isJsonNull()) {
            client.setTaxId(json.get("ice").getAsString());
        }
        if (json.has("credit_eligible") && !json.get("credit_eligible").isJsonNull()) {
            client.setCreditEligible(json.get("credit_eligible").getAsBoolean());
        }
        if (json.has("credit_days") && !json.get("credit_days").isJsonNull()) {
            client.setCreditDays(json.get("credit_days").getAsInt());
        }
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                client.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return client;
    }
}
