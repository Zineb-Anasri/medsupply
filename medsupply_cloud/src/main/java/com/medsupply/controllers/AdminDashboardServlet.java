package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medsupply.services.AdminDashboardService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * AdminDashboardServlet - Handles admin dashboard analytics operations
 * 
 * Endpoints:
 * - GET /api/admin/dashboard/overview - Complete dashboard overview
 * - GET /api/admin/dashboard/sales - Sales overview metrics
 * - GET /api/admin/dashboard/financial - Financial overview metrics
 * - GET /api/admin/dashboard/clients - Client analytics
 * - GET /api/admin/dashboard/products - Product analytics
 * - GET /api/admin/dashboard/logistics - Logistics overview
 * - GET /api/admin/dashboard/activity - Recent activity
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - Only ADMIN role can access this module
 * - Reject all non-admin users
 */
@WebServlet("/api/admin/dashboard/*")
public class AdminDashboardServlet extends HttpServlet {

    private AdminDashboardService dashboardService;
    private Gson gson;

    @Override
    public void init() {
        this.dashboardService = new AdminDashboardService();
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

            // Only ADMIN can access dashboard
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can access the dashboard");
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path. Use /overview, /sales, /financial, /clients, /products, /logistics, or /activity");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            Map<String, Object> data;

            switch (pathInfo) {
                case "/overview":
                    data = dashboardService.getDashboardOverview();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/sales":
                    data = dashboardService.getSalesOverview();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/financial":
                    data = dashboardService.getFinancialOverview();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/clients":
                    data = dashboardService.getClientAnalytics();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/products":
                    data = dashboardService.getProductAnalytics();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/logistics":
                    data = dashboardService.getLogisticsOverview();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                case "/activity":
                    data = dashboardService.getRecentActivity();
                    response.addProperty("success", true);
                    response.add("data", gson.toJsonTree(data));
                    resp.setStatus(200);
                    break;
                default:
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid request path. Use /overview, /sales, /financial, /clients, /products, /logistics, or /activity");
                    resp.setStatus(400);
                    break;
            }

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
