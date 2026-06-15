package com.medsupply.controllers;

import com.google.gson.Gson;
import com.medsupply.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.SupplierBid;
import com.medsupply.models.Tender;
import com.medsupply.models.TenderItem;
import com.medsupply.services.TenderService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * TenderServlet - Handles B2B reverse auction tender management operations
 * 
 * Endpoints:
 * - POST /api/tenders - Create tender (CLIENT only)
 * - GET /api/tenders - List tenders (CLIENT: own, SUPPLIER: open, ADMIN: all)
 * - GET /api/tenders/open - Get open tenders (SUPPLIER)
 * - GET /api/tenders/{id} - Get tender details
 * - GET /api/tenders/{id}/items - Get tender items
 * - POST /api/tenders/{id}/bids - Submit bid (SUPPLIER only)
 * - GET /api/tenders/{id}/bids - Get bids for tender
 * - GET /api/tenders/{id}/best-bid - Get best bid for tender
 * - PUT /api/tenders/{id}/close - Close tender (CLIENT or ADMIN)
 * - PUT /api/tenders/{id}/award - Award tender (CLIENT or ADMIN)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - CLIENT can create and manage own tenders
 * - SUPPLIER can view open tenders and submit bids
 * - ADMIN can view all tenders and manage them
 */
@WebServlet("/api/tenders/*")
public class TenderServlet extends HttpServlet {

    private TenderService tenderService;
    private Gson gson;

    @Override
    public void init() {
        this.tenderService = new TenderService();
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
            String userId = (String) session.getAttribute("clientId"); // client profile id (for tender ownership/create)

            String pathInfo = req.getPathInfo();

            // GET /api/tenders/my-bids - the supplier's own bids across all tenders (SUPPLIER)
            if (pathInfo != null && pathInfo.equals("/my-bids")) {
                if (!"SUPPLIER".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only suppliers have bids");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                List<SupplierBid> myBids = tenderService.getBidsBySupplier((String) session.getAttribute("supplierId"));
                response.addProperty("success", true);
                response.addProperty("count", myBids.size());
                response.add("bids", gson.toJsonTree(myBids));
                resp.setStatus(200);
            }
            // GET /api/tenders/open - Get open tenders (SUPPLIER)
            else if (pathInfo != null && pathInfo.equals("/open")) {
                if (!"SUPPLIER".equals(role) && !"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only suppliers and admins can view open tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                List<Tender> tenders = tenderService.getOpenTenders();
                response.addProperty("success", true);
                response.addProperty("count", tenders.size());
                response.add("tenders", gson.toJsonTree(tenders));
                resp.setStatus(200);
            }
            // GET /api/tenders/{id}/items - Get tender items
            else if (pathInfo != null && pathInfo.matches("/[^/]+/items")) {
                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/items"));
                // A non-owning CLIENT must not read another client's tender items.
                // ADMIN and SUPPLIER (who need the items to bid) are allowed.
                Tender itemsTender = tenderService.getTender(tenderId);
                if ("CLIENT".equals(role) && !itemsTender.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own tender items");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                List<TenderItem> items = tenderService.getTenderItems(tenderId);
                response.addProperty("success", true);
                response.addProperty("count", items.size());
                response.add("items", gson.toJsonTree(items));
                resp.setStatus(200);
            }
            // GET /api/tenders/{id}/bids - Get bids for tender
            else if (pathInfo != null && pathInfo.matches("/[^/]+/bids")) {
                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/bids"));
                // Bids are competitive: only the owning CLIENT or an ADMIN may read them.
                Tender bidsTender = tenderService.getTender(tenderId);
                if (!"ADMIN".equals(role) && !("CLIENT".equals(role) && bidsTender.getClientId().equals(userId))) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only the tender owner or an admin can view bids");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                List<SupplierBid> bids = tenderService.getBidsByTender(tenderId);
                response.addProperty("success", true);
                response.addProperty("count", bids.size());
                response.add("bids", gson.toJsonTree(bids));
                resp.setStatus(200);
            }
            // GET /api/tenders/{id}/best-bid - Get best bid for tender
            else if (pathInfo != null && pathInfo.matches("/[^/]+/best-bid")) {
                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/best-bid"));
                // Best bid is competitive: only the owning CLIENT or an ADMIN may read it.
                Tender bestBidTender = tenderService.getTender(tenderId);
                if (!"ADMIN".equals(role) && !("CLIENT".equals(role) && bestBidTender.getClientId().equals(userId))) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only the tender owner or an admin can view the best bid");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }
                SupplierBid bestBid = tenderService.getBestBid(tenderId);
                response.addProperty("success", true);
                if (bestBid != null) {
                    response.add("bestBid", gson.toJsonTree(bestBid));
                } else {
                    response.addProperty("bestBid", (String) null);
                }
                resp.setStatus(200);
            }
            // GET /api/tenders/{id} - Get tender details
            else if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String tenderId = pathInfo.substring(1);
                Tender tender = tenderService.getTender(tenderId);

                // Clients can only view their own tenders
                if ("CLIENT".equals(role) && !tender.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.add("tender", gson.toJsonTree(tender));
                resp.setStatus(200);
            }
            // GET /api/tenders - List tenders
            else if (pathInfo == null || pathInfo.equals("/")) {
                List<Tender> tenders;

                if ("ADMIN".equals(role)) {
                    tenders = tenderService.getAllTenders();
                } else if ("CLIENT".equals(role)) {
                    tenders = tenderService.getTendersByClient(userId);
                } else if ("SUPPLIER".equals(role)) {
                    tenders = tenderService.getOpenTenders();
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.addProperty("count", tenders.size());
                response.add("tenders", gson.toJsonTree(tenders));
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid UUID format");
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
            String userId = (String) session.getAttribute("clientId"); // client profile id (for tender ownership/create)

            String pathInfo = req.getPathInfo();

            // POST /api/tenders/{id}/bids - Submit bid (SUPPLIER only)
            if (pathInfo != null && pathInfo.matches("/[^/]+/bids")) {
                if (!"SUPPLIER".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only suppliers can submit bids");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/bids"));

                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = req.getReader().readLine()) != null) {
                    requestBody.append(line);
                }

                JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                BigDecimal price = requestJson.get("price").getAsBigDecimal();

                SupplierBid bid = tenderService.submitBid(tenderId, (String) session.getAttribute("supplierId"), price);

                response.addProperty("success", true);
                response.addProperty("message", "Bid submitted successfully");
                response.add("bid", gson.toJsonTree(bid));
                resp.setStatus(201);
            }
            // POST /api/tenders - Create tender (CLIENT only)
            else if (pathInfo == null || pathInfo.equals("/")) {
                if (!"CLIENT".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients can create tenders");
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
                String title = requestJson.get("title").getAsString();
                String description = requestJson.get("description").getAsString();
                BigDecimal budgetMax = requestJson.get("budgetMax").getAsBigDecimal();
                LocalDateTime deadline = LocalDateTime.parse(requestJson.get("deadline").getAsString());

                // Parse items array if present
                List<TenderItem> items = null;
                if (requestJson.has("items") && requestJson.get("items").isJsonArray()) {
                    items = gson.fromJson(requestJson.get("items").getAsJsonArray(), 
                                         new com.google.gson.reflect.TypeToken<List<TenderItem>>() {}.getType());
                }

                Tender tender = tenderService.createTender(userId, title, description, budgetMax, deadline, items);

                response.addProperty("success", true);
                response.addProperty("message", "Tender created successfully");
                response.add("tender", gson.toJsonTree(tender));
                resp.setStatus(201);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid request format");
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
            String userId = (String) session.getAttribute("clientId"); // client profile id (for tender ownership/create)

            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // PUT /api/tenders/{id}/award - Award tender (CLIENT or ADMIN)
            if (pathInfo.matches("/[^/]+/award")) {
                if (!"CLIENT".equals(role) && !"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients and admins can award tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/award"));
                Tender tender = tenderService.getTender(tenderId);

                // Clients can only award their own tenders
                if ("CLIENT".equals(role) && !tender.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only award your own tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean awarded = tenderService.awardTender(tenderId);

                if (awarded) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Tender awarded successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to award tender");
                    resp.setStatus(400);
                }
            }
            // PUT /api/tenders/{id}/close - Close tender (CLIENT or ADMIN)
            else if (pathInfo.matches("/[^/]+/close")) {
                if (!"CLIENT".equals(role) && !"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - Only clients and admins can close tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                String tenderId = pathInfo.substring(1, pathInfo.indexOf("/close"));
                Tender tender = tenderService.getTender(tenderId);

                // Clients can only close their own tenders
                if ("CLIENT".equals(role) && !tender.getClientId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only close your own tenders");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean closed = tenderService.closeTender(tenderId);

                if (closed) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Tender closed successfully");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to close tender");
                    resp.setStatus(400);
                }
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path. Use /api/tenders/{id}/close or /api/tenders/{id}/award");
                resp.setStatus(400);
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid UUID format");
            resp.setStatus(400);
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
