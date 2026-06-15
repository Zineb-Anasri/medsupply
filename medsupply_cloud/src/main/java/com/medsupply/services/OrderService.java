package com.medsupply.services;

import com.medsupply.dao.OrderDAO;
import com.medsupply.dao.OrderItemDAO;
import com.medsupply.dao.QuoteDAO;
import com.medsupply.dao.QuoteItemDAO;
import com.medsupply.models.Order;
import com.medsupply.models.OrderItem;
import com.medsupply.models.Quote;
import com.medsupply.models.QuoteItem;

import java.math.BigDecimal;
import java.util.List;
/**
 * OrderService - Business logic for order management
 * Handles quote-to-order conversion, order creation, and status management
 */
public class OrderService {

    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private QuoteDAO quoteDAO;
    private QuoteItemDAO quoteItemDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.quoteDAO = new QuoteDAO();
        this.quoteItemDAO = new QuoteItemDAO();
    }

    /**
     * Convert an approved quote to an order
     * @param quoteId Quote ID
     * @return Created order
     */
    public Order convertQuoteToOrder(String quoteId) throws Exception {
        // Validate quote exists and is in APPROVED status
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }
        if (!"APPROVED".equals(quote.getStatus())) {
            throw new Exception("Quote must be in APPROVED status to convert to order");
        }

        // Prevent double conversion - check if order already exists for this quote
        Order existingOrder = orderDAO.findByQuoteId(quoteId);
        if (existingOrder != null) {
            throw new Exception("Quote has already been converted to an order");
        }

        // Get quote items
        List<QuoteItem> quoteItems = quoteItemDAO.findByQuoteId(quoteId);
        if (quoteItems == null || quoteItems.isEmpty()) {
            throw new Exception("Quote has no items");
        }

        // Create order with PENDING status
        Order order = new Order(quoteId, quote.getClientId(), "PENDING", quote.getTotalAmount());
        Order createdOrder = orderDAO.create(order);

        // Create order items from quote items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (QuoteItem quoteItem : quoteItems) {
            // Use admin price if available, otherwise use proposed price
            BigDecimal unitPrice = quoteItem.getAdminPrice() != null ? 
                quoteItem.getAdminPrice() : quoteItem.getProposedPrice();
            
            OrderItem orderItem = new OrderItem(createdOrder.getOrderId(), 
                quoteItem.getProductId(), 
                quoteItem.getQuantity(), 
                unitPrice);
            
            orderItemDAO.create(orderItem);
            
            // Calculate total
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quoteItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // Update order total amount
        orderDAO.updateTotalAmount(createdOrder.getOrderId(), totalAmount);

        // Update quote status to CONVERTED
        quoteDAO.updateStatus(quoteId, "CONVERTED");

        return createdOrder;
    }

    /**
     * Get order by ID with items
     * @param orderId Order ID
     * @return Order with items
     */
    public Order getOrderWithItems(String orderId) throws Exception {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new Exception("Order not found");
        }
        return order;
    }

    /**
     * Get order items
     * @param orderId Order ID
     * @return List of order items
     */
    public List<OrderItem> getOrderItems(String orderId) throws Exception {
        return orderItemDAO.findByOrderId(orderId);
    }

    /**
     * Get all orders (ADMIN)
     * @return List of all orders
     */
    public List<Order> getAllOrders() throws Exception {
        return orderDAO.findAll();
    }

    /**
     * Get orders by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of orders for the client
     */
    public List<Order> getOrdersByClient(String clientId) throws Exception {
        return orderDAO.findByClientId(clientId);
    }

    /**
     * Get orders by status (ADMIN)
     * @param status Order status
     * @return List of orders with the status
     */
    public List<Order> getOrdersByStatus(String status) throws Exception {
        return orderDAO.findByStatus(status);
    }

    /**
     * Update order status (ADMIN)
     * @param orderId Order ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateOrderStatus(String orderId, String status) throws Exception {
        // Validate order exists
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new Exception("Order not found");
        }

        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new Exception("Invalid status transition from " + order.getStatus() + " to " + status);
        }

        return orderDAO.updateStatus(orderId, status);
    }

    /**
     * Validate status transition
     * Valid transitions:
     * PENDING -> PROCESSING
     * PROCESSING -> SHIPPED
     * SHIPPED -> DELIVERED
     * DELIVERED -> COMPLETED
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case "PENDING":
                return "PROCESSING".equals(newStatus);
            case "PROCESSING":
                return "SHIPPED".equals(newStatus);
            case "SHIPPED":
                return "DELIVERED".equals(newStatus);
            case "DELIVERED":
                return "COMPLETED".equals(newStatus);
            case "COMPLETED":
                return false; // No transitions from COMPLETED
            default:
                return false;
        }
    }
}
