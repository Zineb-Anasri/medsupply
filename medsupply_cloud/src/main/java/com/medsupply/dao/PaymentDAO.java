package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Payment;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * PaymentDAO - Data Access Object for Payment table
 * Handles all database operations for payments
 */
public class PaymentDAO {

    /**
     * Find payment by ID
     * @param paymentId Payment ID (UUID)
     * @return Payment object if found, null otherwise
     */
    public Payment findById(String paymentId) throws Exception {
        String filters = "id=eq." + paymentId;
        String response = SupabaseClient.get("payments", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToPayment(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find payment by order ID
     * @param orderId Order ID
     * @return Payment object if found, null otherwise
     */
    public Payment findByOrderId(String orderId) throws Exception {
        String filters = "order_id=eq." + orderId;
        String response = SupabaseClient.get("payments", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToPayment(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find all payments
     * @return List of all payments
     */
    public List<Payment> findAll() throws Exception {
        String response = SupabaseClient.get("payments", "order=created_at.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            payments.add(mapJsonToPayment(jsonArray.get(i).getAsJsonObject()));
        }
        return payments;
    }

    /**
     * Find payments by client ID
     * @param clientId Client ID
     * @return List of payments for the client
     */
    public List<Payment> findByClientId(String clientId) throws Exception {
        // Payments don't have client_id in schema, need to join with orders
        String filters = "order_id=in.(select id from orders where client_id=eq." + clientId + ")&order=created_at.desc";
        String response = SupabaseClient.get("payments", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            payments.add(mapJsonToPayment(jsonArray.get(i).getAsJsonObject()));
        }
        return payments;
    }

    /**
     * Find payments by status
     * @param status Payment status
     * @return List of payments with the status
     */
    public List<Payment> findByStatus(String status) throws Exception {
        String filters = "status=eq." + status + "&order=created_at.desc";
        String response = SupabaseClient.get("payments", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            payments.add(mapJsonToPayment(jsonArray.get(i).getAsJsonObject()));
        }
        return payments;
    }

    /**
     * Find overdue payments
     * @return List of overdue payments
     */
    public List<Payment> findOverdue() throws Exception {
        String filters = "status=in.(PENDING,PARTIAL)&due_date=lt." + LocalDate.now().toString() + "&order=due_date.asc";
        String response = SupabaseClient.get("payments", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            payments.add(mapJsonToPayment(jsonArray.get(i).getAsJsonObject()));
        }
        return payments;
    }

    /**
     * Create a new payment
     * @param payment Payment object to create
     * @return Created payment
     */
    public Payment create(Payment payment) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", payment.getPaymentId());
        body.addProperty("order_id", payment.getOrderId());
        body.addProperty("amount", payment.getAmount());
        body.addProperty("paid_amount", payment.getPaidAmount());
        body.addProperty("status", payment.getStatus());
        body.addProperty("due_date", payment.getDueDate() != null ? payment.getDueDate().toString() : null);
        body.addProperty("payment_method", payment.getPaymentMethod());
        body.addProperty("transaction_reference", payment.getTransactionReference());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("payments", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToPayment(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update payment status
     * @param paymentId Payment ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateStatus(String paymentId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + paymentId;
        String response = SupabaseClient.patchWithFilters("payments", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Update paid amount
     * @param paymentId Payment ID
     * @param paidAmount New paid amount
     * @return true if update successful
     */
    public boolean updatePaidAmount(String paymentId, BigDecimal paidAmount) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("paid_amount", paidAmount);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + paymentId;
        String response = SupabaseClient.patchWithFilters("payments", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Update payment method and transaction reference
     * @param paymentId Payment ID
     * @param paymentMethod Payment method
     * @param transactionReference Transaction reference
     * @return true if update successful
     */
    public boolean updatePaymentDetails(String paymentId, String paymentMethod, String transactionReference) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("payment_method", paymentMethod);
        body.addProperty("transaction_reference", transactionReference);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + paymentId;
        String response = SupabaseClient.patchWithFilters("payments", filters, body.toString());
        return !response.isEmpty();
    }

    /**
     * Map JSON to Payment object
     */
    private Payment mapJsonToPayment(JsonObject json) {
        Payment payment = new Payment();
        if (json.has("id") && !json.get("id").isJsonNull())
            payment.setPaymentId(json.get("id").getAsString());
        if (json.has("order_id") && !json.get("order_id").isJsonNull())
            payment.setOrderId(json.get("order_id").getAsString());
        if (json.has("amount") && !json.get("amount").isJsonNull())
            payment.setAmount(new BigDecimal(json.get("amount").getAsString()));
        if (json.has("paid_amount") && !json.get("paid_amount").isJsonNull())
            payment.setPaidAmount(new BigDecimal(json.get("paid_amount").getAsString()));
        if (json.has("status") && !json.get("status").isJsonNull())
            payment.setStatus(json.get("status").getAsString());
        if (json.has("due_date") && !json.get("due_date").isJsonNull()) {
            try {
                payment.setDueDate(LocalDate.parse(json.get("due_date").getAsString()));
            } catch (Exception ignored) {}
        }
        if (json.has("payment_method") && !json.get("payment_method").isJsonNull())
            payment.setPaymentMethod(json.get("payment_method").getAsString());
        if (json.has("transaction_reference") && !json.get("transaction_reference").isJsonNull())
            payment.setTransactionReference(json.get("transaction_reference").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                payment.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                payment.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return payment;
    }
}
