package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.models.Client;
import com.medsupply.models.Supplier;
import com.medsupply.models.User;
import com.medsupply.services.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * RegisterServlet - Handles user registration
 * POST /api/auth/register
 * Body: {"email": "...", "password": "...", "role": "CLIENT|SUPPLIER", "profile": {...}}
 */
@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private AuthService authService;
    private Gson gson;

    @Override
    public void init() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            // Read request body
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                requestBody.append(line);
            }

            JsonObject requestJson = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();
            String role = requestJson.get("role").getAsString();

            Object profileData = null;

            // Parse profile data based on role
            if ("CLIENT".equals(role)) {
                JsonObject profileJson = requestJson.getAsJsonObject("profile");
                Client client = new Client();
                client.setName(profileJson.get("name").getAsString());
                client.setType(profileJson.get("type").getAsString());
                client.setAddress(profileJson.get("address").getAsString());
                client.setPhone(profileJson.get("phone").getAsString());
                client.setEmail(profileJson.has("email") ? profileJson.get("email").getAsString() : email);
                if (profileJson.has("taxId")) {
                    client.setTaxId(profileJson.get("taxId").getAsString());
                }
                profileData = client;
            } else if ("SUPPLIER".equals(role)) {
                JsonObject profileJson = requestJson.getAsJsonObject("profile");
                Supplier supplier = new Supplier();
                supplier.setCompanyName(profileJson.get("companyName").getAsString());
                supplier.setContactPerson(profileJson.get("contactPerson").getAsString());
                supplier.setAddress(profileJson.get("address").getAsString());
                supplier.setPhone(profileJson.get("phone").getAsString());
                supplier.setEmail(profileJson.has("email") ? profileJson.get("email").getAsString() : email);
                if (profileJson.has("taxId")) {
                    supplier.setTaxId(profileJson.get("taxId").getAsString());
                }
                if (profileJson.has("licenseNumber")) {
                    supplier.setLicenseNumber(profileJson.get("licenseNumber").getAsString());
                }
                profileData = supplier;
            }

            // Register user
            User user = authService.register(email, password, role, profileData);

            response.addProperty("success", true);
            response.addProperty("message", "Registration successful");
            response.addProperty("userId", user.getUserId());
            response.addProperty("email", user.getEmail());
            response.addProperty("role", user.getRole());
            resp.setStatus(201);

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Registration failed: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
