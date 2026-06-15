package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Delivery;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * DeliveryDAO - Data Access Object for Delivery table
 * Handles all database operations for deliveries
 */
public class DeliveryDAO {

    /**
     * Find delivery by ID
     * @param deliveryId Delivery ID (UUID)
     * @return Delivery object if found, null otherwise
     */
    public Delivery findById(String deliveryId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(deliveryId);
        String response = SupabaseClient.get("deliveries", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToDelivery(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find delivery by order ID
     * @param orderId Order ID
     * @return Delivery object if found, null otherwise
     */
    public Delivery findByOrderId(String orderId) throws Exception {
        String filters = "order_id=eq." + SupabaseClient.enc(orderId);
        String response = SupabaseClient.get("deliveries", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToDelivery(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all deliveries
     * @return List of all deliveries
     */
    public List<Delivery> findAll() throws Exception {
        String response = SupabaseClient.get("deliveries", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            deliveries.add(mapJsonToDelivery(jsonArray.get(i).getAsJsonObject()));
        }
        return deliveries;
    }

    /**
     * Find deliveries by client ID
     * @param clientId Client ID
     * @return List of deliveries for the client
     */
    public List<Delivery> findByClientId(String clientId) throws Exception {
        // Deliveries have no client_id column; PostgREST does not support SQL subqueries,
        // so resolve the client's order ids first, then filter by order_id=in.(...).
        List<Delivery> deliveries = new ArrayList<>();
        String ordersResp = SupabaseClient.get("orders",
            "client_id=eq." + SupabaseClient.enc(clientId) + "&select=id");
        JsonArray orderRows = SupabaseClient.parseJsonArray(ordersResp);
        if (orderRows == null || orderRows.size() == 0) {
            return deliveries;
        }
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < orderRows.size(); i++) {
            if (i > 0) ids.append(",");
            ids.append(SupabaseClient.enc(orderRows.get(i).getAsJsonObject().get("id").getAsString()));
        }

        String filters = "order_id=in.(" + ids + ")&order=created_at.desc";
        String response = SupabaseClient.get("deliveries", filters);

        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        for (int i = 0; i < jsonArray.size(); i++) {
            Delivery delivery = mapJsonToDelivery(jsonArray.get(i).getAsJsonObject());
            delivery.setClientId(clientId);
            deliveries.add(delivery);
        }
        return deliveries;
    }

    /**
     * Find deliveries by status
     * @param status Delivery status
     * @return List of deliveries with the status
     */
    public List<Delivery> findByStatus(String status) throws Exception {
        String filters = "status=eq." + SupabaseClient.enc(status) + "&order=created_at.desc";
        String response = SupabaseClient.get("deliveries", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            deliveries.add(mapJsonToDelivery(jsonArray.get(i).getAsJsonObject()));
        }
        return deliveries;
    }

    /**
     * Find active deliveries (PENDING, PREPARING, IN_TRANSIT)
     * @return List of active deliveries
     */
    public List<Delivery> findActive() throws Exception {
        String filters = "status=in.(PENDING,PREPARING,IN_TRANSIT)&order=created_at.asc";
        String response = SupabaseClient.get("deliveries", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            deliveries.add(mapJsonToDelivery(jsonArray.get(i).getAsJsonObject()));
        }
        return deliveries;
    }

    /**
     * Create a new delivery
     * @param delivery Delivery object to create
     * @return Created delivery
     */
    public Delivery create(Delivery delivery) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", delivery.getDeliveryId());
        body.addProperty("order_id", delivery.getOrderId());
        body.addProperty("status", delivery.getStatus());
        body.addProperty("delivery_address", delivery.getDeliveryAddress());
        body.addProperty("contact_phone", delivery.getContactPhone());
        body.addProperty("contact_person", delivery.getContactPerson());
        body.addProperty("current_latitude", delivery.getCurrentLatitude());
        body.addProperty("current_longitude", delivery.getCurrentLongitude());
        body.addProperty("last_location_update", delivery.getLastLocationUpdate() != null ? delivery.getLastLocationUpdate().toString() : null);
        body.addProperty("tracking_url", delivery.getTrackingUrl());
        body.addProperty("estimated_delivery_date", delivery.getEstimatedDeliveryDate() != null ? delivery.getEstimatedDeliveryDate().toString() : null);
        body.addProperty("actual_delivery_date", delivery.getActualDeliveryDate() != null ? delivery.getActualDeliveryDate().toString() : null);
        body.addProperty("delivery_notes", delivery.getDeliveryNotes());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("deliveries", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToDelivery(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update delivery status
     * @param deliveryId Delivery ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String deliveryId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(deliveryId);
        String response = SupabaseClient.patchWithFilters("deliveries", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update GPS location (prepared for Supabase Realtime integration)
     * @param deliveryId Delivery ID
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true if update successful
     */
    public boolean updateLocation(String deliveryId, Double latitude, Double longitude) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("current_latitude", latitude);
        body.addProperty("current_longitude", longitude);
        body.addProperty("last_location_update", LocalDateTime.now().toString());
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(deliveryId);
        String response = SupabaseClient.patchWithFilters("deliveries", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update delivery details
     * @param deliveryId Delivery ID
     * @param deliveryAddress Delivery address
     * @param contactPhone Contact phone
     * @param contactPerson Contact person
     * @param estimatedDeliveryDate Estimated delivery date
     * @return true if update successful
     */
    public boolean updateDetails(String deliveryId, String deliveryAddress, String contactPhone, 
                                  String contactPerson, LocalDateTime estimatedDeliveryDate) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("delivery_address", deliveryAddress);
        body.addProperty("contact_phone", contactPhone);
        body.addProperty("contact_person", contactPerson);
        body.addProperty("estimated_delivery_date", estimatedDeliveryDate != null ? estimatedDeliveryDate.toString() : null);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(deliveryId);
        String response = SupabaseClient.patchWithFilters("deliveries", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Mark delivery as delivered
     * @param deliveryId Delivery ID
     * @param actualDeliveryDate Actual delivery date
     * @param deliveryNotes Delivery notes
     * @return true if update successful
     */
    public boolean markDelivered(String deliveryId, LocalDateTime actualDeliveryDate, 
                                  String deliveryNotes) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", "DELIVERED");
        body.addProperty("actual_delivery_date", actualDeliveryDate.toString());
        body.addProperty("delivery_notes", deliveryNotes);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(deliveryId);
        String response = SupabaseClient.patchWithFilters("deliveries", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to Delivery object
     */
    private Delivery mapJsonToDelivery(JsonObject json) {
        Delivery delivery = new Delivery();
        if (json.has("id") && !json.get("id").isJsonNull())
            delivery.setDeliveryId(json.get("id").getAsString());
        if (json.has("order_id") && !json.get("order_id").isJsonNull())
            delivery.setOrderId(json.get("order_id").getAsString());
        if (json.has("status") && !json.get("status").isJsonNull())
            delivery.setStatus(json.get("status").getAsString());
        if (json.has("delivery_address") && !json.get("delivery_address").isJsonNull())
            delivery.setDeliveryAddress(json.get("delivery_address").getAsString());
        if (json.has("contact_phone") && !json.get("contact_phone").isJsonNull())
            delivery.setContactPhone(json.get("contact_phone").getAsString());
        if (json.has("contact_person") && !json.get("contact_person").isJsonNull())
            delivery.setContactPerson(json.get("contact_person").getAsString());
        if (json.has("current_latitude") && !json.get("current_latitude").isJsonNull())
            delivery.setCurrentLatitude(json.get("current_latitude").getAsDouble());
        if (json.has("current_longitude") && !json.get("current_longitude").isJsonNull())
            delivery.setCurrentLongitude(json.get("current_longitude").getAsDouble());
        if (json.has("last_location_update") && !json.get("last_location_update").isJsonNull()) {
            try {
                String dateStr = json.get("last_location_update").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                delivery.setLastLocationUpdate(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("tracking_url") && !json.get("tracking_url").isJsonNull())
            delivery.setTrackingUrl(json.get("tracking_url").getAsString());
        if (json.has("estimated_delivery_date") && !json.get("estimated_delivery_date").isJsonNull()) {
            try {
                String dateStr = json.get("estimated_delivery_date").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                delivery.setEstimatedDeliveryDate(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("actual_delivery_date") && !json.get("actual_delivery_date").isJsonNull()) {
            try {
                String dateStr = json.get("actual_delivery_date").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                delivery.setActualDeliveryDate(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("delivery_notes") && !json.get("delivery_notes").isJsonNull())
            delivery.setDeliveryNotes(json.get("delivery_notes").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                delivery.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                delivery.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return delivery;
    }
}
