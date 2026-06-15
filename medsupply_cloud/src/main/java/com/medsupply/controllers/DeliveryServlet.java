package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Delivery;
import com.medsupply.services.DeliveryService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
/**
 * DeliveryServlet - Handles delivery management operations
 * 
 * Endpoints:
 * - POST /api/deliveries/create/{orderId} - Create delivery for order (ADMIN only)
 * - POST /api/deliveries/{id}/status - Update delivery status (ADMIN only)
 * - POST /api/deliveries/{id}/gps - Update GPS location (prepared for Supabase Realtime)
 * - POST /api/deliveries/{id}/confirm - Confirm delivery receipt (CLIENT only)
 * - GET /api/deliveries - Get deliveries by role (CLIENT: own, ADMIN: all)
 * - GET /api/deliveries/{id} - Get delivery details
 * - GET /api/deliveries/active - Get active deliveries (ADMIN only)
 * - PUT /api/deliveries/{id}/details - Update delivery details (ADMIN only)
 * - PUT /api/deliveries/{id}/delivered - Mark delivery as delivered (ADMIN only)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT: can view own deliveries, confirm receipt
 * - ADMIN: can create deliveries, update status, manage all deliveries, GPS tracking
 * - SUPPLIER: no access
 */
@WebServlet("/api/deliveries/*")
public class DeliveryServlet extends HttpServlet {

    private DeliveryService deliveryService;
    private Gson gson;

    @Override
    public void init() {
        this.deliveryService = new DeliveryService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");
            String userId = (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();

            // GET /api/deliveries/active - Get active deliveries (ADMIN only)
            if (pathInfo != null && pathInfo.equals("/active")) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can view active deliveries");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                List<Delivery> deliveries = deliveryService.getActiveDeliveries();

                response.addProperty("success", true);
                response.addProperty("count", deliveries.size());
                response.add("deliveries", gson.toJsonTree(deliveries));
                resp.setStatus(200);
            }
            // GET /api/deliveries/{id} - Get delivery details
            else if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String deliveryId = pathInfo.substring(1);
                
                Delivery delivery = deliveryService.getDelivery(deliveryId);
                
                // CLIENT can only view their own deliveries
                if ("CLIENT".equals(role) && !delivery.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own deliveries");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.add("delivery", gson.toJsonTree(delivery));
                resp.setStatus(200);
            }
            // GET /api/deliveries - Get deliveries by role
            else {
                List<Delivery> deliveries;

                if ("CLIENT".equals(role)) {
                    // CLIENT: get their own deliveries
                    deliveries = deliveryService.getDeliveriesByClient(userId);
                } else if ("ADMIN".equals(role)) {
                    // ADMIN: get all deliveries
                    String status = req.getParameter("status");
                    if (status != null && !status.isEmpty()) {
                        deliveries = deliveryService.getDeliveriesByStatus(status);
                    } else {
                        deliveries = deliveryService.getAllDeliveries();
                    }
                } else {
                    // SUPPLIER: no access
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Suppliers cannot access deliveries");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.addProperty("count", deliveries.size());
                response.add("deliveries", gson.toJsonTree(deliveries));
                resp.setStatus(200);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid delivery ID format");
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

            // Extract delivery ID and action from path
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // POST /api/deliveries/create/{orderId} - Create delivery for order (ADMIN only)
            if ("create".equals(pathParts[1])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can create deliveries");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                if (pathParts.length < 3) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Order ID required");
                    resp.setStatus(400);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String orderId = pathParts[2];

                // Read request body for delivery details
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String deliveryAddress = requestJson.has("deliveryAddress") ? 
                    requestJson.get("deliveryAddress").getAsString() : null;
                String contactPhone = requestJson.has("contactPhone") ? 
                    requestJson.get("contactPhone").getAsString() : null;
                String contactPerson = requestJson.has("contactPerson") ? 
                    requestJson.get("contactPerson").getAsString() : null;

                Delivery delivery = deliveryService.createDeliveryForOrder(orderId, deliveryAddress, 
                    contactPhone, contactPerson);

                response.addProperty("success", true);
                response.addProperty("message", "Delivery created successfully");
                response.add("delivery", gson.toJsonTree(delivery));
                resp.setStatus(201);
            }
            // POST /api/deliveries/{id}/status - Update delivery status (ADMIN only)
            else if (pathParts.length >= 3 && "status".equals(pathParts[2])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can update delivery status");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String deliveryId = pathParts[1];

                // Read request body for new status
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String status = requestJson.get("status").getAsString();

                boolean updated = deliveryService.updateDeliveryStatus(deliveryId, status);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Delivery status updated successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to update delivery status");
                    resp.setStatus(400);
                }
            }
            // POST /api/deliveries/{id}/gps - Update GPS location (prepared for Supabase Realtime)
            else if (pathParts.length >= 3 && "gps".equals(pathParts[2])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can update GPS location");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String deliveryId = pathParts[1];

                // Read request body for GPS coordinates
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                Double latitude = requestJson.get("latitude").getAsDouble();
                Double longitude = requestJson.get("longitude").getAsDouble();

                boolean updated = deliveryService.updateGpsLocation(deliveryId, latitude, longitude);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "GPS location updated successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to update GPS location");
                    resp.setStatus(400);
                }
            }
            // POST /api/deliveries/{id}/confirm - Confirm delivery receipt (CLIENT only)
            else if (pathParts.length >= 3 && "confirm".equals(pathParts[2])) {
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can confirm delivery receipt");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String deliveryId = pathParts[1];

                boolean confirmed = deliveryService.confirmDeliveryReceipt(deliveryId);

                if (confirmed) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Delivery receipt confirmed successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to confirm delivery receipt");
                    resp.setStatus(400);
                }
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action. Use /api/deliveries/create/{orderId}, /api/deliveries/{id}/status, /api/deliveries/{id}/gps, or /api/deliveries/{id}/confirm");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid ID format");
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
                response.addProperty("message", "Unauthorized - Only admins can update delivery details");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract delivery ID and action from path
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
                response.addProperty("message", "Invalid request path. Use /api/deliveries/{id}/details or /api/deliveries/{id}/delivered");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String deliveryId = pathParts[1];
            String action = pathParts[2];

            // Handle details update
            if ("details".equals(action)) {
                // Read request body
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String deliveryAddress = requestJson.has("deliveryAddress") ? 
                    requestJson.get("deliveryAddress").getAsString() : null;
                String contactPhone = requestJson.has("contactPhone") ? 
                    requestJson.get("contactPhone").getAsString() : null;
                String contactPerson = requestJson.has("contactPerson") ? 
                    requestJson.get("contactPerson").getAsString() : null;
                LocalDateTime estimatedDeliveryDate = requestJson.has("estimatedDeliveryDate") ? 
                    LocalDateTime.parse(requestJson.get("estimatedDeliveryDate").getAsString()) : null;

                boolean updated = deliveryService.updateDeliveryDetails(deliveryId, deliveryAddress, 
                    contactPhone, contactPerson, estimatedDeliveryDate);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Delivery details updated successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to update delivery details");
                    resp.setStatus(400);
                }

            } else if ("delivered".equals(action)) {
                // Read request body for delivery notes
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String deliveryNotes = requestJson.has("deliveryNotes") ? 
                    requestJson.get("deliveryNotes").getAsString() : null;

                boolean delivered = deliveryService.markDeliveryAsDelivered(deliveryId, deliveryNotes);

                if (delivered) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Delivery marked as delivered successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to mark delivery as delivered");
                    resp.setStatus(400);
                }

            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action. Use /api/deliveries/{id}/details or /api/deliveries/{id}/delivered");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid delivery ID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
