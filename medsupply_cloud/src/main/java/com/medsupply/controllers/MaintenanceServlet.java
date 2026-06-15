package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Intervention;
import com.medsupply.models.MaintenanceContract;
import com.medsupply.models.MaintenanceRequest;
import com.medsupply.services.MaintenanceService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
/**
 * MaintenanceServlet - Handles maintenance management operations
 * 
 * Endpoints:
 * 
 * Maintenance Contracts:
 * - POST /api/maintenance/contracts - Create contract (ADMIN only)
 * - GET /api/maintenance/contracts - Get contracts by role (CLIENT: own, ADMIN: all)
 * - GET /api/maintenance/contracts/active - Get active contracts (ADMIN only)
 * - GET /api/maintenance/contracts/{id} - Get contract details
 * - PUT /api/maintenance/contracts/{id}/status - Update contract status (ADMIN only)
 * 
 * Maintenance Requests:
 * - POST /api/maintenance/requests - Create request (CLIENT only)
 * - GET /api/maintenance/requests - Get requests by role (CLIENT: own, ADMIN: all)
 * - GET /api/maintenance/requests/{id} - Get request details
 * - GET /api/maintenance/requests/status/{status} - Get requests by status (ADMIN only)
 * - GET /api/maintenance/requests/priority/{priority} - Get requests by priority (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/status - Update request status (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/priority - Update request priority (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/close - Close request (ADMIN only)
 * 
 * Interventions:
 * - POST /api/maintenance/interventions - Create intervention (ADMIN only)
 * - GET /api/maintenance/interventions - Get all interventions (ADMIN only)
 * - GET /api/maintenance/interventions/{id} - Get intervention details
 * - GET /api/maintenance/interventions/request/{requestId} - Get interventions by request
 * - PUT /api/maintenance/interventions/{id}/details - Update intervention details (ADMIN only)
 * - PUT /api/maintenance/interventions/{id}/complete - Complete intervention (ADMIN only)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT: view own contracts/requests, create requests, track interventions
 * - ADMIN: create/manage contracts, view all requests, assign interventions, close requests
 * - SUPPLIER: no access
 */
@WebServlet("/api/maintenance/*")
public class MaintenanceServlet extends HttpServlet {

    private MaintenanceService maintenanceService;
    private Gson gson;

    @Override
    public void init() {
        this.maintenanceService = new MaintenanceService();
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

            // Handle contracts endpoints
            if (pathInfo != null && pathInfo.startsWith("/contracts")) {
                handleContractsGet(req, resp, response, role, userId, pathInfo);
            }
            // Handle requests endpoints
            else if (pathInfo != null && pathInfo.startsWith("/requests")) {
                handleRequestsGet(req, resp, response, role, userId, pathInfo);
            }
            // Handle interventions endpoints
            else if (pathInfo != null && pathInfo.startsWith("/interventions")) {
                handleInterventionsGet(req, resp, response, role, pathInfo);
            }
            else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid ID format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
        }
    }

    private void handleContractsGet(HttpServletRequest req, HttpServletResponse resp, 
                                    JsonObject response, String role, String userId, 
                                    String pathInfo) throws IOException, Exception {
        // GET /api/maintenance/contracts/active - Get active contracts (ADMIN only)
        if (pathInfo.equals("/contracts/active")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can view active contracts");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            List<MaintenanceContract> contracts = maintenanceService.getActiveContracts();
            response.addProperty("success", true);
            response.addProperty("count", contracts.size());
            response.add("contracts", gson.toJsonTree(contracts));
            resp.setStatus(200);
        }
        // GET /api/maintenance/contracts/{id} - Get contract details
        else if (pathInfo.matches("/contracts/[^/]+")) {
            String contractId = pathInfo.substring("/contracts/".length());
            MaintenanceContract contract = maintenanceService.getContract(contractId);

            // CLIENT can only view their own contracts
            if ("CLIENT".equals(role) && !contract.getClientId().equals(userId)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - You can only view your own contracts");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            response.addProperty("success", true);
            response.add("contract", gson.toJsonTree(contract));
            resp.setStatus(200);
        }
        // GET /api/maintenance/contracts - Get contracts by role
        else if (pathInfo.equals("/contracts")) {
            List<MaintenanceContract> contracts;

            if ("CLIENT".equals(role)) {
                contracts = maintenanceService.getContractsByClient(userId);
            } else if ("ADMIN".equals(role)) {
                contracts = maintenanceService.getAllContracts();
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Suppliers cannot access maintenance");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            response.addProperty("success", true);
            response.addProperty("count", contracts.size());
            response.add("contracts", gson.toJsonTree(contracts));
            resp.setStatus(200);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid request path");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        }
    }

    private void handleRequestsGet(HttpServletRequest req, HttpServletResponse resp, 
                                   JsonObject response, String role, String userId, 
                                   String pathInfo) throws IOException, Exception {
        // GET /api/maintenance/requests/status/{status} - Get requests by status (ADMIN only)
        if (pathInfo.matches("/requests/status/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can filter by status");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String status = pathInfo.substring("/requests/status/".length());
            List<MaintenanceRequest> requests = maintenanceService.getRequestsByStatus(status);
            response.addProperty("success", true);
            response.addProperty("count", requests.size());
            response.add("requests", gson.toJsonTree(requests));
            resp.setStatus(200);
        }
        // GET /api/maintenance/requests/priority/{priority} - Get requests by priority (ADMIN only)
        else if (pathInfo.matches("/requests/priority/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can filter by priority");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String priority = pathInfo.substring("/requests/priority/".length());
            List<MaintenanceRequest> requests = maintenanceService.getRequestsByPriority(priority);
            response.addProperty("success", true);
            response.addProperty("count", requests.size());
            response.add("requests", gson.toJsonTree(requests));
            resp.setStatus(200);
        }
        // GET /api/maintenance/requests/{id} - Get request details
        else if (pathInfo.matches("/requests/[^/]+")) {
            String requestId = pathInfo.substring("/requests/".length());
            MaintenanceRequest request = maintenanceService.getRequest(requestId);

            // CLIENT can only view their own requests
            if ("CLIENT".equals(role) && !request.getClientId().equals(userId)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - You can only view your own requests");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            response.addProperty("success", true);
            response.add("request", gson.toJsonTree(request));
            resp.setStatus(200);
        }
        // GET /api/maintenance/requests - Get requests by role
        else if (pathInfo.equals("/requests")) {
            List<MaintenanceRequest> requests;

            if ("CLIENT".equals(role)) {
                requests = maintenanceService.getRequestsByClient(userId);
            } else if ("ADMIN".equals(role)) {
                requests = maintenanceService.getAllRequests();
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Suppliers cannot access maintenance");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            response.addProperty("success", true);
            response.addProperty("count", requests.size());
            response.add("requests", gson.toJsonTree(requests));
            resp.setStatus(200);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid request path");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        }
    }

    private void handleInterventionsGet(HttpServletRequest req, HttpServletResponse resp, 
                                       JsonObject response, String role, String pathInfo) throws IOException, Exception {
        // GET /api/maintenance/interventions/request/{requestId} - Get interventions by request
        if (pathInfo.matches("/interventions/request/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can view interventions");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String requestId = pathInfo.substring("/interventions/request/".length());
            List<Intervention> interventions = maintenanceService.getInterventionsByRequest(requestId);
            response.addProperty("success", true);
            response.addProperty("count", interventions.size());
            response.add("interventions", gson.toJsonTree(interventions));
            resp.setStatus(200);
        }
        // GET /api/maintenance/interventions/{id} - Get intervention details
        else if (pathInfo.matches("/interventions/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can view interventions");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String interventionId = pathInfo.substring("/interventions/".length());
            Intervention intervention = maintenanceService.getIntervention(interventionId);
            response.addProperty("success", true);
            response.add("intervention", gson.toJsonTree(intervention));
            resp.setStatus(200);
        }
        // GET /api/maintenance/interventions - Get all interventions (ADMIN only)
        else if (pathInfo.equals("/interventions")) {
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can view interventions");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            List<Intervention> interventions = maintenanceService.getAllInterventions();
            response.addProperty("success", true);
            response.addProperty("count", interventions.size());
            response.add("interventions", gson.toJsonTree(interventions));
            resp.setStatus(200);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid request path");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        }
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

            String pathInfo = req.getPathInfo();

            // POST /api/maintenance/contracts - Create contract (ADMIN only)
            if (pathInfo != null && pathInfo.equals("/contracts")) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can create contracts");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String clientId = requestJson.get("clientId").getAsString();
                String productId = requestJson.get("productId").getAsString();
                Integer warrantyDuration = requestJson.get("warrantyDuration").getAsInt();
                LocalDate startDate = LocalDate.parse(requestJson.get("startDate").getAsString());

                MaintenanceContract contract = maintenanceService.createContract(clientId, productId, 
                    warrantyDuration, startDate);

                response.addProperty("success", true);
                response.addProperty("message", "Contract created successfully");
                response.add("contract", gson.toJsonTree(contract));
                resp.setStatus(201);
            }
            // POST /api/maintenance/requests - Create request (CLIENT only)
            else if (pathInfo != null && pathInfo.equals("/requests")) {
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can create requests");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String contractId = requestJson.get("contractId").getAsString();
                String productId = requestJson.get("productId").getAsString();
                String description = requestJson.get("description").getAsString();
                String priority = requestJson.get("priority").getAsString();

                MaintenanceRequest request = maintenanceService.createRequest(contractId, userId, 
                    productId, description, priority);

                response.addProperty("success", true);
                response.addProperty("message", "Request created successfully");
                response.add("request", gson.toJsonTree(request));
                resp.setStatus(201);
            }
            // POST /api/maintenance/interventions - Create intervention (ADMIN only)
            else if (pathInfo != null && pathInfo.equals("/interventions")) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can create interventions");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String requestId = requestJson.get("requestId").getAsString();
                String technicianName = requestJson.get("technicianName").getAsString();
                String diagnosis = requestJson.get("diagnosis").getAsString();

                Intervention intervention = maintenanceService.createIntervention(requestId, 
                    technicianName, diagnosis);

                response.addProperty("success", true);
                response.addProperty("message", "Intervention created successfully");
                response.add("intervention", gson.toJsonTree(intervention));
                resp.setStatus(201);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid ID format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");

            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 4) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Handle contracts updates
            if ("contracts".equals(pathParts[2])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can update contracts");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String contractId = pathParts[3];
                String action = pathParts[4];

                if ("status".equals(action)) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = req.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                    String status = requestJson.get("status").getAsString();

                    boolean updated = maintenanceService.updateContractStatus(contractId, status);

                    if (updated) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Contract status updated successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to update contract status");
                        resp.setStatus(400);
                    }
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid action");
                    resp.setStatus(400);
                }
            }
            // Handle requests updates
            else if ("requests".equals(pathParts[2])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can update requests");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String requestId = pathParts[3];
                String action = pathParts[4];

                if ("status".equals(action)) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = req.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                    String status = requestJson.get("status").getAsString();

                    boolean updated = maintenanceService.updateRequestStatus(requestId, status);

                    if (updated) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Request status updated successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to update request status");
                        resp.setStatus(400);
                    }
                } else if ("priority".equals(action)) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = req.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                    String priority = requestJson.get("priority").getAsString();

                    boolean updated = maintenanceService.updateRequestPriority(requestId, priority);

                    if (updated) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Request priority updated successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to update request priority");
                        resp.setStatus(400);
                    }
                } else if ("close".equals(action)) {
                    boolean closed = maintenanceService.closeRequest(requestId);

                    if (closed) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Request closed successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to close request");
                        resp.setStatus(400);
                    }
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid action");
                    resp.setStatus(400);
                }
            }
            // Handle interventions updates
            else if ("interventions".equals(pathParts[2])) {
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can update interventions");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String interventionId = pathParts[3];
                String action = pathParts[4];

                if ("details".equals(action)) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = req.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                    String actionsPerformed = requestJson.has("actionsPerformed") ? 
                        requestJson.get("actionsPerformed").getAsString() : null;
                    String replacedParts = requestJson.has("replacedParts") ? 
                        requestJson.get("replacedParts").getAsString() : null;
                    BigDecimal interventionCost = requestJson.has("interventionCost") ? 
                        requestJson.get("interventionCost").getAsBigDecimal() : null;
                    String notes = requestJson.has("notes") ? 
                        requestJson.get("notes").getAsString() : null;

                    boolean updated = maintenanceService.updateInterventionDetails(interventionId, 
                        actionsPerformed, replacedParts, interventionCost, notes);

                    if (updated) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Intervention details updated successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to update intervention details");
                        resp.setStatus(400);
                    }
                } else if ("complete".equals(action)) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = req.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                    String actionsPerformed = requestJson.has("actionsPerformed") ? 
                        requestJson.get("actionsPerformed").getAsString() : null;
                    String replacedParts = requestJson.has("replacedParts") ? 
                        requestJson.get("replacedParts").getAsString() : null;
                    BigDecimal interventionCost = requestJson.has("interventionCost") ? 
                        requestJson.get("interventionCost").getAsBigDecimal() : null;
                    String notes = requestJson.has("notes") ? 
                        requestJson.get("notes").getAsString() : null;

                    boolean completed = maintenanceService.completeIntervention(interventionId, 
                        actionsPerformed, replacedParts, interventionCost, notes);

                    if (completed) {
                        response.addProperty("success", true);
                        response.addProperty("message", "Intervention completed successfully");
                        resp.setStatus(200);
                    } else {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to complete intervention");
                        resp.setStatus(400);
                    }
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid action");
                    resp.setStatus(400);
                }
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid ID format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
        }
    }
}
