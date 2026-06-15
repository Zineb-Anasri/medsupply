package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.medsupply.dao.ClientDAO;
import com.medsupply.models.Client;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * ClientServlet - read-only access to client profiles (for name resolution & admin listing).
 *
 * Endpoints:
 * - GET /api/clients          - List all clients (ADMIN only)
 * - GET /api/clients/{userId} - Get one client by its user id (ADMIN, or the CLIENT itself)
 *
 * All endpoints require authentication (AuthFilter). Entities elsewhere store the owning
 * client's USER id in their client_id field, so resolution is keyed by user id.
 */
@WebServlet("/api/clients/*")
public class ClientServlet extends HttpServlet {

    private ClientDAO clientDAO;
    private Gson gson;

    @Override
    public void init() {
        this.clientDAO = new ClientDAO();
        this.gson = JsonUtil.GSON;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();
        try {
            HttpSession session = req.getSession(false);
            String role = session == null ? null : (String) session.getAttribute("role");
            String userId = session == null ? null : (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                // GET /api/clients/{userId}
                String targetUserId = pathInfo.substring(1);
                if (!"ADMIN".equals(role) && !targetUserId.equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                Client client = clientDAO.findByUserId(targetUserId);
                if (client == null) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Client not found");
                    resp.setStatus(404);
                } else {
                    response.addProperty("success", true);
                    response.add("client", gson.toJsonTree(client));
                    resp.setStatus(200);
                }
            } else {
                // GET /api/clients - ADMIN listing
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can list clients");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                List<Client> clients = clientDAO.findAll();
                response.addProperty("success", true);
                response.addProperty("count", clients.size());
                response.add("clients", gson.toJsonTree(clients));
                resp.setStatus(200);
            }
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
