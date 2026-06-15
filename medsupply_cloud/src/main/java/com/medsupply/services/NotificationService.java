package com.medsupply.services;

import com.medsupply.dao.NotificationDAO;
import com.medsupply.models.Notification;

import java.util.List;
/**
 * NotificationService - Business logic for notification management
 * Handles notification creation, retrieval, and read status management
 * 
 * Business Rules:
 * - Notifications are created automatically by other services (orders, payments, delivery, maintenance)
 * - Users can retrieve their notifications
 * - Users can mark notifications as read
 * - Admin can send system notifications
 * 
 * Integration Hooks:
 * - createNotification() - Public method for other services to call
 */
public class NotificationService {

    private NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Create notification (integration hook for other services)
     * This method is called by OrderService, PaymentService, DeliveryService, MaintenanceService
     * @param userId User ID
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type (ORDER, PAYMENT, DELIVERY, MAINTENANCE, SYSTEM)
     * @return Created notification
     */
    public Notification createNotification(String userId, String title, String message, String type) throws Exception {
        // Validate type
        if (!isValidType(type)) {
            throw new Exception("Invalid notification type. Valid values: ORDER, PAYMENT, DELIVERY, MAINTENANCE, SYSTEM");
        }

        Notification notification = new Notification(userId, title, message, type);
        return notificationDAO.create(notification);
    }

    /**
     * Get notification by ID
     * @param notificationId Notification ID
     * @return Notification
     */
    public Notification getNotification(String notificationId) throws Exception {
        Notification notification = notificationDAO.findById(notificationId);
        if (notification == null) {
            throw new Exception("Notification not found");
        }
        return notification;
    }

    /**
     * Get notifications by user ID
     * @param userId User ID
     * @return List of notifications for the user
     */
    public List<Notification> getNotificationsByUser(String userId) throws Exception {
        return notificationDAO.findByUserId(userId);
    }

    /**
     * Get unread notifications by user ID
     * @param userId User ID
     * @return List of unread notifications for the user
     */
    public List<Notification> getUnreadNotifications(String userId) throws Exception {
        return notificationDAO.findUnreadByUserId(userId);
    }

    /**
     * Get all notifications (ADMIN)
     * @return List of all notifications
     */
    public List<Notification> getAllNotifications() throws Exception {
        return notificationDAO.findAll();
    }

    /**
     * Get notifications by type (ADMIN)
     * @param type Notification type
     * @return List of notifications with the type
     */
    public List<Notification> getNotificationsByType(String type) throws Exception {
        return notificationDAO.findByType(type);
    }

    /**
     * Mark notification as read
     * @param notificationId Notification ID
     * @return true if update successful
     */
    public boolean markAsRead(String notificationId) throws Exception {
        return notificationDAO.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read for a user
     * @param userId User ID
     * @return true if update successful
     */
    public boolean markAllAsRead(String userId) throws Exception {
        return notificationDAO.markAllAsRead(userId);
    }

    /**
     * Get unread count for a user
     * @param userId User ID
     * @return Number of unread notifications
     */
    public int getUnreadCount(String userId) throws Exception {
        return notificationDAO.getUnreadCount(userId);
    }

    /**
     * Delete notification (ADMIN)
     * @param notificationId Notification ID
     * @return true if deletion successful
     */
    public boolean deleteNotification(String notificationId) throws Exception {
        return notificationDAO.delete(notificationId);
    }

    /**
     * Validate notification type
     */
    private boolean isValidType(String type) {
        return "ORDER".equals(type) || "PAYMENT".equals(type) || 
               "DELIVERY".equals(type) || "MAINTENANCE".equals(type) || 
               "SYSTEM".equals(type);
    }
}
