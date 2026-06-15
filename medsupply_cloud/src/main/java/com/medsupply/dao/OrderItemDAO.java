package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.OrderItem;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * OrderItemDAO - Data Access Object for OrderItem table
 * Handles all database operations for order items
 */
public class OrderItemDAO {

    /**
     * Find order item by ID
     * @param itemId Order Item ID (UUID)
     * @return OrderItem object if found, null otherwise
     */
    public OrderItem findById(String itemId) throws Exception {
        String filters = "id=eq." + itemId;
        String response = SupabaseClient.get("order_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToOrderItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all items for an order
     * @param orderId Order ID
     * @return List of order items
     */
    public List<OrderItem> findByOrderId(String orderId) throws Exception {
        String filters = "order_id=eq." + orderId + "&order=created_at.asc";
        String response = SupabaseClient.get("order_items", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            items.add(mapJsonToOrderItem(jsonArray.get(i).getAsJsonObject()));
        }
        return items;
    }

    /**
     * Create a new order item
     * @param item OrderItem object to create
     * @return Created order item
     */
    public OrderItem create(OrderItem item) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", item.getItemId());
        body.addProperty("order_id", item.getOrderId());
        body.addProperty("product_id", item.getProductId());
        body.addProperty("quantity", item.getQuantity());
        body.addProperty("unit_price", item.getUnitPrice());
        body.addProperty("total_price", item.getTotalPrice());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("order_items", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToOrderItem(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update order item quantity
     * @param itemId Order Item ID
     * @param quantity New quantity
     * @return true if update successful
     */
    public boolean updateQuantity(String itemId, Integer quantity) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("quantity", quantity);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + itemId;
        String response = SupabaseClient.patchWithFilters("order_items", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Delete order item
     * @param itemId Order Item ID
     * @return true if deletion successful
     */
    public boolean delete(String itemId) throws Exception {
        String response = SupabaseClient.delete("order_items", itemId);
        return !response.isEmpty();
    }

    /**
     * Delete all items for an order
     * @param orderId Order ID
     * @return true if deletion successful
     */
    public boolean deleteByOrderId(String orderId) throws Exception {
        String response = SupabaseClient.delete("order_items", orderId);
        return !response.isEmpty();
    }

    /**
     * Map JSON to OrderItem object
     */
    private OrderItem mapJsonToOrderItem(JsonObject json) {
        OrderItem item = new OrderItem();
        if (json.has("id") && !json.get("id").isJsonNull())
            item.setItemId(json.get("id").getAsString());
        if (json.has("order_id") && !json.get("order_id").isJsonNull())
            item.setOrderId(json.get("order_id").getAsString());
        if (json.has("product_id") && !json.get("product_id").isJsonNull())
            item.setProductId(json.get("product_id").getAsString());
        if (json.has("quantity") && !json.get("quantity").isJsonNull())
            item.setQuantity(json.get("quantity").getAsInt());
        if (json.has("unit_price") && !json.get("unit_price").isJsonNull())
            item.setUnitPrice(new BigDecimal(json.get("unit_price").getAsString()));
        if (json.has("total_price") && !json.get("total_price").isJsonNull())
            item.setTotalPrice(new BigDecimal(json.get("total_price").getAsString()));
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
