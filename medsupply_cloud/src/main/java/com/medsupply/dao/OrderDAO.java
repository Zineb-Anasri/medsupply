package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Order;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * OrderDAO - Data Access Object for Order table
 * Handles all database operations for orders
 */
public class OrderDAO {

    /**
     * Find order by ID
     * @param orderId Order ID (UUID)
     * @return Order object if found, null otherwise
     */
    public Order findById(String orderId) throws Exception {
        String filters = "id=eq." + orderId;
        String response = SupabaseClient.get("orders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToOrder(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find order by quote ID
     * @param quoteId Quote ID
     * @return Order object if found, null otherwise
     */
    public Order findByQuoteId(String quoteId) throws Exception {
        String filters = "quote_id=eq." + quoteId;
        String response = SupabaseClient.get("orders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToOrder(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all orders
     * @return List of all orders
     */
    public List<Order> findAll() throws Exception {
        String response = SupabaseClient.get("orders", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            orders.add(mapJsonToOrder(jsonArray.get(i).getAsJsonObject()));
        }
        return orders;
    }

    /**
     * Find orders by client ID
     * @param clientId Client ID
     * @return List of orders for the client
     */
    public List<Order> findByClientId(String clientId) throws Exception {
        String filters = "client_id=eq." + clientId + "&order=created_at.desc";
        String response = SupabaseClient.get("orders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            orders.add(mapJsonToOrder(jsonArray.get(i).getAsJsonObject()));
        }
        return orders;
    }

    /**
     * Find orders by status
     * @param status Order status
     * @return List of orders with the status
     */
    public List<Order> findByStatus(String status) throws Exception {
        String filters = "status=eq." + status + "&order=created_at.desc";
        String response = SupabaseClient.get("orders", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            orders.add(mapJsonToOrder(jsonArray.get(i).getAsJsonObject()));
        }
        return orders;
    }

    /**
     * Create a new order
     * @param order Order object to create
     * @return Created order
     */
    public Order create(Order order) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", order.getOrderId());
        body.addProperty("quote_id", order.getQuoteId());
        body.addProperty("client_id", order.getClientId());
        body.addProperty("status", order.getStatus());
        body.addProperty("total_amount", order.getTotalAmount());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("orders", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToOrder(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update order status
     * @param orderId Order ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String orderId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + orderId;
        String response = SupabaseClient.patchWithFilters("orders", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Update order total amount
     * @param orderId Order ID
     * @param totalAmount New total amount
     * @return true if update successful
     */
    public boolean updateTotalAmount(String orderId, BigDecimal totalAmount) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("total_amount", totalAmount);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + orderId;
        String response = SupabaseClient.patchWithFilters("orders", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Map JSON to Order object
     */
    private Order mapJsonToOrder(JsonObject json) {
        Order order = new Order();
        if (json.has("id") && !json.get("id").isJsonNull())
            order.setOrderId(json.get("id").getAsString());
        if (json.has("quote_id") && !json.get("quote_id").isJsonNull())
            order.setQuoteId(json.get("quote_id").getAsString());
        if (json.has("client_id") && !json.get("client_id").isJsonNull())
            order.setClientId(json.get("client_id").getAsString());
        if (json.has("status") && !json.get("status").isJsonNull())
            order.setStatus(json.get("status").getAsString());
        if (json.has("total_amount") && !json.get("total_amount").isJsonNull())
            order.setTotalAmount(new BigDecimal(json.get("total_amount").getAsString()));
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                order.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                order.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return order;
    }
}
