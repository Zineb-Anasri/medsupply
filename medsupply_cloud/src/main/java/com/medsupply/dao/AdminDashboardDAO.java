package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboardDAO - Data Access Object for Admin Dashboard analytics
 * Handles data aggregation from existing tables for business intelligence and reporting
 * 
 * This DAO performs data aggregation from existing tables without modifying database structure
 * Since Supabase REST API doesn't support complex SQL aggregations, data is fetched and aggregated in Java
 */
public class AdminDashboardDAO {

    // ==================== SALES OVERVIEW ====================

    /**
     * Get total number of orders
     */
    public int getTotalOrders() throws Exception {
        String response = SupabaseClient.get("orders", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Get total revenue from orders (sum of all orders)
     */
    public double getTotalRevenue() throws Exception {
        String response = SupabaseClient.get("orders", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        double total = 0.0;
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject order = jsonArray.get(i).getAsJsonObject();
            total += order.get("total_amount").getAsDouble();
        }
        return total;
    }

    /**
     * Get orders count by status
     */
    public Map<String, Integer> getOrdersByStatus() throws Exception {
        String response = SupabaseClient.get("orders", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        Map<String, Integer> statusCounts = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject order = jsonArray.get(i).getAsJsonObject();
            String status = order.get("status").getAsString();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        return statusCounts;
    }

    // ==================== FINANCIAL OVERVIEW ====================

    /**
     * Get total payments received
     */
    public double getTotalPaymentsReceived() throws Exception {
        String filters = "status=eq.PAID";
        String response = SupabaseClient.get("payments", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        double total = 0.0;
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject payment = jsonArray.get(i).getAsJsonObject();
            total += payment.get("paid_amount").getAsDouble();
        }
        return total;
    }

    /**
     * Get outstanding receivables (unpaid or partially paid orders)
     */
    public double getOutstandingReceivables() throws Exception {
        String filters = "status=in.(PENDING,PROCESSING,SHIPPED)";
        String ordersResponse = SupabaseClient.get("orders", filters);
        JsonArray ordersArray = SupabaseClient.parseJsonArray(ordersResponse);
        
        double total = 0.0;
        for (int i = 0; i < ordersArray.size(); i++) {
            JsonObject order = ordersArray.get(i).getAsJsonObject();
            String orderId = order.get("order_id").getAsString();
            double orderTotal = order.get("total_amount").getAsDouble();
            
            // Get payments for this order
            String paymentFilters = "order_id=eq." + SupabaseClient.enc(orderId);
            String paymentsResponse = SupabaseClient.get("payments", paymentFilters);
            JsonArray paymentsArray = SupabaseClient.parseJsonArray(paymentsResponse);
            
            double paidAmount = 0.0;
            for (int j = 0; j < paymentsArray.size(); j++) {
                JsonObject payment = paymentsArray.get(j).getAsJsonObject();
                paidAmount += payment.get("paid_amount").getAsDouble();
            }
            
            total += (orderTotal - paidAmount);
        }
        return total;
    }

    /**
     * Get overdue payments (based on due_date)
     */
    public double getOverduePayments() throws Exception {
        String response = SupabaseClient.get("payments", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        LocalDate today = LocalDate.now();
        double total = 0.0;
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject payment = jsonArray.get(i).getAsJsonObject();
            String status = payment.get("status").getAsString();
            if (!"PAID".equals(status) && payment.has("due_date") && !payment.get("due_date").isJsonNull()) {
                LocalDate dueDate = LocalDate.parse(payment.get("due_date").getAsString());
                if (dueDate.isBefore(today)) {
                    double amount = payment.get("amount").getAsDouble();
                    double paidAmount = payment.get("paid_amount").getAsDouble();
                    total += (amount - paidAmount);
                }
            }
        }
        return total;
    }

    /**
     * Get clients with highest debt
     */
    public List<Map<String, Object>> getClientsWithHighestDebt() throws Exception {
        String clientsResponse = SupabaseClient.get("clients", "");
        JsonArray clientsArray = SupabaseClient.parseJsonArray(clientsResponse);
        
        Map<Integer, Map<String, Object>> clientDebtMap = new HashMap<>();
        
        for (int i = 0; i < clientsArray.size(); i++) {
            JsonObject client = clientsArray.get(i).getAsJsonObject();
            Integer clientId = client.get("client_id").getAsInt();
            
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", clientId);
            clientData.put("companyName", client.get("company_name").getAsString());
            clientData.put("clientType", client.get("client_type").getAsString());
            clientData.put("debt", 0.0);
            
            clientDebtMap.put(clientId, clientData);
        }
        
        // Get orders and calculate debt
        String ordersResponse = SupabaseClient.get("orders", "");
        JsonArray ordersArray = SupabaseClient.parseJsonArray(ordersResponse);
        
        for (int i = 0; i < ordersArray.size(); i++) {
            JsonObject order = ordersArray.get(i).getAsJsonObject();
            String status = order.get("status").getAsString();
            if ("PENDING".equals(status) || "PROCESSING".equals(status) || "SHIPPED".equals(status)) {
                Integer clientId = order.get("client_id").getAsInt();
                String orderId = order.get("order_id").getAsString();
                double orderTotal = order.get("total_amount").getAsDouble();
                
                // Get payments for this order
                String paymentFilters = "order_id=eq." + SupabaseClient.enc(orderId);
                String paymentsResponse = SupabaseClient.get("payments", paymentFilters);
                JsonArray paymentsArray = SupabaseClient.parseJsonArray(paymentsResponse);
                
                double paidAmount = 0.0;
                boolean allPaid = false;
                for (int j = 0; j < paymentsArray.size(); j++) {
                    JsonObject payment = paymentsArray.get(j).getAsJsonObject();
                    paidAmount += payment.get("paid_amount").getAsDouble();
                    if ("PAID".equals(payment.get("status").getAsString())) {
                        allPaid = true;
                    }
                }
                
                if (!allPaid && clientDebtMap.containsKey(clientId)) {
                    Map<String, Object> clientData = clientDebtMap.get(clientId);
                    double currentDebt = (Double) clientData.get("debt");
                    clientData.put("debt", currentDebt + (orderTotal - paidAmount));
                }
            }
        }
        
        // Convert to list and sort by debt
        List<Map<String, Object>> clients = new ArrayList<>(clientDebtMap.values());
        clients.sort((a, b) -> Double.compare((Double) b.get("debt"), (Double) a.get("debt")));
        
        // Return top 10
        return clients.subList(0, Math.min(10, clients.size()));
    }

    // ==================== CLIENT ANALYTICS ====================

    /**
     * Get total number of clients
     */
    public int getTotalClients() throws Exception {
        String response = SupabaseClient.get("clients", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Get clients by type
     */
    public Map<String, Integer> getClientsByType() throws Exception {
        String response = SupabaseClient.get("clients", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        Map<String, Integer> typeCounts = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject client = jsonArray.get(i).getAsJsonObject();
            String type = client.get("client_type").getAsString();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        return typeCounts;
    }

    /**
     * Get most active clients (by number of orders)
     */
    public List<Map<String, Object>> getMostActiveClients() throws Exception {
        String clientsResponse = SupabaseClient.get("clients", "");
        JsonArray clientsArray = SupabaseClient.parseJsonArray(clientsResponse);
        
        Map<Integer, Map<String, Object>> clientOrderMap = new HashMap<>();
        
        for (int i = 0; i < clientsArray.size(); i++) {
            JsonObject client = clientsArray.get(i).getAsJsonObject();
            Integer clientId = client.get("client_id").getAsInt();
            
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", clientId);
            clientData.put("companyName", client.get("company_name").getAsString());
            clientData.put("clientType", client.get("client_type").getAsString());
            clientData.put("orderCount", 0);
            
            clientOrderMap.put(clientId, clientData);
        }
        
        // Count orders per client
        String ordersResponse = SupabaseClient.get("orders", "");
        JsonArray ordersArray = SupabaseClient.parseJsonArray(ordersResponse);
        
        for (int i = 0; i < ordersArray.size(); i++) {
            JsonObject order = ordersArray.get(i).getAsJsonObject();
            Integer clientId = order.get("client_id").getAsInt();
            
            if (clientOrderMap.containsKey(clientId)) {
                Map<String, Object> clientData = clientOrderMap.get(clientId);
                int currentCount = (Integer) clientData.get("orderCount");
                clientData.put("orderCount", currentCount + 1);
            }
        }
        
        // Convert to list and sort by order count
        List<Map<String, Object>> clients = new ArrayList<>(clientOrderMap.values());
        clients.sort((a, b) -> Integer.compare((Integer) b.get("orderCount"), (Integer) a.get("orderCount")));
        
        // Return top 10
        return clients.subList(0, Math.min(10, clients.size()));
    }

    // ==================== PRODUCT ANALYTICS ====================

    /**
     * Get total number of products
     */
    public int getTotalProducts() throws Exception {
        String response = SupabaseClient.get("products", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Get low stock products (stock < min_stock)
     */
    public List<Map<String, Object>> getLowStockProducts() throws Exception {
        String response = SupabaseClient.get("products", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject product = jsonArray.get(i).getAsJsonObject();
            int stock = product.get("stock").getAsInt();
            int minStock = product.get("min_stock").getAsInt();
            
            if (stock < minStock) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("productId", product.get("product_id").getAsInt());
                productData.put("name", product.get("name").getAsString());
                productData.put("category", product.get("category").getAsString());
                productData.put("stock", stock);
                productData.put("minStock", minStock);
                products.add(productData);
            }
        }
        
        // Sort by stock ascending
        products.sort((a, b) -> Integer.compare((Integer) a.get("stock"), (Integer) b.get("stock")));
        
        // Return top 20
        return products.subList(0, Math.min(20, products.size()));
    }

    /**
     * Get most sold products (based on order_items)
     */
    public List<Map<String, Object>> getMostSoldProducts() throws Exception {
        String productsResponse = SupabaseClient.get("products", "");
        JsonArray productsArray = SupabaseClient.parseJsonArray(productsResponse);
        
        Map<Integer, Map<String, Object>> productSalesMap = new HashMap<>();
        
        for (int i = 0; i < productsArray.size(); i++) {
            JsonObject product = productsArray.get(i).getAsJsonObject();
            Integer productId = product.get("product_id").getAsInt();
            
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", productId);
            productData.put("name", product.get("name").getAsString());
            productData.put("category", product.get("category").getAsString());
            productData.put("totalSold", 0);
            
            productSalesMap.put(productId, productData);
        }
        
        // Sum quantities from order_items
        String orderItemsResponse = SupabaseClient.get("order_items", "");
        JsonArray orderItemsArray = SupabaseClient.parseJsonArray(orderItemsResponse);
        
        for (int i = 0; i < orderItemsArray.size(); i++) {
            JsonObject orderItem = orderItemsArray.get(i).getAsJsonObject();
            Integer productId = orderItem.get("product_id").getAsInt();
            int quantity = orderItem.get("quantity").getAsInt();
            
            if (productSalesMap.containsKey(productId)) {
                Map<String, Object> productData = productSalesMap.get(productId);
                int currentSold = (Integer) productData.get("totalSold");
                productData.put("totalSold", currentSold + quantity);
            }
        }
        
        // Convert to list and sort by total sold
        List<Map<String, Object>> products = new ArrayList<>(productSalesMap.values());
        products.sort((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")));
        
        // Return top 10
        return products.subList(0, Math.min(10, products.size()));
    }

    /**
     * Get products by category
     */
    public Map<String, Integer> getProductsByCategory() throws Exception {
        String response = SupabaseClient.get("products", "");
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject product = jsonArray.get(i).getAsJsonObject();
            String category = product.get("category").getAsString();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        return categoryCounts;
    }

    // ==================== LOGISTICS OVERVIEW ====================

    /**
     * Get orders in delivery
     */
    public int getOrdersInDelivery() throws Exception {
        String filters = "status=in.(PENDING,PREPARING,IN_TRANSIT)";
        String response = SupabaseClient.get("deliveries", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Get delivered orders
     */
    public int getDeliveredOrders() throws Exception {
        String filters = "status=in.(DELIVERED,RECEIVED)";
        String response = SupabaseClient.get("deliveries", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Get delayed deliveries (based on estimated_delivery_date)
     */
    public int getDelayedDeliveries() throws Exception {
        String filters = "status=in.(PENDING,PREPARING,IN_TRANSIT)";
        String response = SupabaseClient.get("deliveries", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        LocalDate today = LocalDate.now();
        int delayedCount = 0;
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject delivery = jsonArray.get(i).getAsJsonObject();
            if (delivery.has("estimated_delivery_date") && !delivery.get("estimated_delivery_date").isJsonNull()) {
                LocalDate estimatedDate = LocalDate.parse(delivery.get("estimated_delivery_date").getAsString());
                if (estimatedDate.isBefore(today)) {
                    delayedCount++;
                }
            }
        }
        return delayedCount;
    }

    /**
     * Get active deliveries count
     */
    public int getActiveDeliveries() throws Exception {
        String filters = "status=eq.IN_TRANSIT";
        String response = SupabaseClient.get("deliveries", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    // ==================== RECENT ACTIVITY ====================

    /**
     * Get latest orders
     */
    public List<Map<String, Object>> getLatestOrders(int limit) throws Exception {
        String response = SupabaseClient.get("orders", "order=created_at.desc&limit=" + limit);
        JsonArray ordersArray = SupabaseClient.parseJsonArray(response);
        
        List<Map<String, Object>> orders = new ArrayList<>();
        
        for (int i = 0; i < ordersArray.size(); i++) {
            JsonObject order = ordersArray.get(i).getAsJsonObject();
            Integer clientId = order.get("client_id").getAsInt();
            
            // Get client info
            String clientFilters = "client_id=eq." + SupabaseClient.enc(String.valueOf(clientId));
            String clientResponse = SupabaseClient.get("clients", clientFilters);
            JsonArray clientsArray = SupabaseClient.parseJsonArray(clientResponse);
            
            String companyName = "";
            if (clientsArray.size() > 0) {
                companyName = clientsArray.get(0).getAsJsonObject().get("company_name").getAsString();
            }
            
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", order.get("order_id").getAsString());
            orderData.put("clientId", clientId);
            orderData.put("companyName", companyName);
            orderData.put("totalAmount", order.get("total_amount").getAsDouble());
            orderData.put("status", order.get("status").getAsString());
            orderData.put("createdAt", order.get("created_at").getAsString());
            orders.add(orderData);
        }
        
        return orders;
    }

    /**
     * Get latest payments
     */
    public List<Map<String, Object>> getLatestPayments(int limit) throws Exception {
        String response = SupabaseClient.get("payments", "order=created_at.desc&limit=" + limit);
        JsonArray paymentsArray = SupabaseClient.parseJsonArray(response);
        
        List<Map<String, Object>> payments = new ArrayList<>();
        
        for (int i = 0; i < paymentsArray.size(); i++) {
            JsonObject payment = paymentsArray.get(i).getAsJsonObject();
            Integer clientId = payment.get("client_id").getAsInt();
            
            // Get client info
            String clientFilters = "client_id=eq." + SupabaseClient.enc(String.valueOf(clientId));
            String clientResponse = SupabaseClient.get("clients", clientFilters);
            JsonArray clientsArray = SupabaseClient.parseJsonArray(clientResponse);
            
            String companyName = "";
            if (clientsArray.size() > 0) {
                companyName = clientsArray.get(0).getAsJsonObject().get("company_name").getAsString();
            }
            
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("paymentId", payment.get("payment_id").getAsString());
            paymentData.put("orderId", payment.get("order_id").getAsString());
            paymentData.put("clientId", clientId);
            paymentData.put("companyName", companyName);
            paymentData.put("amount", payment.get("amount").getAsDouble());
            paymentData.put("paidAmount", payment.get("paid_amount").getAsDouble());
            paymentData.put("status", payment.get("status").getAsString());
            paymentData.put("createdAt", payment.get("created_at").getAsString());
            payments.add(paymentData);
        }
        
        return payments;
    }

    /**
     * Get latest notifications
     */
    public List<Map<String, Object>> getLatestNotifications(int limit) throws Exception {
        String response = SupabaseClient.get("notifications", "order=created_at.desc&limit=" + limit);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject notification = jsonArray.get(i).getAsJsonObject();
            
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", notification.get("notification_id").getAsString());
            notificationData.put("userId", notification.get("user_id").getAsInt());
            notificationData.put("title", notification.get("title").getAsString());
            notificationData.put("type", notification.get("type").getAsString());
            notificationData.put("isRead", notification.get("is_read").getAsBoolean());
            notificationData.put("createdAt", notification.get("created_at").getAsString());
            notifications.add(notificationData);
        }
        
        return notifications;
    }
}
