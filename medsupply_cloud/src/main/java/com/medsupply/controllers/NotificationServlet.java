package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Notification;
import com.medsupply.services.NotificationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
/**
 * NotificationServlet - Handles notification management operations
 * 
 * Endpoints:
 * - GET /api/notifications - Get notifications by user (authenticated users)
 * - GET /api/notifications/unread - Get unread notifications (authenticated users)
 * - GET /api/notifications/count - Get unread count (authenticated users)
 * - GET /api/notifications/{id} - Get notification details
 * - POST /api/notifications - Create notification (ADMIN only - system notifications)
 * - PUT /api/notifications/{id}/read - Mark notification as read (owner only)
 * - PUT /api/notifications/read-all - Mark all notifications as read (authenticated users)
 * 
 * Security:
 * - All endpoints require authentication (protected by AuthFilter)
 * - Users can view and manage their own notifications
 * - Admin can send system notifications and view all notifications
 * - Integration hooks available for other services to create notifications via NotificationService
 */
@WebServlet("/api/notifications/*")
public class NotificationServlet extends HttpServlet {

    private NotificationService notificationService;
    private Gson gson;

    @Override
    public void init() {
        this.notificationService = new NotificationService();
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

            // GET /api/notifications/count - Get unread count
            if (pathInfo != null && pathInfo.equals("/count")) {
                int unreadCount = notificationService.getUnreadCount(userId);
                response.addProperty("success", true);
                response.addProperty("unreadCount", unreadCount);
                resp.setStatus(200);
            }
            // GET /api/notifications/unread - Get unread notifications
            else if (pathInfo != null && pathInfo.equals("/unread")) {
                List<Notification> notifications = notificationService.getUnreadNotifications(userId);
                response.addProperty("success", true);
                response.addProperty("count", notifications.size());
                response.add("notifications", gson.toJsonTree(notifications));
                resp.setStatus(200);
            }
            // GET /api/notifications/{id} - Get notification details
            else if (pathInfo != null && pathInfo.matches("/[^/]+")) {
                String notificationId = pathInfo.substring(1);
                Notification notification = notificationService.getNotification(notificationId);

                // Users can only view their own notifications
                if (!notification.getUserId().equals(userId) && !"ADMIN".equals(role)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only view your own notifications");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                response.addProperty("success", true);
                response.add("notification", gson.toJsonTree(notification));
                resp.setStatus(200);
            }
            // GET /api/notifications - Get notifications by user
            else if (pathInfo == null || pathInfo.equals("/")) {
                List<Notification> notifications;

                if ("ADMIN".equals(role)) {
                    // Admin can optionally filter by type
                    String type = req.getParameter("type");
                    if (type != null && !type.isEmpty()) {
                        notifications = notificationService.getNotificationsByType(type);
                    } else {
                        notifications = notificationService.getAllNotifications();
                    }
                } else {
                    // Regular users get their own notifications
                    notifications = notificationService.getNotificationsByUser(userId);
                }

                response.addProperty("success", true);
                response.addProperty("count", notifications.size());
                response.add("notifications", gson.toJsonTree(notifications));
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid notification ID format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
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

            // POST /api/notifications - Create notification (ADMIN only - system notifications)
            if (!"ADMIN".equals(role)) {
                response.addProperty("success", false);
                response.addProperty("message", "Unauthorized - Only admins can create system notifications");
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
            String userId = requestJson.get("userId").getAsString();
            String title = requestJson.get("title").getAsString();
            String message = requestJson.get("message").getAsString();
            String type = requestJson.get("type").getAsString();

            Notification notification = notificationService.createNotification(userId, title, message, type);

            response.addProperty("success", true);
            response.addProperty("message", "Notification created successfully");
            response.add("notification", gson.toJsonTree(notification));
            resp.setStatus(201);

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid request format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
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
            String userId = (String) session.getAttribute("userId");

            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // PUT /api/notifications/read-all - Mark all notifications as read
            if (pathInfo.equals("/read-all")) {
                boolean updated = notificationService.markAllAsRead(userId);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "All notifications marked as read");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "No unread notifications to mark");
                    resp.setStatus(400);
                }
            }
            // PUT /api/notifications/{id}/read - Mark notification as read
            else if (pathInfo.matches("/[^/]+/read")) {
                String notificationId = pathInfo.substring(1, pathInfo.indexOf("/read"));
                Notification notification = notificationService.getNotification(notificationId);

                // Users can only mark their own notifications as read
                if (!notification.getUserId().equals(userId)) {
                    response.addProperty("success", false);
                    response.addProperty("message", "Unauthorized - You can only mark your own notifications as read");
                    resp.setStatus(403);
                    resp.getWriter().print(gson.toJson(response));
                    return;
                }

                boolean updated = notificationService.markAsRead(notificationId);

                if (updated) {
                    response.addProperty("success", true);
                    response.addProperty("message", "Notification marked as read");
                    resp.setStatus(200);
                } else {
                    response.addProperty("success", false);
                    response.addProperty("message", "Failed to mark notification as read");
                    resp.setStatus(400);
                }
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid request path. Use /api/notifications/read-all or /api/notifications/{id}/read");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
            }

        } catch (IllegalArgumentException e) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid notification ID format");
            resp.setStatus(400);
            resp.getWriter().print(gson.toJson(response));
        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(response));
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
