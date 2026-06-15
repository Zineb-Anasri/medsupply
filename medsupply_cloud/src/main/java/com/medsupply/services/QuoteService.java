package com.medsupply.services;

import java.math.BigDecimal;
import java.util.List;
import com.medsupply.dao.ProductDAO;
import com.medsupply.dao.QuoteDAO;
import com.medsupply.dao.QuoteItemDAO;
import com.medsupply.models.Product;
import com.medsupply.models.Quote;
import com.medsupply.models.QuoteItem;

/**
 * QuoteService - Business logic for quote management
 * Handles quote creation, validation, pricing, and status transitions
 */
public class QuoteService {

    private QuoteDAO quoteDAO;
    private QuoteItemDAO quoteItemDAO;
    private ProductDAO productDAO;

    public QuoteService() {
        this.quoteDAO = new QuoteDAO();
        this.quoteItemDAO = new QuoteItemDAO();
        this.productDAO = new ProductDAO();
    }

    /**
     * Create a new quote with items
     * @param clientId Client ID
     * @param items List of quote items
     * @return Created quote
     */
    public Quote createQuote(String clientId, List<QuoteItem> items) throws Exception {
        // Validate quote has items
        if (items == null || items.isEmpty()) {
            throw new Exception("Quote must contain at least one item");
        }

        // Create quote with PENDING status
        Quote quote = new Quote(clientId, "PENDING");
        Quote createdQuote = quoteDAO.create(quote);

        // Add items to quote
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (QuoteItem item : items) {
            // Validate product exists
            Product product = productDAO.findById(item.getProductId());
            if (product == null) {
                throw new Exception("Product not found: " + item.getProductId());
            }

            // Validate quantity is positive
            if (item.getQuantity() <= 0) {
                throw new Exception("Quantity must be positive for product: " + item.getProductId());
            }

            // Set quote ID and proposed price (from product catalog).
            // Items arrive deserialized from client JSON (via Gson), which bypasses the
            // model constructor, so generate the primary key if it was not provided.
            item.setQuoteId(createdQuote.getQuoteId());
            if (item.getItemId() == null) {
                item.setItemId(java.util.UUID.randomUUID().toString());
            }
            item.setProposedPrice(product.getUnitPrice());

            // Calculate item total
            BigDecimal itemTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Create quote item
            quoteItemDAO.create(item);
        }

        // Update quote total amount (and reflect it on the returned object)
        quoteDAO.updateTotalAmount(createdQuote.getQuoteId(), totalAmount);
        createdQuote.setTotalAmount(totalAmount);

        return createdQuote;
    }

    /**
     * Get quote by ID with items
     * @param quoteId Quote ID
     * @return Quote with items
     */
    public Quote getQuoteWithItems(String quoteId) throws Exception {
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }
        return quote;
    }

    /**
     * Get quote items
     * @param quoteId Quote ID
     * @return List of quote items
     */
    public List<QuoteItem> getQuoteItems(String quoteId) throws Exception {
        return quoteItemDAO.findByQuoteId(quoteId);
    }

    /**
     * Get all quotes (ADMIN)
     * @return List of all quotes
     */
    public List<Quote> getAllQuotes() throws Exception {
        return quoteDAO.findAll();
    }

    /**
     * Get quotes by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of quotes for the client
     */
    public List<Quote> getQuotesByClient(String clientId) throws Exception {
        return quoteDAO.findByClientId(clientId);
    }

    /**
     * Get quotes by status (ADMIN)
     * @param status Quote status
     * @return List of quotes with the status
     */
    public List<Quote> getQuotesByStatus(String status) throws Exception {
        return quoteDAO.findByStatus(status);
    }

    /**
     * Review quote and set admin pricing (ADMIN)
     * @param quoteId Quote ID
     * @param itemPrices Map of item ID to admin price
     * @return true if review successful
     */
    public boolean reviewQuote(String quoteId, java.util.Map<String, BigDecimal> itemPrices) throws Exception {
        // Validate quote exists and is in PENDING status
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }
        if (!"PENDING".equals(quote.getStatus())) {
            throw new Exception("Quote can only be reviewed in PENDING status");
        }

        // Update item prices
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (java.util.Map.Entry<String, BigDecimal> entry : itemPrices.entrySet()) {
            String itemId = entry.getKey();
            BigDecimal adminPrice = entry.getValue();

            // Validate price is positive
            if (adminPrice == null || adminPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("Admin price must be positive");
            }

            // Update item price
            quoteItemDAO.updateAdminPrice(itemId, adminPrice);

            // Get item to calculate total
            QuoteItem item = quoteItemDAO.findById(itemId);
            if (item != null) {
                BigDecimal itemTotal = adminPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        // Update quote total and status
        quoteDAO.updateTotalAmount(quoteId, totalAmount);
        quoteDAO.updateStatus(quoteId, "REVIEWED");

        return true;
    }

    /**
     * Accept quote (CLIENT)
     * @param quoteId Quote ID
     * @return true if acceptance successful
     */
    public boolean acceptQuote(String quoteId) throws Exception {
        // Validate quote exists and is in REVIEWED status
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }
        if (!"REVIEWED".equals(quote.getStatus())) {
            throw new Exception("Quote can only be accepted in REVIEWED status");
        }

        // Update status to APPROVED
        return quoteDAO.updateStatus(quoteId, "APPROVED");
    }

    /**
     * Reject quote (CLIENT)
     * @param quoteId Quote ID
     * @return true if rejection successful
     */
    public boolean rejectQuote(String quoteId) throws Exception {
        // Validate quote exists
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }

        // Can only reject PENDING or REVIEWED quotes
        if (!"PENDING".equals(quote.getStatus()) && !"REVIEWED".equals(quote.getStatus())) {
            throw new Exception("Quote can only be rejected in PENDING or REVIEWED status");
        }

        // Update status to REJECTED
        return quoteDAO.updateStatus(quoteId, "REJECTED");
    }

    /**
     * Convert approved quote to order (future implementation)
     * @param quoteId Quote ID
     * @return true if conversion successful
     */
    public boolean convertToOrder(String quoteId) throws Exception {
        // Validate quote exists and is in APPROVED status
        Quote quote = quoteDAO.findById(quoteId);
        if (quote == null) {
            throw new Exception("Quote not found");
        }
        if (!"APPROVED".equals(quote.getStatus())) {
            throw new Exception("Quote can only be converted in APPROVED status");
        }

        // Update status to CONVERTED
        return quoteDAO.updateStatus(quoteId, "CONVERTED");
    }
}
