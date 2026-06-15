package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Order;
import com.medsupply.models.OrderItem;
import com.medsupply.services.OrderService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
/**
 * OrderServlet - Handles order management operations
 * 
 * Endpoints:
 * - POST /api/orders/convert/{quoteId} - Convert approved quote to order (ADMIN only)
 * - GET /api/orders - Get orders by role (CLIENT: own, ADMIN: all)
 * - GET /api/orders/{id} - Get order details with items
 * - PUT /api/orders/{id}/status - Update order status (ADMIN only)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT: can view own orders
 * - ADMIN: can convert quotes, view all orders, update status
 * - SUPPLIER: no access
 */
@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private Gson gson;

    @Override
    public void init() {
        this.orderService = new OrderService();
        this.gson = JsonUtil.GSON;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");
            String userId = (String) session.getAttribute("clientId"); // client profile id (clients.id)

            String pathInfo = req.getPathInfo();

            // GET /api/orders/{id} - Get order details with items
            if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String orderId = pathInfo.substring(1);
                
                Order order = orderService.getOrderWithItems(orderId);

                // SUPPLIER has no access; CLIENT can only view their own orders.
                if ("SUPPLIER".equals(role)
                        || ("CLIENT".equals(role) && !order.getClientId().equals(userId))) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own orders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                List<OrderItem> items = orderService.getOrderItems(orderId);

                response.addProperty("success", true);
                response.add("order", gson.toJsonTree(order));
                response.add("items", gson.toJsonTree(items));
                resp.setStatus(200);
            }
            // GET /api/orders - Get orders by role
            else {
                List<Order> orders;

                if ("CLIENT".equals(role)) {
                    // CLIENT: get their own orders
                    orders = orderService.getOrdersByClient(userId);
                } else if ("ADMIN".equals(role)) {
                    // ADMIN: get all orders
                    String status = req.getParameter("status");
                    if (status != null && !status.isEmpty()) {
                        orders = orderService.getOrdersByStatus(status);
                    } else {
                        orders = orderService.getAllOrders();
                    }
                } else {
                    // SUPPLIER: no access
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Suppliers cannot access orders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.addProperty("count", orders.size());
                response.add("orders", gson.toJsonTree(orders));
                resp.setStatus(200);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid order ID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");

            // Check if user is ADMIN
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can convert quotes to orders");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract quote ID from path
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.startsWith("/convert/")) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path. Use /api/orders/convert/{quoteId}");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String quoteId = pathInfo.substring("/convert/".length());

            // Convert quote to order
            Order order = orderService.convertQuoteToOrder(quoteId);

            response.addProperty("success", true);
            response.addProperty("message", "Order created successfully from quote");
            response.add("order", gson.toJsonTree(order));
            resp.setStatus(201);

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid quote ID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");

            // Check if user is ADMIN
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can update order status");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract order ID and action from path
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 3) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path. Use /api/orders/{id}/status");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String orderId = pathParts[1];
            String action = pathParts[2];

            // Handle status update
            if ("status".equals(action)) {
                // Read request body for new status
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String newStatus = requestJson.get("status").getAsString();

                // Validate status
                if (!isValidStatus(newStatus)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid status. Valid statuses: PENDING, PROCESSING, SHIPPED, DELIVERED, COMPLETED");
                    resp.setStatus(400);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean updated = orderService.updateOrderStatus(orderId, newStatus);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Order status updated successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to update order status");
                    resp.setStatus(400);
                }

            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action. Use /api/orders/{id}/status");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid order ID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }

    /**
     * Validate status value
     */
    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) || "PROCESSING".equals(status) || 
               "SHIPPED".equals(status) || "DELIVERED".equals(status) || 
               "COMPLETED".equals(status);
    }
}
