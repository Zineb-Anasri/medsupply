package com.medsupply.services;

import com.medsupply.dao.DeliveryDAO;
import com.medsupply.dao.NotificationDAO;
import com.medsupply.dao.PaymentDAO;
import com.medsupply.dao.ProductDAO;
import com.medsupply.models.Notification;

/**
 * RealtimeService - Helper class for Supabase Realtime integration
 * 
 * IMPORTANT: This service does NOT implement WebSockets or realtime logic in Java.
 * It provides documented methods that trigger database updates which Supabase Realtime
 * automatically broadcasts to subscribed frontend clients.
 * 
 * ARCHITECTURE:
 * - Java backend updates database via JDBC (existing DAOs)
 * - Supabase Realtime broadcasts changes to subscribed clients
 * - Frontend subscribes to Supabase Realtime channels using JavaScript
 * 
 * TABLES WITH REALTIME ENABLED (in Supabase):
 * - deliveries (for GPS tracking)
 * - payments (for payment status updates)
 * - notifications (for instant notification delivery)
 * - products (for stock updates)
 * 
 * USAGE:
 * Call these methods when you want to trigger realtime updates.
 * The underlying JDBC operations will automatically trigger Supabase Realtime events.
 */
public class RealtimeService {

    private DeliveryDAO deliveryDAO;
    private PaymentDAO paymentDAO;
    private NotificationDAO notificationDAO;
    private ProductDAO productDAO;

    public RealtimeService() {
        this.deliveryDAO = new DeliveryDAO();
        this.paymentDAO = new PaymentDAO();
        this.notificationDAO = new NotificationDAO();
        this.productDAO = new ProductDAO();
    }

    // ==================== DELIVERY REAL-TIME TRACKING ====================

    /**
     * Update delivery GPS location (triggers realtime event)
     * Supabase Realtime will broadcast this update to subscribed clients
     * 
     * @param deliveryId Delivery ID
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true if update successful
     */
    public boolean updateDeliveryLocation(String deliveryId, Double latitude, Double longitude) throws Exception {
        return deliveryDAO.updateLocation(deliveryId, latitude, longitude);
    }

    /**
     * Update delivery status (triggers realtime event)
     * Supabase Realtime will broadcast this update to subscribed clients
     * 
     * @param deliveryId Delivery ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateDeliveryStatus(String deliveryId, String status) throws Exception {
        return deliveryDAO.updateStatus(deliveryId, status);
    }

    // ==================== PAYMENT REAL-TIME UPDATES ====================

    /**
     * Update payment status (triggers realtime event)
     * Supabase Realtime will broadcast this update to subscribed clients
     * 
     * @param paymentId Payment ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updatePaymentStatus(String paymentId, String status) throws Exception {
        return paymentDAO.updateStatus(paymentId, status);
    }

    /**
     * Update payment amount (triggers realtime event)
     * Supabase Realtime will broadcast this update to subscribed clients
     * 
     * @param paymentId Payment ID
     * @param paidAmount Amount paid
     * @return true if update successful
     */
    public boolean updatePaymentAmount(String paymentId, java.math.BigDecimal paidAmount) throws Exception {
        return paymentDAO.updatePaidAmount(paymentId, paidAmount);
    }

    // ==================== NOTIFICATION REAL-TIME SYSTEM ====================

    /**
     * Create notification (triggers realtime INSERT event)
     * Supabase Realtime will broadcast this new notification to subscribed clients
     * 
     * @param userId User ID
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type
     * @return Created notification
     */
    public Notification createNotification(String userId, String title, String message, String type) throws Exception {
        Notification notification = new Notification(userId, title, message, type);
        return notificationDAO.create(notification);
    }

    /**
     * Mark notification as read (triggers realtime UPDATE event)
     * Supabase Realtime will broadcast this update to subscribed clients
     * 
     * @param notificationId Notification ID
     * @return true if update successful
     */
    public boolean markNotificationAsRead(String notificationId) throws Exception {
        return notificationDAO.markAsRead(notificationId);
    }

    // ==================== STOCK REAL-TIME UPDATES ====================

    /**
     * Update product stock (triggers realtime event)
     * Note: Stock is managed in the separate "stocks" table in Supabase.
     * This method is a placeholder until a StockDAO is implemented.
     * 
     * @param productId Product ID (UUID)
     * @param stock New stock quantity
     * @return true if update successful
     */
    public boolean updateProductStock(String productId, Integer stock) throws Exception {
        throw new Exception("Stock management via products table is deprecated. Use stocks table directly.");
    }
}
