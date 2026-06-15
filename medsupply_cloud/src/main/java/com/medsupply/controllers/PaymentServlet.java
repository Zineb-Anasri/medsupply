package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Payment;
import com.medsupply.services.PaymentService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
/**
 * PaymentServlet - Handles payment management operations
 * 
 * Endpoints:
 * - POST /api/payments/create/{orderId} - Create payment for order (ADMIN only)
 * - POST /api/payments/{id}/process - Process payment (CLIENT only)
 * - GET /api/payments - Get payments by role (CLIENT: own, ADMIN: all)
 * - GET /api/payments/{id} - Get payment details
 * - GET /api/payments/overdue - Get overdue payments (ADMIN only)
 * - PUT /api/payments/{id}/cancel - Cancel payment (ADMIN only)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT: can view own payments, process payments
 * - ADMIN: can create payments, view all payments, cancel payments, view overdue
 * - SUPPLIER: no access
 */
@WebServlet("/api/payments/*")
public class PaymentServlet extends HttpServlet {

    private PaymentService paymentService;
    private Gson gson;

    @Override
    public void init() {
        this.paymentService = new PaymentService();
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
            String userId = (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();

            // GET /api/payments/overdue - Get overdue payments (ADMIN only)
            if (pathInfo != null && pathInfo.equals("/overdue")) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can view overdue payments");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                List<Payment> payments = paymentService.getOverduePayments();

                response.addProperty("success", true);
                response.addProperty("count", payments.size());
                response.add("payments", gson.toJsonTree(payments));
                resp.setStatus(200);
            }
            // GET /api/payments/{id} - Get payment details
            else if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String paymentId = pathInfo.substring(1);
                
                Payment payment = paymentService.getPayment(paymentId);

                // SUPPLIER has no access; CLIENT can only view their own payments.
                if ("SUPPLIER".equals(role)
                        || ("CLIENT".equals(role) && !payment.getClientId().equals(userId))) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own payments");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.add("payment", gson.toJsonTree(payment));
                resp.setStatus(200);
            }
            // GET /api/payments - Get payments by role
            else {
                List<Payment> payments;

                if ("CLIENT".equals(role)) {
                    // CLIENT: get their own payments
                    payments = paymentService.getPaymentsByClient(userId);
                } else if ("ADMIN".equals(role)) {
                    // ADMIN: get all payments
                    String status = req.getParameter("status");
                    if (status != null && !status.isEmpty()) {
                        payments = paymentService.getPaymentsByStatus(status);
                    } else {
                        payments = paymentService.getAllPayments();
                    }
                } else {
                    // SUPPLIER: no access
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Suppliers cannot access payments");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.addProperty("count", payments.size());
                response.add("payments", gson.toJsonTree(payments));
                resp.setStatus(200);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid payment ID format");
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
            String userId = (String) session.getAttribute("userId");

            // Extract payment ID and action from path
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

            // POST /api/payments/create/{orderId} - Create payment for order (ADMIN only)
            if ("create".equals(pathParts[1])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can create payments");
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
                Payment payment = paymentService.createPaymentForOrder(orderId);

                response.addProperty("success", true);
                response.addProperty("message", "Payment created successfully");
                response.add("payment", gson.toJsonTree(payment));
                resp.setStatus(201);
            }
            // POST /api/payments/{id}/process - Process payment (CLIENT only)
            else if (pathParts.length >= 3 && "process".equals(pathParts[2])) {
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can process payments");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String paymentId = pathParts[1];

                // Ownership: a client may only process its own payment.
                Payment ownPayment = paymentService.getPayment(paymentId);
                if (!userId.equals(ownPayment.getClientId())) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only process your own payments");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                // Read request body
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                BigDecimal amount = requestJson.get("amount").getAsBigDecimal();
                String paymentMethod = requestJson.get("paymentMethod").getAsString();
                String transactionReference = requestJson.has("transactionReference") ? 
                    requestJson.get("transactionReference").getAsString() : null;

                Payment payment = paymentService.processPayment(paymentId, amount, paymentMethod, transactionReference);

                response.addProperty("success", true);
                response.addProperty("message", "Payment processed successfully");
                response.add("payment", gson.toJsonTree(payment));
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action. Use /api/payments/create/{orderId} or /api/payments/{id}/process");
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
                response.addProperty("message", "Unauthorized - Only admins can cancel payments");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Extract payment ID and action from path
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
                response.addProperty("message", "Invalid request path. Use /api/payments/{id}/cancel");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String paymentId = pathParts[1];
            String action = pathParts[2];

            // Handle cancel action
            if ("cancel".equals(action)) {
                boolean cancelled = paymentService.cancelPayment(paymentId);

                if (cancelled) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Payment cancelled successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to cancel payment");
                    resp.setStatus(400);
                }

            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action. Use /api/payments/{id}/cancel");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid payment ID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
