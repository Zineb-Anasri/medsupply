package com.medsupply.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification model - Represents a notification for a user
 * Used by all modules (orders, payments, delivery, maintenance, quotes)
 * 
 * Business Rules:
 * - Notifications are created automatically by other services
 * - Users can retrieve and mark notifications as read
 * - Admin can send system notifications
 */
public class Notification {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type; // ORDER, PAYMENT, DELIVERY, MAINTENANCE, SYSTEM
    private Boolean isRead;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(String userId, String title, String message, String type) {
        this.notificationId = UUID.randomUUID().toString();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Check if notification is unread
     */
    public boolean isUnread() {
        return isRead != null && !isRead;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
