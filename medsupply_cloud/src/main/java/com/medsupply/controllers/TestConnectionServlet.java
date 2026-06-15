package com.medsupply.controllers;

import com.medsupply.utils.SupabaseClient;
import com.google.gson.JsonArray;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/test")
public class TestConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String result = SupabaseClient.get("users", "limit=1");
            JsonArray arr = SupabaseClient.parseJsonArray(result);

            resp.setStatus(200);
            PrintWriter out = resp.getWriter();
            out.print("{\"status\":\"SUCCESS\",\"message\":\"Connexion Supabase etablie\",\"base_de_donnees\":\"MedSupply Cloud V1.0\"}");
            out.flush();

        } catch (Exception e) {
            resp.setStatus(500);
            PrintWriter out = resp.getWriter();
            out.print("{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
}