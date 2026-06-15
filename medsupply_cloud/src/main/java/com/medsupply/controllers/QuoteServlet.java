package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.medsupply.models.Quote;
import com.medsupply.models.QuoteItem;
import com.medsupply.services.QuoteService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/**
 * QuoteServlet - Handles quote (demande de devis) operations
 * 
 * Endpoints:
 * - POST /api/quotes - Create quote request (CLIENT only)
 * - GET /api/quotes - Get quotes by role (CLIENT: their quotes, ADMIN: all)
 * - GET /api/quotes/{id} - Get quote details with items
 * - PUT /api/quotes/{id}/review - ADMIN sets pricing and updates status to REVIEWED
 * - PUT /api/quotes/{id}/accept - CLIENT accepts quote → status becomes APPROVED
 * - PUT /api/quotes/{id}/reject - CLIENT rejects quote
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT: can create and view their quotes, accept/reject their quotes
 * - ADMIN: can view all quotes, review and set pricing
 * - SUPPLIER: no access
 */
@WebServlet("/api/quotes/*")
public class QuoteServlet extends HttpServlet {

    private QuoteService quoteService;
    private Gson gson;

    @Override
    public void init() {
        this.quoteService = new QuoteService();
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

            // GET /api/quotes/{id} - Get quote details with items
            if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String quoteId = pathInfo.substring(1);
                
                Quote quote = quoteService.getQuoteWithItems(quoteId);

                // SUPPLIER has no access; CLIENT can only view their own quotes.
                if ("SUPPLIER".equals(role)
                        || ("CLIENT".equals(role) && !quote.getClientId().equals(userId))) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                List<QuoteItem> items = quoteService.getQuoteItems(quoteId);

                response.addProperty("success", true);
                response.add("quote", gson.toJsonTree(quote));
                response.add("items", gson.toJsonTree(items));
                resp.setStatus(200);
            }
            // GET /api/quotes - Get quotes by role
            else {
                List<Quote> quotes;

                if ("CLIENT".equals(role)) {
                    // CLIENT: get their own quotes
                    quotes = quoteService.getQuotesByClient(userId);
                } else if ("ADMIN".equals(role)) {
                    // ADMIN: get all quotes
                    String status = req.getParameter("status");
                    if (status != null && !status.isEmpty()) {
                        quotes = quoteService.getQuotesByStatus(status);
                    } else {
                        quotes = quoteService.getAllQuotes();
                    }
                } else {
                    // SUPPLIER: no access
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Suppliers cannot access quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.addProperty("count", quotes.size());
                response.add("quotes", gson.toJsonTree(quotes));
                resp.setStatus(200);
            }

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            HttpSession session = req.getSession(false);
            String role = (String) session.getAttribute("role");
            String userId = (String) session.getAttribute("userId");

            // Check if user is CLIENT
            if (!"CLIENT".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only clients can create quotes");
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

            // Parse items
            List<QuoteItem> items = gson.fromJson(requestJson.get("items").toString(), 
                    new TypeToken<List<QuoteItem>>(){}.getType());

            // Create quote
            Quote quote = quoteService.createQuote(userId, items);

            response.addProperty("success", true);
            response.addProperty("message", "Quote created successfully");
            response.add("quote", gson.toJsonTree(quote));
            resp.setStatus(201);

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
            String userId = (String) session.getAttribute("userId");

            // Extract quote ID and action from path
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
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            String quoteId = pathParts[1];
            String action = pathParts[2];

            // Handle different actions
            if ("review".equals(action)) {
                // PUT /api/quotes/{id}/review - ADMIN sets pricing
                if (!"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only admins can review quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                // Read request body for item prices
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                Map<String, BigDecimal> itemPrices = gson.fromJson(requestJson.get("itemPrices").toString(), 
                        new TypeToken<Map<String, BigDecimal>>(){}.getType());

                boolean reviewed = quoteService.reviewQuote(quoteId, itemPrices);

                if (reviewed) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Quote reviewed successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to review quote");
                    resp.setStatus(400);
                }

            } else if ("accept".equals(action)) {
                // PUT /api/quotes/{id}/accept - CLIENT accepts quote
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can accept quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                // Ownership: a client may only accept its own quote.
                Quote acceptQuote = quoteService.getQuoteWithItems(quoteId);
                if (!acceptQuote.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only accept your own quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean accepted = quoteService.acceptQuote(quoteId);

                if (accepted) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Quote accepted successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to accept quote");
                    resp.setStatus(400);
                }

            } else if ("reject".equals(action)) {
                // PUT /api/quotes/{id}/reject - CLIENT rejects quote
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can reject quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                // Ownership: a client may only reject its own quote.
                Quote rejectQuote = quoteService.getQuoteWithItems(quoteId);
                if (!rejectQuote.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only reject your own quotes");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean rejected = quoteService.rejectQuote(quoteId);

                if (rejected) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Quote rejected successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to reject quote");
                    resp.setStatus(400);
                }

            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid action");
                resp.setStatus(400);
            }

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
}
