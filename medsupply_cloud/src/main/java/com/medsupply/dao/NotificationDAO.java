package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Notification;
import com.medsupply.utils.SupabaseClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * NotificationDAO - Data Access Object for Notification table
 * Handles all database operations for notifications
 */
public class NotificationDAO {

    /**
     * Find notification by ID
     * @param notificationId Notification ID (UUID)
     * @return Notification object if found, null otherwise
     */
    public Notification findById(String notificationId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(notificationId);
        String response = SupabaseClient.get("notifications", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToNotification(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find notifications by user ID
     * @param userId User ID
     * @return List of notifications for the user
     */
    public List<Notification> findByUserId(String userId) throws Exception {
        String filters = "user_id=eq." + SupabaseClient.enc(userId) + "&order=created_at.desc";
        String response = SupabaseClient.get("notifications", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            notifications.add(mapJsonToNotification(jsonArray.get(i).getAsJsonObject()));
        }
        return notifications;
    }

    /**
     * Find unread notifications by user ID
     * @param userId User ID
     * @return List of unread notifications for the user
     */
    public List<Notification> findUnreadByUserId(String userId) throws Exception {
        String filters = "user_id=eq." + SupabaseClient.enc(userId) + "&is_read=eq.false&order=created_at.desc";
        String response = SupabaseClient.get("notifications", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            notifications.add(mapJsonToNotification(jsonArray.get(i).getAsJsonObject()));
        }
        return notifications;
    }

    /**
     * Find notifications by type
     * @param type Notification type
     * @return List of notifications with the type
     */
    public List<Notification> findByType(String type) throws Exception {
        String filters = "type=eq." + SupabaseClient.enc(type) + "&order=created_at.desc";
        String response = SupabaseClient.get("notifications", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            notifications.add(mapJsonToNotification(jsonArray.get(i).getAsJsonObject()));
        }
        return notifications;
    }

    /**
     * Find all notifications
     * @return List of all notifications
     */
    public List<Notification> findAll() throws Exception {
        String response = SupabaseClient.get("notifications", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            notifications.add(mapJsonToNotification(jsonArray.get(i).getAsJsonObject()));
        }
        return notifications;
    }

    /**
     * Create a new notification
     * @param notification Notification object to create
     * @return Created notification
     */
    public Notification create(Notification notification) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", notification.getNotificationId());
        body.addProperty("user_id", notification.getUserId());
        body.addProperty("title", notification.getTitle());
        body.addProperty("message", notification.getMessage());
        body.addProperty("type", notification.getType());
        body.addProperty("is_read", notification.getIsRead());
        body.addProperty("created_at", notification.getCreatedAt().toString());
        
        String response = SupabaseClient.post("notifications", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToNotification(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Mark notification as read
     * @param notificationId Notification ID
     * @return true if update successful
     */
    public boolean markAsRead(String notificationId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_read", true);
        
        String filters = "id=eq." + SupabaseClient.enc(notificationId);
        String response = SupabaseClient.patchWithFilters("notifications", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Mark all notifications as read for a user
     * @param userId User ID
     * @return true if update successful
     */
    public boolean markAllAsRead(String userId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_read", true);

        String filters = "user_id=eq." + SupabaseClient.enc(userId) + "&is_read=eq.false";
        String response = SupabaseClient.patchWithFilters("notifications", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete notification
     * @param notificationId Notification ID
     * @return true if deletion successful
     */
    public boolean delete(String notificationId) throws Exception {
        String response = SupabaseClient.delete("notifications", notificationId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Get unread count for a user
     * @param userId User ID
     * @return Number of unread notifications
     */
    public int getUnreadCount(String userId) throws Exception {
        String filters = "user_id=eq." + SupabaseClient.enc(userId) + "&is_read=eq.false";
        String response = SupabaseClient.get("notifications", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        return jsonArray.size();
    }

    /**
     * Map JSON to Notification object
     */
    private Notification mapJsonToNotification(JsonObject json) {
        Notification notification = new Notification();
        if (json.has("id") && !json.get("id").isJsonNull())
            notification.setNotificationId(json.get("id").getAsString());
        if (json.has("user_id") && !json.get("user_id").isJsonNull())
            notification.setUserId(json.get("user_id").getAsString());
        if (json.has("title") && !json.get("title").isJsonNull())
            notification.setTitle(json.get("title").getAsString());
        if (json.has("message") && !json.get("message").isJsonNull())
            notification.setMessage(json.get("message").getAsString());
        if (json.has("type") && !json.get("type").isJsonNull())
            notification.setType(json.get("type").getAsString());
        if (json.has("is_read") && !json.get("is_read").isJsonNull())
            notification.setIsRead(json.get("is_read").getAsBoolean());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                notification.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return notification;
    }
}
