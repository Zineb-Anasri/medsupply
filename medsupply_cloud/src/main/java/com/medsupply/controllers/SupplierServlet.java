package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.medsupply.dao.SupplierDAO;
import com.medsupply.models.Supplier;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * SupplierServlet - read-only access to supplier profiles (name resolution & admin listing).
 *
 * Endpoints:
 * - GET /api/suppliers            - List all suppliers (ADMIN only)
 * - GET /api/suppliers/{userId}   - Get one supplier by its user id (ADMIN, or the SUPPLIER itself)
 *
 * All endpoints require authentication (AuthFilter). Bids store the supplier's USER id, so
 * supplier-name resolution is keyed by user id.
 */
@WebServlet("/api/suppliers/*")
public class SupplierServlet extends HttpServlet {

    private SupplierDAO supplierDAO;
    private Gson gson;

    @Override
    public void init() {
        this.supplierDAO = new SupplierDAO();
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
                // GET /api/suppliers/{userId}
                String targetUserId = pathInfo.substring(1);
                if (!"ADMIN".equals(role) && !targetUserId.equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                Supplier supplier = supplierDAO.findByUserId(targetUserId);
                if (supplier == null) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Supplier not found");
                    resp.setStatus(404);
                } else {
                    response.addProperty("success", true);
                    response.add("supplier", gson.toJsonTree(supplier));
                    resp.setStatus(200);
                }
            } else {
                // GET /api/suppliers - ADMIN listing
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can list suppliers");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                List<Supplier> suppliers = supplierDAO.findAll();
                response.addProperty("success", true);
                response.addProperty("count", suppliers.size());
                response.add("suppliers", gson.toJsonTree(suppliers));
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
