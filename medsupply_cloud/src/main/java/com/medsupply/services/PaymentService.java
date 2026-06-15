package com.medsupply.services;

import com.medsupply.dao.ClientDAO;
import com.medsupply.dao.OrderDAO;
import com.medsupply.dao.PaymentDAO;
import com.medsupply.models.Client;
import com.medsupply.models.Order;
import com.medsupply.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
/**
 * PaymentService - Business logic for payment management
 * Handles payment creation, credit rules based on client type, and payment processing
 * 
 * Business Rules:
 * - HOSPITAL and LABORATORY clients: up to 60 days credit, partial payments allowed
 * - Other client types: immediate payment required
 */
public class PaymentService {

    private PaymentDAO paymentDAO;
    private OrderDAO orderDAO;
    private ClientDAO clientDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.orderDAO = new OrderDAO();
        this.clientDAO = new ClientDAO();
    }

    /**
     * Create payment for an order based on client credit rules
     * @param orderId Order ID
     * @return Created payment
     */
    public Payment createPaymentForOrder(String orderId) throws Exception {
        // Validate order exists
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new Exception("Order not found");
        }

        // Check if payment already exists for this order
        Payment existingPayment = paymentDAO.findByOrderId(orderId);
        if (existingPayment != null) {
            throw new Exception("Payment already exists for this order");
        }

        // Get client to determine credit rules
        Client client = clientDAO.findByUserId(order.getClientId());
        if (client == null) {
            throw new Exception("Client not found");
        }

        // Determine payment status and due date based on client type
        String clientType = client.getType();
        String status;
        LocalDate dueDate;

        if ("HOSPITAL".equals(clientType) || "LABORATORY".equals(clientType)) {
            // Credit clients: 60 days credit, partial payments allowed
            status = "PENDING";
            dueDate = LocalDate.now().plusDays(60);
        } else {
            // Immediate payment clients: payment required immediately
            status = "PENDING";
            dueDate = LocalDate.now(); // Due today
        }

        // Create payment
        Payment payment = new Payment(orderId, order.getClientId(), order.getTotalAmount(), 
                                     status, dueDate, null);
        Payment createdPayment = paymentDAO.create(payment);

        return createdPayment;
    }

    /**
     * Process payment (full or partial)
     * @param paymentId Payment ID
     * @param amount Amount to pay
     * @param paymentMethod Payment method
     * @param transactionReference Transaction reference
     * @return Updated payment
     */
    public Payment processPayment(String paymentId, BigDecimal amount, String paymentMethod, 
                                  String transactionReference) throws Exception {
        // Validate payment exists
        Payment payment = paymentDAO.findById(paymentId);
        if (payment == null) {
            throw new Exception("Payment not found");
        }

        // Validate payment status
        if ("PAID".equals(payment.getStatus()) || "CANCELLED".equals(payment.getStatus())) {
            throw new Exception("Payment cannot be processed in current status");
        }

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Payment amount must be positive");
        }

        // Check if client has credit (HOSPITAL or LABORATORY)
        Client client = clientDAO.findByUserId(payment.getClientId());
        if (client == null) {
            throw new Exception("Client not found");
        }

        boolean hasCredit = "HOSPITAL".equals(client.getType()) || "LABORATORY".equals(client.getType());

        // If no credit, only full payment is allowed
        if (!hasCredit && amount.compareTo(payment.getAmount()) < 0) {
            throw new Exception("Partial payments not allowed for this client type");
        }

        // Calculate new paid amount
        BigDecimal newPaidAmount = payment.getPaidAmount().add(amount);

        // Validate payment doesn't exceed total
        if (newPaidAmount.compareTo(payment.getAmount()) > 0) {
            throw new Exception("Payment amount exceeds total due");
        }

        // Update paid amount
        paymentDAO.updatePaidAmount(paymentId, newPaidAmount);

        // Update payment method and transaction reference
        paymentDAO.updatePaymentDetails(paymentId, paymentMethod, transactionReference);

        // Update status based on payment completion
        String newStatus;
        if (newPaidAmount.compareTo(payment.getAmount()) >= 0) {
            newStatus = "PAID";
        } else {
            newStatus = "PARTIAL";
        }
        paymentDAO.updateStatus(paymentId, newStatus);

        // Return updated payment
        return paymentDAO.findById(paymentId);
    }

    /**
     * Get payment by ID
     * @param paymentId Payment ID
     * @return Payment
     */
    public Payment getPayment(String paymentId) throws Exception {
        Payment payment = paymentDAO.findById(paymentId);
        if (payment == null) {
            throw new Exception("Payment not found");
        }
        return payment;
    }

    /**
     * Get payment by order ID
     * @param orderId Order ID
     * @return Payment
     */
    public Payment getPaymentByOrder(String orderId) throws Exception {
        Payment payment = paymentDAO.findByOrderId(orderId);
        if (payment == null) {
            throw new Exception("Payment not found for this order");
        }
        return payment;
    }

    /**
     * Get all payments (ADMIN)
     * @return List of all payments
     */
    public List<Payment> getAllPayments() throws Exception {
        return paymentDAO.findAll();
    }

    /**
     * Get payments by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of payments for the client
     */
    public List<Payment> getPaymentsByClient(String clientId) throws Exception {
        return paymentDAO.findByClientId(clientId);
    }

    /**
     * Get payments by status (ADMIN)
     * @param status Payment status
     * @return List of payments with the status
     */
    public List<Payment> getPaymentsByStatus(String status) throws Exception {
        return paymentDAO.findByStatus(status);
    }

    /**
     * Get overdue payments (ADMIN)
     * @return List of overdue payments
     */
    public List<Payment> getOverduePayments() throws Exception {
        return paymentDAO.findOverdue();
    }

    /**
     * Mark overdue payments
     * @return Number of payments marked as overdue
     */
    public int markOverduePayments() throws Exception {
        List<Payment> overduePayments = paymentDAO.findOverdue();
        int count = 0;
        
        for (Payment payment : overduePayments) {
            if ("PENDING".equals(payment.getStatus()) || "PARTIAL".equals(payment.getStatus())) {
                paymentDAO.updateStatus(payment.getPaymentId(), "OVERDUE");
                count++;
            }
        }
        
        return count;
    }

    /**
     * Cancel payment (ADMIN)
     * @param paymentId Payment ID
     * @return true if cancellation successful
     */
    public boolean cancelPayment(String paymentId) throws Exception {
        Payment payment = paymentDAO.findById(paymentId);
        if (payment == null) {
            throw new Exception("Payment not found");
        }

        // Can only cancel PENDING or PARTIAL payments
        if (!"PENDING".equals(payment.getStatus()) && !"PARTIAL".equals(payment.getStatus())) {
            throw new Exception("Payment can only be cancelled in PENDING or PARTIAL status");
        }

        return paymentDAO.updateStatus(paymentId, "CANCELLED");
    }

    /**
     * Check if client has credit eligibility
     * @param clientId Client ID
     * @return true if client has credit (HOSPITAL or LABORATORY)
     */
    public boolean hasCreditEligibility(String clientId) throws Exception {
        Client client = clientDAO.findByUserId(clientId);
        if (client == null) {
            return false;
        }
        return "HOSPITAL".equals(client.getType()) || "LABORATORY".equals(client.getType());
    }
}
