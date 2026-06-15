package com.medsupply.services;

import com.medsupply.dao.SupplierBidDAO;
import com.medsupply.dao.TenderDAO;
import com.medsupply.dao.TenderItemDAO;
import com.medsupply.models.SupplierBid;
import com.medsupply.models.Tender;
import com.medsupply.models.TenderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * TenderService - Business logic for B2B reverse auction tender management
 * Handles tender creation, bidding rules, deadline management, and winner selection
 * 
 * Business Rules:
 * - Only CLIENT (hospital/lab) can create tenders
 * - Only SUPPLIER can submit bids
 * - Each bid must be LOWER than current best bid
 * - One supplier cannot spam bids higher than previous bid
 * - Tender expires at deadline
 * - Lowest bid wins
 */
public class TenderService {

    private TenderDAO tenderDAO;
    private TenderItemDAO tenderItemDAO;
    private SupplierBidDAO supplierBidDAO;

    public TenderService() {
        this.tenderDAO = new TenderDAO();
        this.tenderItemDAO = new TenderItemDAO();
        this.supplierBidDAO = new SupplierBidDAO();
    }

    // ==================== Tender Operations ====================

    /**
     * Create tender (CLIENT only)
     * @param clientId Client ID
     * @param title Tender title
     * @param description Tender description
     * @param budgetMax Maximum budget
     * @param deadline Bid submission deadline
     * @param items List of tender items
     * @return Created tender
     */
    public Tender createTender(String clientId, String title, String description, 
                               BigDecimal budgetMax, LocalDateTime deadline, 
                               List<TenderItem> items) throws Exception {
        // Validate deadline is in the future
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new Exception("Deadline must be in the future");
        }

        // Validate budget is positive
        if (budgetMax.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Budget must be greater than zero");
        }

        // Create tender
        Tender tender = new Tender(clientId, title, description, budgetMax, deadline);
        Tender createdTender = tenderDAO.create(tender);

        // Create tender items
        if (items != null && !items.isEmpty()) {
            for (TenderItem item : items) {
                item.setTenderId(createdTender.getTenderId());
            }
            tenderItemDAO.createBatch(items);
        }

        return createdTender;
    }

    /**
     * Get tender by ID
     * @param tenderId Tender ID
     * @return Tender
     */
    public Tender getTender(String tenderId) throws Exception {
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            throw new Exception("Tender not found");
        }
        return tender;
    }

    /**
     * Get tenders by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of tenders for the client
     */
    public List<Tender> getTendersByClient(String clientId) throws Exception {
        return tenderDAO.findByClientId(clientId);
    }

    /**
     * Get all tenders (ADMIN)
     * @return List of all tenders
     */
    public List<Tender> getAllTenders() throws Exception {
        return tenderDAO.findAll();
    }

    /**
     * Get open tenders (SUPPLIER)
     * @return List of open tenders
     */
    public List<Tender> getOpenTenders() throws Exception {
        return tenderDAO.findOpen();
    }

    /**
     * Get tender items
     * @param tenderId Tender ID
     * @return List of tender items
     */
    public List<TenderItem> getTenderItems(String tenderId) throws Exception {
        return tenderItemDAO.findByTenderId(tenderId);
    }

    /**
     * Close tender (CLIENT or ADMIN)
     * @param tenderId Tender ID
     * @return true if closure successful
     */
    public boolean closeTender(String tenderId) throws Exception {
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            throw new Exception("Tender not found");
        }

        // Can only close open tenders
        if (!"OPEN".equals(tender.getStatus())) {
            throw new Exception("Tender is not open");
        }

        return tenderDAO.updateStatus(tenderId, "CLOSED");
    }

    /**
     * Award tender to winner (CLIENT or ADMIN)
     * @param tenderId Tender ID
     * @return true if award successful
     */
    public boolean awardTender(String tenderId) throws Exception {
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            throw new Exception("Tender not found");
        }

        // Can only award closed tenders with bids
        if (!"CLOSED".equals(tender.getStatus())) {
            throw new Exception("Tender must be closed before awarding");
        }

        int bidCount = supplierBidDAO.countByTenderId(tenderId);
        if (bidCount == 0) {
            throw new Exception("Cannot award tender with no bids");
        }

        return tenderDAO.updateStatus(tenderId, "AWARDED");
    }

    // ==================== Bidding Operations ====================

    /**
     * Submit bid (SUPPLIER only)
     * @param tenderId Tender ID
     * @param supplierId Supplier ID
     * @param price Bid price
     * @return Created bid
     */
    public SupplierBid submitBid(String tenderId, String supplierId, BigDecimal price) throws Exception {
        // Validate tender exists and is open
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            throw new Exception("Tender not found");
        }

        if (!tender.isOpen()) {
            throw new Exception("Tender is not open for bidding");
        }

        // Validate price is positive
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Bid price must be greater than zero");
        }

        // Validate price is within budget
        if (price.compareTo(tender.getBudgetMax()) > 0) {
            throw new Exception("Bid price exceeds maximum budget");
        }

        // Get current best bid
        SupplierBid bestBid = supplierBidDAO.findBestBid(tenderId);

        // If there's a best bid, new bid must be lower
        if (bestBid != null) {
            if (price.compareTo(bestBid.getPrice()) >= 0) {
                throw new Exception("Bid must be lower than current best bid of " + bestBid.getPrice());
            }
        }

        // Get supplier's latest bid for this tender
        SupplierBid supplierLatestBid = supplierBidDAO.findLatestBid(tenderId, supplierId);

        // Supplier cannot bid higher than their previous bid
        if (supplierLatestBid != null) {
            if (price.compareTo(supplierLatestBid.getPrice()) >= 0) {
                throw new Exception("Bid must be lower than your previous bid of " + supplierLatestBid.getPrice());
            }
        }

        // Create bid
        SupplierBid bid = new SupplierBid(tenderId, supplierId, price);
        return supplierBidDAO.create(bid);
    }

    /**
     * Get bids for a tender
     * @param tenderId Tender ID
     * @return List of bids for the tender
     */
    public List<SupplierBid> getBidsByTender(String tenderId) throws Exception {
        return supplierBidDAO.findByTenderId(tenderId);
    }

    /**
     * Get best bid for a tender
     * @param tenderId Tender ID
     * @return Best bid (lowest price)
     */
    public SupplierBid getBestBid(String tenderId) throws Exception {
        return supplierBidDAO.findBestBid(tenderId);
    }

    /**
     * Get bids by supplier
     * @param supplierId Supplier ID
     * @return List of bids from the supplier
     */
    public List<SupplierBid> getBidsBySupplier(String supplierId) throws Exception {
        return supplierBidDAO.findBySupplierId(supplierId);
    }

    /**
     * Get bid count for a tender
     * @param tenderId Tender ID
     * @return Number of bids
     */
    public int getBidCount(String tenderId) throws Exception {
        return supplierBidDAO.countByTenderId(tenderId);
    }

    // ==================== Deadline Management ====================

    /**
     * Check and update expired tenders
     * This should be called periodically (e.g., via scheduled task)
     * @return Number of tenders updated
     */
    public int updateExpiredTenders() throws Exception {
        int updated = 0;
        List<Tender> openTenders = tenderDAO.findByStatus("OPEN");

        for (Tender tender : openTenders) {
            if (tender.isExpired()) {
                tenderDAO.updateStatus(tender.getTenderId(), "EXPIRED");
                updated++;
            }
        }

        return updated;
    }
}
