package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.medsupply.services.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * VerifyEmailServlet - Handles email verification
 * GET /api/auth/verify?token=XXXX
 */
@WebServlet("/api/auth/verify")
public class VerifyEmailServlet extends HttpServlet {

    private AuthService authService;
    private Gson gson;

    @Override
    public void init() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            String token = req.getParameter("token");

            if (token == null || token.trim().isEmpty()) {
                response.addProperty("success", false);
                response.addProperty("message", "Verification token is required");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(response));
                return;
            }

            // Verify email
            boolean verified = authService.verifyEmail(token);

            if (verified) {
                response.addProperty("success", true);
                response.addProperty("message", "Email verified successfully. You can now login.");
                resp.setStatus(200);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Failed to verify email");
                resp.setStatus(400);
            }

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", e.getMessage());
            resp.setStatus(400);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
