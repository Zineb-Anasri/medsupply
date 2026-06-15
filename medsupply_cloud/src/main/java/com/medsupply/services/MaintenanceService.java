package com.medsupply.services;

import java.time.LocalDate;
import java.util.List;
import com.medsupply.dao.InterventionDAO;
import com.medsupply.dao.MaintenanceContractDAO;
import com.medsupply.dao.MaintenanceRequestDAO;
import com.medsupply.dao.ProductDAO;
import com.medsupply.models.Intervention;
import com.medsupply.models.MaintenanceContract;
import com.medsupply.models.MaintenanceRequest;

/**
 * MaintenanceService - Business logic for maintenance management
 * Handles contract validation, request creation, intervention assignment, and status transitions
 * 
 * Business Rules:
 * - Only active maintenance contracts can receive maintenance requests
 * - Warranty validation for contract eligibility
 * - Request status transitions: OPEN → ASSIGNED → IN_PROGRESS → COMPLETED → CLOSED
 * - All interventions must be recorded for history
 */
public class MaintenanceService {

    private MaintenanceContractDAO contractDAO;
    private MaintenanceRequestDAO requestDAO;
    private InterventionDAO interventionDAO;
    private ProductDAO productDAO;

    public MaintenanceService() {
        this.contractDAO = new MaintenanceContractDAO();
        this.requestDAO = new MaintenanceRequestDAO();
        this.interventionDAO = new InterventionDAO();
        this.productDAO = new ProductDAO();
    }

    // ==================== Maintenance Contract Operations ====================

    /**
     * Create maintenance contract (ADMIN)
     * @param clientId Client ID
     * @param productId Product ID
     * @param warrantyDuration Warranty duration in months
     * @param startDate Start date
     * @return Created contract
     */
    public MaintenanceContract createContract(String clientId, String productId, 
                                             Integer warrantyDuration, LocalDate startDate) throws Exception {
        // Validate product exists
        if (productDAO.findById(productId) == null) {
            throw new Exception("Product not found");
        }

        // Calculate end date based on warranty duration
        LocalDate endDate = startDate.plusMonths(warrantyDuration);

        // Create contract with ACTIVE status
        MaintenanceContract contract = new MaintenanceContract(clientId, productId, warrantyDuration, 
                                                               startDate, endDate, "ACTIVE");
        
        // Generate contract reference
        String contractRef = "MTC-" + System.currentTimeMillis();
        contract.setContractReference(contractRef);
        
        return contractDAO.create(contract);
    }

    /**
     * Get contract by ID
     * @param contractId Contract ID
     * @return Contract
     */
    public MaintenanceContract getContract(String contractId) throws Exception {
        MaintenanceContract contract = contractDAO.findById(contractId);
        if (contract == null) {
            throw new Exception("Contract not found");
        }
        return contract;
    }

    /**
     * Get contracts by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of contracts for the client
     */
    public List<MaintenanceContract> getContractsByClient(String clientId) throws Exception {
        return contractDAO.findByClientId(clientId);
    }

    /**
     * Get all contracts (ADMIN)
     * @return List of all contracts
     */
    public List<MaintenanceContract> getAllContracts() throws Exception {
        return contractDAO.findAll();
    }

    /**
     * Get active contracts (ADMIN)
     * @return List of active contracts
     */
    public List<MaintenanceContract> getActiveContracts() throws Exception {
        return contractDAO.findActive();
    }

    /**
     * Update contract status (ADMIN)
     * @param contractId Contract ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateContractStatus(String contractId, String status) throws Exception {
        // Validate contract exists
        MaintenanceContract contract = contractDAO.findById(contractId);
        if (contract == null) {
            throw new Exception("Contract not found");
        }

        return contractDAO.updateStatus(contractId, status);
    }

    /**
     * Check if contract is eligible for maintenance requests
     * @param contractId Contract ID
     * @return true if eligible
     */
    public boolean isContractEligible(String contractId) throws Exception {
        MaintenanceContract contract = contractDAO.findById(contractId);
        if (contract == null) {
            return false;
        }
        return contract.isActive();
    }

    // ==================== Maintenance Request Operations ====================

    /**
     * Create maintenance request (CLIENT)
     * @param contractId Contract ID
     * @param clientId Client ID
     * @param productId Product ID
     * @param description Description
     * @param priority Priority (LOW, MEDIUM, HIGH, URGENT)
     * @return Created request
     */
    public MaintenanceRequest createRequest(String contractId, String clientId, String productId, 
                                          String description, String priority) throws Exception {
        // Validate contract is active and eligible
        if (!isContractEligible(contractId)) {
            throw new Exception("Contract is not active or expired. Cannot create maintenance request.");
        }

        // Validate priority
        if (!isValidPriority(priority)) {
            throw new Exception("Invalid priority. Valid values: LOW, MEDIUM, HIGH, URGENT");
        }

        // Create request with OPEN status
        MaintenanceRequest request = new MaintenanceRequest(contractId, clientId, productId, 
                                                           description, priority);
        
        return requestDAO.create(request);
    }

    /**
     * Get request by ID
     * @param requestId Request ID
     * @return Request
     */
    public MaintenanceRequest getRequest(String requestId) throws Exception {
        MaintenanceRequest request = requestDAO.findById(requestId);
        if (request == null) {
            throw new Exception("Request not found");
        }
        return request;
    }

    /**
     * Get requests by client ID (CLIENT)
     * @param clientId Client ID
     * @return List of requests for the client
     */
    public List<MaintenanceRequest> getRequestsByClient(String clientId) throws Exception {
        return requestDAO.findByClientId(clientId);
    }

    /**
     * Get all requests (ADMIN)
     * @return List of all requests
     */
    public List<MaintenanceRequest> getAllRequests() throws Exception {
        return requestDAO.findAll();
    }

    /**
     * Get requests by status (ADMIN)
     * @param status Request status
     * @return List of requests with the status
     */
    public List<MaintenanceRequest> getRequestsByStatus(String status) throws Exception {
        return requestDAO.findByStatus(status);
    }

    /**
     * Get requests by priority (ADMIN)
     * @param priority Request priority
     * @return List of requests with the priority
     */
    public List<MaintenanceRequest> getRequestsByPriority(String priority) throws Exception {
        return requestDAO.findByPriority(priority);
    }

    /**
     * Update request status (ADMIN)
     * @param requestId Request ID
     * @param status New status
     * @return true if update successful
     */
    public boolean updateRequestStatus(String requestId, String status) throws Exception {
        // Validate request exists
        MaintenanceRequest request = requestDAO.findById(requestId);
        if (request == null) {
            throw new Exception("Request not found");
        }

        // Validate status transition
        if (!isValidRequestStatusTransition(request.getStatus(), status)) {
            throw new Exception("Invalid status transition from " + request.getStatus() + " to " + status);
        }

        return requestDAO.updateStatus(requestId, status);
    }

    /**
     * Update request priority (ADMIN)
     * @param requestId Request ID
     * @param priority New priority
     * @return true if update successful
     */
    public boolean updateRequestPriority(String requestId, String priority) throws Exception {
        // Validate priority
        if (!isValidPriority(priority)) {
            throw new Exception("Invalid priority. Valid values: LOW, MEDIUM, HIGH, URGENT");
        }

        return requestDAO.updatePriority(requestId, priority);
    }

    /**
     * Close maintenance request (ADMIN)
     * @param requestId Request ID
     * @return true if closure successful
     */
    public boolean closeRequest(String requestId) throws Exception {
        // Validate request exists
        MaintenanceRequest request = requestDAO.findById(requestId);
        if (request == null) {
            throw new Exception("Request not found");
        }

        // Can only close COMPLETED requests
        if (!"COMPLETED".equals(request.getStatus())) {
            throw new Exception("Request can only be closed in COMPLETED status");
        }

        return requestDAO.updateStatus(requestId, "CLOSED");
    }

    // ==================== Intervention Operations ====================

    /**
     * Create intervention for request (ADMIN)
     * @param requestId Request ID
     * @param technicianName Technician name
     * @param diagnosis Diagnosis
     * @return Created intervention
     */
    public Intervention createIntervention(String requestId, String technicianName, 
                                          String diagnosis) throws Exception {
        // Validate request exists
        MaintenanceRequest request = requestDAO.findById(requestId);
        if (request == null) {
            throw new Exception("Request not found");
        }

        // Update request status to ASSIGNED if it's OPEN
        if ("OPEN".equals(request.getStatus())) {
            requestDAO.updateStatus(requestId, "ASSIGNED");
        }

        // Create intervention
        Intervention intervention = new Intervention(requestId, technicianName, diagnosis);
        return interventionDAO.create(intervention);
    }

    /**
     * Get intervention by ID
     * @param interventionId Intervention ID
     * @return Intervention
     */
    public Intervention getIntervention(String interventionId) throws Exception {
        Intervention intervention = interventionDAO.findById(interventionId);
        if (intervention == null) {
            throw new Exception("Intervention not found");
        }
        return intervention;
    }

    /**
     * Get interventions by request ID
     * @param requestId Request ID
     * @return List of interventions for the request
     */
    public List<Intervention> getInterventionsByRequest(String requestId) throws Exception {
        return interventionDAO.findByRequestId(requestId);
    }

    /**
     * Get all interventions (ADMIN)
     * @return List of all interventions
     */
    public List<Intervention> getAllInterventions() throws Exception {
        return interventionDAO.findAll();
    }

    /**
     * Update intervention details (ADMIN)
     * @param interventionId Intervention ID
     * @param actionsPerformed Actions performed
     * @param replacedParts Replaced parts
     * @param interventionCost Intervention cost
     * @param notes Notes
     * @return true if update successful
     */
    public boolean updateInterventionDetails(String interventionId, String actionsPerformed, 
                                             String replacedParts, java.math.BigDecimal interventionCost, 
                                             String notes) throws Exception {
        // Validate intervention exists
        Intervention intervention = interventionDAO.findById(interventionId);
        if (intervention == null) {
            throw new Exception("Intervention not found");
        }

        return interventionDAO.updateDetails(interventionId, actionsPerformed, replacedParts, 
                                            interventionCost, notes);
    }

    /**
     * Complete intervention (ADMIN)
     * @param interventionId Intervention ID
     * @param actionsPerformed Actions performed
     * @param replacedParts Replaced parts
     * @param interventionCost Intervention cost
     * @param notes Notes
     * @return true if completion successful
     */
    public boolean completeIntervention(String interventionId, String actionsPerformed, 
                                       String replacedParts, java.math.BigDecimal interventionCost, 
                                       String notes) throws Exception {
        // Validate intervention exists
        Intervention intervention = interventionDAO.findById(interventionId);
        if (intervention == null) {
            throw new Exception("Intervention not found");
        }

        // Update intervention details and mark as completed
        boolean updated = interventionDAO.updateDetails(interventionId, actionsPerformed, replacedParts, 
                                                       interventionCost, notes);
        
        if (updated) {
            interventionDAO.updateCompletionStatus(interventionId, "COMPLETED");
            
            // Update request status to COMPLETED
            requestDAO.updateStatus(intervention.getRequestId(), "COMPLETED");
        }

        return updated;
    }

    // ==================== Validation Helpers ====================

    /**
     * Validate priority value
     */
    private boolean isValidPriority(String priority) {
        return "LOW".equals(priority) || "MEDIUM".equals(priority) || 
               "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    /**
     * Validate request status transition
     * Valid transitions:
     * OPEN → ASSIGNED
     * ASSIGNED → IN_PROGRESS
     * IN_PROGRESS → COMPLETED
     * COMPLETED → CLOSED
     */
    private boolean isValidRequestStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case "OPEN":
                return "ASSIGNED".equals(newStatus);
            case "ASSIGNED":
                return "IN_PROGRESS".equals(newStatus);
            case "IN_PROGRESS":
                return "COMPLETED".equals(newStatus);
            case "COMPLETED":
                return "CLOSED".equals(newStatus);
            case "CLOSED":
                return false; // No transitions from CLOSED
            default:
                return false;
        }
    }
}
