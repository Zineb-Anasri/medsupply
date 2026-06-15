package com.medsupply.services;

import com.medsupply.dao.ClientDAO;
import com.medsupply.dao.DeliveryDAO;
import com.medsupply.dao.OrderDAO;
import com.medsupply.dao.PaymentDAO;
import com.medsupply.models.Client;
import com.medsupply.models.Delivery;
import com.medsupply.models.Order;
import com.medsupply.models.Payment;

import java.time.LocalDateTime;
import java.util.List;
/**
 * DeliveryService - Business logic for delivery management
 * Handles delivery eligibility, lifecycle management, and GPS tracking preparation
 * 
 * Business Rules:
 * - Delivery can only be created for orders with PAID or PROCESSING status
 * - Delivery lifecycle: PENDING → PREPARING → IN_TRANSIT → DELIVERED → RECEIVED
 * - Prepared for future Supabase Realtime GPS tracking integration
 */
public class DeliveryService {

    private DeliveryDAO deliveryDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private ClientDAO clientDAO;

    public DeliveryService() {
        this.deliveryDAO = new DeliveryDAO();
        this.orderDAO = new OrderDAO();
        this.paymentDAO = new PaymentDAO();
        this.clientDAO = new ClientDAO();
    }

    /**
     * Create delivery for an order (only for eligible orders)
     * @param orderId Order ID
     * @param deliveryAddress Delivery address
     * @param contactPhone Contact phone
     * @param contactPerson Contact person
     * @return Created delivery
     */
    public Delivery createDeliveryForOrder(String orderId, String deliveryAddress, 
                                          String contactPhone, String contactPerson) throws Exception {
        // Validate order exists
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new Exception("Order not found");
        }

        // Check if delivery already exists for this order
        Delivery existingDelivery = deliveryDAO.findByOrderId(orderId);
        if (existingDelivery != null) {
            throw new Exception("Delivery already exists for this order");
        }

        // Validate order eligibility for delivery
        if (!isOrderEligibleForDelivery(orderId)) {
            throw new Exception("Order is not eligible for delivery. Order must be PAID or PROCESSING");
        }

        // Get client address if not provided
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            Client client = clientDAO.findByUserId(order.getClientId());
            if (client != null && client.getAddress() != null) {
                deliveryAddress = client.getAddress();
            }
        }

        // Create delivery with PENDING status
        Delivery delivery = new Delivery(orderId, order.getClientId(), "PENDING", deliveryAddress);
        delivery.setContactPhone(contactPhone);
        delivery.setContactPerson(contactPerson);
        
        // Set estimated delivery date (3 days from now)
        delivery.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
        
        Delivery createdDelivery = deliveryDAO.create(delivery);

        // Update order status to PROCESSING if it was PAID
        if ("PAID".equals(getOrderPaymentStatus(orderId))) {
            orderDAO.updateStatus(orderId, "PROCESSING");
        }

        return createdDelivery;
    }

    /**
     * Check if order is eligible for delivery
     * @param orderId Order ID
     * @return true if eligible
     */
    public boolean isOrderEligibleForDelivery(String orderId) throws Exception {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            return false;
        }

        // Order must be in PROCESSING status or payment must be PAID
        if ("PROCESSING".equals(order.getStatus())) {
            return true;
        }

        // Check payment status
        String paymentStatus = getOrderPaymentStatus(orderId);
        return "PAID".equals(paymentStatus);
    }

    /**
     * Get payment status for order
     * @param orderId Order ID
     * @return Payment status
     */
    private String getOrderPaymentStatus(String orderId) throws Exception {
        Payment payment = paymentDAO.findByOrderId(orderId);
        if (payment == null) {
            return null;
        }
        return payment.getStatus();
    }

    /**
     * Get delivery by ID
     * @param deliveryId Delivery ID
     * @return Delivery
     */
    public Delivery getDelivery(String deliveryId) throws Exception {
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }
        return delivery;
    }

    /**
     * Get delivery by order ID
     * @param orderId Order ID
     * @return Delivery
     */
    public Delivery getDeliveryByOrder(String orderId) throws Exception {
        Delivery delivery = deliveryDAO.findByOrderId(orderId);
        if (delivery == null) {
            throw new Exception("Delivery not found for this order");
        }
        return delivery;
    }

    /**
     * Get all deliveries (ADMIN)
     * @return List of all deliveries
     */
    public List<Delivery> getAllDeliveries() throws Exception {
        return deliveryDAO.findAll();
    }

    /**
     * Get deliveries by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of deliveries for the client
     */
    public List<Delivery> getDeliveriesByClient(String clientId) throws Exception {
        return deliveryDAO.findByClientId(clientId);
    }

    /**
     * Get deliveries by status (ADMIN)
     * @param status Delivery status
     * @return List of deliveries with the status
     */
    public List<Delivery> getDeliveriesByStatus(String status) throws Exception {
        return deliveryDAO.findByStatus(status);
    }

    /**
     * Get active deliveries (ADMIN)
     * @return List of active deliveries (PENDING, PREPARING, IN_TRANSIT)
     */
    public List<Delivery> getActiveDeliveries() throws Exception {
        return deliveryDAO.findActive();
    }

    /**
     * Update delivery status (ADMIN)
     * @param deliveryId Delivery ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateDeliveryStatus(String deliveryId, String status) throws Exception {
        // Validate delivery exists
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }

        // Validate status transition
        if (!isValidStatusTransition(delivery.getStatus(), status)) {
            throw new Exception("Invalid status transition from " + delivery.getStatus() + " to " + status);
        }

        boolean updated = deliveryDAO.updateStatus(deliveryId, status);

        // If status is DELIVERED, update order status to DELIVERED
        if (updated && "DELIVERED".equals(status)) {
            orderDAO.updateStatus(delivery.getOrderId(), "DELIVERED");
        }

        return updated;
    }

    /**
     * Update GPS location (prepared for Supabase Realtime integration)
     * @param deliveryId Delivery ID
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true if update successful
     */
    public boolean updateGpsLocation(String deliveryId, Double latitude, Double longitude) throws Exception {
        // Validate delivery exists
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }

        // Only allow GPS updates for IN_TRANSIT deliveries
        if (!"IN_TRANSIT".equals(delivery.getStatus())) {
            throw new Exception("GPS tracking only available for IN_TRANSIT deliveries");
        }

        return deliveryDAO.updateLocation(deliveryId, latitude, longitude);
    }

    /**
     * Update delivery details (ADMIN)
     * @param deliveryId Delivery ID
     * @param deliveryAddress Delivery address
     * @param contactPhone Contact phone
     * @param contactPerson Contact person
     * @param estimatedDeliveryDate Estimated delivery date
     * @return true if update successful
     */
    public boolean updateDeliveryDetails(String deliveryId, String deliveryAddress, String contactPhone, 
                                          String contactPerson, LocalDateTime estimatedDeliveryDate) throws Exception {
        // Validate delivery exists
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }

        // Only allow updates for PENDING or PREPARING deliveries
        if (!"PENDING".equals(delivery.getStatus()) && !"PREPARING".equals(delivery.getStatus())) {
            throw new Exception("Delivery details can only be updated in PENDING or PREPARING status");
        }

        return deliveryDAO.updateDetails(deliveryId, deliveryAddress, contactPhone, contactPerson, estimatedDeliveryDate);
    }

    /**
     * Mark delivery as delivered (ADMIN)
     * @param deliveryId Delivery ID
     * @param deliveryNotes Delivery notes
     * @return true if update successful
     */
    public boolean markDeliveryAsDelivered(String deliveryId, String deliveryNotes) throws Exception {
        // Validate delivery exists
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }

        // Can only mark IN_TRANSIT deliveries as delivered
        if (!"IN_TRANSIT".equals(delivery.getStatus())) {
            throw new Exception("Delivery can only be marked as delivered from IN_TRANSIT status");
        }

        boolean updated = deliveryDAO.markDelivered(deliveryId, LocalDateTime.now(), deliveryNotes);

        // Update order status to DELIVERED
        if (updated) {
            orderDAO.updateStatus(delivery.getOrderId(), "DELIVERED");
        }

        return updated;
    }

    /**
     * Confirm delivery receipt (CLIENT)
     * @param deliveryId Delivery ID
     * @return true if update successful
     */
    public boolean confirmDeliveryReceipt(String deliveryId) throws Exception {
        // Validate delivery exists
        Delivery delivery = deliveryDAO.findById(deliveryId);
        if (delivery == null) {
            throw new Exception("Delivery not found");
        }

        // Can only confirm DELIVERED deliveries
        if (!"DELIVERED".equals(delivery.getStatus())) {
            throw new Exception("Delivery can only be confirmed in DELIVERED status");
        }

        boolean updated = deliveryDAO.updateStatus(deliveryId, "RECEIVED");

        // Update order status to COMPLETED
        if (updated) {
            orderDAO.updateStatus(delivery.getOrderId(), "COMPLETED");
        }

        return updated;
    }

    /**
     * Validate status transition
     * Valid transitions:
     * PENDING → PREPARING
     * PREPARING → IN_TRANSIT
     * IN_TRANSIT → DELIVERED
     * DELIVERED → RECEIVED
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case "PENDING":
                return "PREPARING".equals(newStatus);
            case "PREPARING":
                return "IN_TRANSIT".equals(newStatus);
            case "IN_TRANSIT":
                return "DELIVERED".equals(newStatus);
            case "DELIVERED":
                return "RECEIVED".equals(newStatus);
            case "RECEIVED":
                return false; // No transitions from RECEIVED
            default:
                return false;
        }
    }
}
