package com.medsupply.models;

import java.time.LocalDateTime;

/**
 * User model - Authentication core table
 */
public class User {
    private String userId;   // UUID stored as String
    private String email;
    private String password;
    private String role;
    private Boolean isActive;
    private Boolean emailVerified;
    private String verificationToken;
    private LocalDateTime tokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.emailVerified = false;
    }

    public String getUserId()                        { return userId; }
    public void setUserId(String userId)             { this.userId = userId; }
    public String getEmail()                         { return email; }
    public void setEmail(String email)               { this.email = email; }
    public String getPassword()                      { return password; }
    public void setPassword(String password)         { this.password = password; }
    public String getRole()                          { return role; }
    public void setRole(String role)                 { this.role = role; }
    public Boolean getIsActive()                     { return isActive; }
    public void setIsActive(Boolean isActive)        { this.isActive = isActive; }
    public Boolean getEmailVerified()                { return emailVerified; }
    public void setEmailVerified(Boolean v)          { this.emailVerified = v; }
    public String getVerificationToken()             { return verificationToken; }
    public void setVerificationToken(String t)       { this.verificationToken = t; }
    public LocalDateTime getTokenExpiry()            { return tokenExpiry; }
    public void setTokenExpiry(LocalDateTime t)      { this.tokenExpiry = t; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime t)        { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)        { this.updatedAt = t; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
}