package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
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
 * Maintenance Contracts:
 * - POST /api/maintenance/contracts - Create contract (ADMIN only)
 * - GET /api/maintenance/contracts - Get contracts by role (CLIENT: own, ADMIN: all)
 * - GET /api/maintenance/contracts/active - Get active contracts (ADMIN only)
 * - GET /api/maintenance/contracts/{id} - Get contract details (owner CLIENT or ADMIN)
 * - PUT /api/maintenance/contracts/{id}/status - Update contract status (ADMIN only)
 *
 * Maintenance Requests:
 * - POST /api/maintenance/requests - Create request (CLIENT only, own contract)
 * - GET /api/maintenance/requests - Get requests by role (CLIENT: own, ADMIN: all)
 * - GET /api/maintenance/requests/{id} - Get request details (owner CLIENT or ADMIN)
 * - GET /api/maintenance/requests/status/{status} - Get requests by status (ADMIN only)
 * - GET /api/maintenance/requests/priority/{priority} - Get requests by priority (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/status - Update request status (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/priority - Update request priority (ADMIN only)
 * - PUT /api/maintenance/requests/{id}/close - Close request (ADMIN only)
 *
 * Interventions (ADMIN only):
 * - POST /api/maintenance/interventions - Create intervention
 * - GET /api/maintenance/interventions - Get all interventions
 * - GET /api/maintenance/interventions/{id} - Get intervention details
 * - GET /api/maintenance/interventions/request/{requestId} - Get interventions by request
 * - PUT /api/maintenance/interventions/{id}/details - Update intervention details
 * - PUT /api/maintenance/interventions/{id}/complete - Complete intervention
 *
 * Security: all endpoints require authentication (AuthFilter). SUPPLIER has no access.
 */
@WebServlet("/api/maintenance/*")
public class MaintenanceServlet extends HttpServlet {

    private MaintenanceService maintenanceService;
    private Gson gson;

    @Override
    public void init() {
        this.maintenanceService = new MaintenanceService();
        this.gson = JsonUtil.GSON;
    }

    /** Single, consistent response writer (avoids duplicated/garbled bodies). */
    private void send(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().print(gson.toJson(body));
    }

    private JsonObject fail(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("success", false);
        o.addProperty("message", message);
        return o;
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = req.getSession(false);
            String role = session == null ? null : (String) session.getAttribute("role");
            String userId = session == null ? null : (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/contracts")) {
                handleContractsGet(req, resp, role, userId, pathInfo);
            } else if (pathInfo != null && pathInfo.startsWith("/requests")) {
                handleRequestsGet(req, resp, role, userId, pathInfo);
            } else if (pathInfo != null && pathInfo.startsWith("/interventions")) {
                handleInterventionsGet(req, resp, role, pathInfo);
            } else {
                send(resp, 400, fail("Invalid request path"));
            }
        } catch (IllegalArgumentException e) {
            send(resp, 400, fail("Invalid ID format"));
        } catch (Exception e) {
            send(resp, 500, fail("Server error: " + e.getMessage()));
        }
    }

    private void handleContractsGet(HttpServletRequest req, HttpServletResponse resp,
                                    String role, String userId, String pathInfo) throws Exception {
        if (pathInfo.equals("/contracts/active")) {
            if (!"ADMIN".equals(role)) {
                send(resp, 403, fail("Unauthorized - Only admins can view active contracts"));
                return;
            }
            List<MaintenanceContract> contracts = maintenanceService.getActiveContracts();
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", contracts.size());
            o.add("contracts", gson.toJsonTree(contracts));
            send(resp, 200, o);
        } else if (pathInfo.matches("/contracts/[^/]+")) {
            String contractId = pathInfo.substring("/contracts/".length());
            MaintenanceContract contract = maintenanceService.getContract(contractId);

            // SUPPLIER has no access; CLIENT may only view its own contract.
            if ("SUPPLIER".equals(role)
                    || ("CLIENT".equals(role) && !contract.getClientId().equals(userId))) {
                send(resp, 403, fail("Unauthorized - You can only view your own contracts"));
                return;
            }

            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.add("contract", gson.toJsonTree(contract));
            send(resp, 200, o);
        } else if (pathInfo.equals("/contracts")) {
            List<MaintenanceContract> contracts;
            if ("CLIENT".equals(role)) {
                contracts = maintenanceService.getContractsByClient(userId);
            } else if ("ADMIN".equals(role)) {
                contracts = maintenanceService.getAllContracts();
            } else {
                send(resp, 403, fail("Unauthorized - Suppliers cannot access maintenance"));
                return;
            }
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", contracts.size());
            o.add("contracts", gson.toJsonTree(contracts));
            send(resp, 200, o);
        } else {
            send(resp, 400, fail("Invalid request path"));
        }
    }

    private void handleRequestsGet(HttpServletRequest req, HttpServletResponse resp,
                                   String role, String userId, String pathInfo) throws Exception {
        if (pathInfo.matches("/requests/status/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                send(resp, 403, fail("Unauthorized - Only admins can filter by status"));
                return;
            }
            String status = pathInfo.substring("/requests/status/".length());
            List<MaintenanceRequest> requests = maintenanceService.getRequestsByStatus(status);
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", requests.size());
            o.add("requests", gson.toJsonTree(requests));
            send(resp, 200, o);
        } else if (pathInfo.matches("/requests/priority/[^/]+")) {
            if (!"ADMIN".equals(role)) {
                send(resp, 403, fail("Unauthorized - Only admins can filter by priority"));
                return;
            }
            String priority = pathInfo.substring("/requests/priority/".length());
            List<MaintenanceRequest> requests = maintenanceService.getRequestsByPriority(priority);
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", requests.size());
            o.add("requests", gson.toJsonTree(requests));
            send(resp, 200, o);
        } else if (pathInfo.matches("/requests/[^/]+")) {
            String requestId = pathInfo.substring("/requests/".length());
            MaintenanceRequest request = maintenanceService.getRequest(requestId);

            if ("SUPPLIER".equals(role)
                    || ("CLIENT".equals(role) && !request.getClientId().equals(userId))) {
                send(resp, 403, fail("Unauthorized - You can only view your own requests"));
                return;
            }

            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.add("request", gson.toJsonTree(request));
            send(resp, 200, o);
        } else if (pathInfo.equals("/requests")) {
            List<MaintenanceRequest> requests;
            if ("CLIENT".equals(role)) {
                requests = maintenanceService.getRequestsByClient(userId);
            } else if ("ADMIN".equals(role)) {
                requests = maintenanceService.getAllRequests();
            } else {
                send(resp, 403, fail("Unauthorized - Suppliers cannot access maintenance"));
                return;
            }
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", requests.size());
            o.add("requests", gson.toJsonTree(requests));
            send(resp, 200, o);
        } else {
            send(resp, 400, fail("Invalid request path"));
        }
    }

    private void handleInterventionsGet(HttpServletRequest req, HttpServletResponse resp,
                                        String role, String pathInfo) throws Exception {
        // All intervention reads are ADMIN-only.
        if (!"ADMIN".equals(role)) {
            send(resp, 403, fail("Unauthorized - Only admins can view interventions"));
            return;
        }

        if (pathInfo.matches("/interventions/request/[^/]+")) {
            String requestId = pathInfo.substring("/interventions/request/".length());
            List<Intervention> interventions = maintenanceService.getInterventionsByRequest(requestId);
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", interventions.size());
            o.add("interventions", gson.toJsonTree(interventions));
            send(resp, 200, o);
        } else if (pathInfo.matches("/interventions/[^/]+")) {
            String interventionId = pathInfo.substring("/interventions/".length());
            Intervention intervention = maintenanceService.getIntervention(interventionId);
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.add("intervention", gson.toJsonTree(intervention));
            send(resp, 200, o);
        } else if (pathInfo.equals("/interventions")) {
            List<Intervention> interventions = maintenanceService.getAllInterventions();
            JsonObject o = new JsonObject();
            o.addProperty("success", true);
            o.addProperty("count", interventions.size());
            o.add("interventions", gson.toJsonTree(interventions));
            send(resp, 200, o);
        } else {
            send(resp, 400, fail("Invalid request path"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = req.getSession(false);
            String role = session == null ? null : (String) session.getAttribute("role");
            String userId = session == null ? null : (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.equals("/contracts")) {
                if (!"ADMIN".equals(role)) {
                    send(resp, 403, fail("Unauthorized - Only admins can create contracts"));
                    return;
                }
                JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                String clientId = requestJson.get("clientId").getAsString();
                String productId = requestJson.get("productId").getAsString();
                Integer warrantyDuration = requestJson.get("warrantyDuration").getAsInt();
                LocalDate startDate = LocalDate.parse(requestJson.get("startDate").getAsString());

                MaintenanceContract contract = maintenanceService.createContract(clientId, productId,
                    warrantyDuration, startDate);

                JsonObject o = new JsonObject();
                o.addProperty("success", true);
                o.addProperty("message", "Contract created successfully");
                o.add("contract", gson.toJsonTree(contract));
                send(resp, 201, o);
            } else if (pathInfo != null && pathInfo.equals("/requests")) {
                if (!"CLIENT".equals(role)) {
                    send(resp, 403, fail("Unauthorized - Only clients can create requests"));
                    return;
                }
                JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                String contractId = requestJson.get("contractId").getAsString();
                String productId = requestJson.get("productId").getAsString();
                String description = requestJson.get("description").getAsString();
                String priority = requestJson.get("priority").getAsString();

                // Ownership: a client may only open requests against its own contract.
                MaintenanceContract contract = maintenanceService.getContract(contractId);
                if (contract == null || !userId.equals(contract.getClientId())) {
                    send(resp, 403, fail("Unauthorized - You can only create requests for your own contracts"));
                    return;
                }

                MaintenanceRequest request = maintenanceService.createRequest(contractId, userId,
                    productId, description, priority);

                JsonObject o = new JsonObject();
                o.addProperty("success", true);
                o.addProperty("message", "Request created successfully");
                o.add("request", gson.toJsonTree(request));
                send(resp, 201, o);
            } else if (pathInfo != null && pathInfo.equals("/interventions")) {
                if (!"ADMIN".equals(role)) {
                    send(resp, 403, fail("Unauthorized - Only admins can create interventions"));
                    return;
                }
                JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                String requestId = requestJson.get("requestId").getAsString();
                String technicianName = requestJson.get("technicianName").getAsString();
                String diagnosis = requestJson.get("diagnosis").getAsString();

                Intervention intervention = maintenanceService.createIntervention(requestId,
                    technicianName, diagnosis);

                JsonObject o = new JsonObject();
                o.addProperty("success", true);
                o.addProperty("message", "Intervention created successfully");
                o.add("intervention", gson.toJsonTree(intervention));
                send(resp, 201, o);
            } else {
                send(resp, 400, fail("Invalid request path"));
            }
        } catch (IllegalArgumentException e) {
            send(resp, 400, fail("Invalid ID format"));
        } catch (Exception e) {
            send(resp, 500, fail("Server error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = req.getSession(false);
            String role = session == null ? null : (String) session.getAttribute("role");

            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                send(resp, 400, fail("Invalid request path"));
                return;
            }

            // pathInfo is "/{resource}/{id}/{action}" -> split = ["", resource, id, action]
            String[] parts = pathInfo.split("/");
            if (parts.length < 4) {
                send(resp, 400, fail("Invalid request path"));
                return;
            }

            String resource = parts[1];
            String id = parts[2];
            String action = parts[3];

            // All maintenance mutations are ADMIN-only.
            if (!"ADMIN".equals(role)) {
                send(resp, 403, fail("Unauthorized - Only admins can update maintenance records"));
                return;
            }

            if ("contracts".equals(resource)) {
                if ("status".equals(action)) {
                    JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                    String status = requestJson.get("status").getAsString();
                    boolean updated = maintenanceService.updateContractStatus(id, status);
                    send(resp, updated ? 200 : 400,
                        updated ? success("Contract status updated successfully")
                                : fail("Failed to update contract status"));
                } else {
                    send(resp, 400, fail("Invalid action"));
                }
            } else if ("requests".equals(resource)) {
                if ("status".equals(action)) {
                    JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                    String status = requestJson.get("status").getAsString();
                    boolean updated = maintenanceService.updateRequestStatus(id, status);
                    send(resp, updated ? 200 : 400,
                        updated ? success("Request status updated successfully")
                                : fail("Failed to update request status"));
                } else if ("priority".equals(action)) {
                    JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                    String priority = requestJson.get("priority").getAsString();
                    boolean updated = maintenanceService.updateRequestPriority(id, priority);
                    send(resp, updated ? 200 : 400,
                        updated ? success("Request priority updated successfully")
                                : fail("Failed to update request priority"));
                } else if ("close".equals(action)) {
                    boolean closed = maintenanceService.closeRequest(id);
                    send(resp, closed ? 200 : 400,
                        closed ? success("Request closed successfully")
                               : fail("Failed to close request"));
                } else {
                    send(resp, 400, fail("Invalid action"));
                }
            } else if ("interventions".equals(resource)) {
                if ("details".equals(action)) {
                    JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                    boolean updated = maintenanceService.updateInterventionDetails(id,
                        optString(requestJson, "actionsPerformed"),
                        optString(requestJson, "replacedParts"),
                        optBigDecimal(requestJson, "interventionCost"),
                        optString(requestJson, "notes"));
                    send(resp, updated ? 200 : 400,
                        updated ? success("Intervention details updated successfully")
                                : fail("Failed to update intervention details"));
                } else if ("complete".equals(action)) {
                    JsonObject requestJson = JsonParser.parseString(readBody(req)).getAsJsonObject();
                    boolean completed = maintenanceService.completeIntervention(id,
                        optString(requestJson, "actionsPerformed"),
                        optString(requestJson, "replacedParts"),
                        optBigDecimal(requestJson, "interventionCost"),
                        optString(requestJson, "notes"));
                    send(resp, completed ? 200 : 400,
                        completed ? success("Intervention completed successfully")
                                  : fail("Failed to complete intervention"));
                } else {
                    send(resp, 400, fail("Invalid action"));
                }
            } else {
                send(resp, 400, fail("Invalid request path"));
            }
        } catch (IllegalArgumentException e) {
            send(resp, 400, fail("Invalid ID format"));
        } catch (Exception e) {
            send(resp, 500, fail("Server error: " + e.getMessage()));
        }
    }

    private JsonObject success(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("success", true);
        o.addProperty("message", message);
        return o;
    }

    private static String optString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static BigDecimal optBigDecimal(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBigDecimal() : null;
    }
}
