package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medsupply.models.User;
import com.medsupply.services.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * LoginServlet - Handles user login
 * POST /api/auth/login
 * Body: {"email": "...", "password": "..."}
 */
@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

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

            JsonObject requestJson = gson.fromJson(requestBody.toString(), JsonObject.class);
            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();

            // Authenticate user
            User user = authService.login(email, password);

            if (user != null) {
                // Create session
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("role", user.getRole());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                response.addProperty("success", true);
                response.addProperty("message", "Login successful");
                response.addProperty("userId", user.getUserId());
                response.addProperty("email", user.getEmail());
                response.addProperty("role", user.getRole());
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid email or password");
                resp.setStatus(401);
            }

        } catch (Exception e) {
            // Handle email verification error
            if (e.getMessage() != null && e.getMessage().contains("verify your email")) {
                response.addProperty("success", false);
                response.addProperty("message", "Please verify your email before logging in");
                resp.setStatus(403);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Login failed: " + e.getMessage());
                resp.setStatus(500);
            }
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
