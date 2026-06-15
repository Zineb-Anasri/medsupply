package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet - Handles user logout
 * POST /api/auth/logout
 */
@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() {
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();

        try {
            // Invalidate session
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            response.addProperty("success", true);
            response.addProperty("message", "Logout successful");
            resp.setStatus(200);

        } catch (Exception e) {
            response.addProperty("success", false);
            response.addProperty("message", "Logout failed: " + e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().print(gson.toJson(response));
    }
}
