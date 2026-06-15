package com.medsupply.services;

import com.medsupply.dao.ClientDAO;
import com.medsupply.dao.SupplierDAO;
import com.medsupply.dao.UserDAO;
import com.medsupply.models.Client;
import com.medsupply.models.Supplier;
import com.medsupply.models.User;
import com.medsupply.utils.EmailService;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthService - Business logic for authentication
 * Handles login, registration, and user profile management
 */
public class AuthService {

    private UserDAO userDAO;
    private ClientDAO clientDAO;
    private SupplierDAO supplierDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.clientDAO = new ClientDAO();
        this.supplierDAO = new SupplierDAO();
    }

    /**
     * Authenticate user with email and password
     * @param email User email
     * @param password User password
     * @return User object if authentication successful, null otherwise
     */
    public User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        User user = userDAO.authenticate(email, password);
        
        // Check if email is verified
        if (user != null && !user.getEmailVerified()) {
            throw new Exception("Please verify your email before logging in");
        }
        
        return user;
    }

    /**
     * Register a new user with profile
     * @param email User email
     * @param password User password
     * @param role User role (ADMIN, CLIENT, SUPPLIER)
     * @param profileData Profile data (Client or Supplier object)
     * @return Created user object
     */
    public User register(String email, String password, String role, Object profileData) throws Exception {
        // Check if email already exists
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            throw new Exception("Email already registered");
        }

        // Create user with email_verified = false
        User user = new User(email, password, role);
        user.setEmailVerified(false);
        User createdUser = userDAO.create(user);

        // Generate verification token and send email
        String verificationToken = generateVerificationToken();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);
        
        userDAO.setVerificationToken(createdUser.getUserId(), verificationToken, tokenExpiry);
        
        // Send verification email
        if (EmailService.isConfigured()) {
            EmailService.sendVerificationEmail(email, verificationToken);
        } else {
            System.out.println("Email service not configured. Verification token: " + verificationToken);
        }

        // Create profile based on role
        if ("CLIENT".equals(role) && profileData instanceof Client) {
            Client client = (Client) profileData;
            client.setUserId(createdUser.getUserId());
            clientDAO.create(client);
        } else if ("SUPPLIER".equals(role) && profileData instanceof Supplier) {
            Supplier supplier = (Supplier) profileData;
            supplier.setUserId(createdUser.getUserId());
            supplierDAO.create(supplier);
        }

        return createdUser;
    }

    /**
     * Get user profile based on role
     * @param userId User ID
     * @param role User role
     * @return Profile object (Client or Supplier) or null
     */
    public Object getUserProfile(String userId, String role) throws Exception {
        if ("CLIENT".equals(role)) {
            return clientDAO.findByUserId(userId);
        } else if ("SUPPLIER".equals(role)) {
            return supplierDAO.findByUserId(userId);
        }
        return null;
    }

    /**
     * Check if user has specific role
     * @param user User object
     * @param requiredRole Required role
     * @return true if user has required role
     */
    public boolean hasRole(User user, String requiredRole) {
        return user != null && requiredRole != null && requiredRole.equals(user.getRole());
    }

    /**
     * Check if user is admin
     * @param user User object
     * @return true if user is admin
     */
    public boolean isAdmin(User user) {
        return hasRole(user, "ADMIN");
    }

    /**
     * Check if user is client
     * @param user User object
     * @return true if user is client
     */
    public boolean isClient(User user) {
        return hasRole(user, "CLIENT");
    }

    /**
     * Check if user is supplier
     * @param user User object
     * @return true if user is supplier
     */
    public boolean isSupplier(User user) {
        return hasRole(user, "SUPPLIER");
    }

    /**
     * Update user password
     * @param userId User ID
     * @param oldPassword Old password for verification
     * @param newPassword New password
     * @return true if password update successful
     */
    public boolean updatePassword(String userId, String oldPassword, String newPassword) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null || user.getPassword() == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            return false;
        }
        return userDAO.updatePassword(userId, newPassword);
    }

    /**
     * Deactivate user account
     * @param userId User ID
     * @return true if deactivation successful
     */
    public boolean deactivateAccount(String userId) throws Exception {
        return userDAO.deactivate(userId);
    }

    /**
     * Verify email using token
     * @param token Verification token
     * @return true if verification successful
     */
    public boolean verifyEmail(String token) throws Exception {
        User user = userDAO.findByVerificationToken(token);
        
        if (user == null) {
            throw new Exception("Invalid verification token");
        }
        
        // Check if token is expired
        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new Exception("Verification token has expired");
        }
        
        // Verify email
        return userDAO.verifyEmail(user.getUserId());
    }

    /**
     * Generate unique verification token
     * @return UUID-based token
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
