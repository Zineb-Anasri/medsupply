package com.medsupply.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medsupply.dao.ClientDAO;
import com.medsupply.dao.SupplierDAO;
import com.medsupply.models.Client;
import com.medsupply.models.Supplier;
import com.medsupply.models.User;
import com.medsupply.services.AuthService;
import com.medsupply.utils.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * ProfileServlet - the signed-in user's own account + profile.
 *
 * - GET  /api/profile           -> { account:{userId,email,role}, profile:{client|supplier|null} }
 * - PUT  /api/profile           -> update own CLIENT/SUPPLIER profile fields
 * - PUT  /api/profile/password  -> { oldPassword, newPassword } change password
 *
 * Requires an authenticated session (AuthFilter). A user may only read/update their own profile.
 */
@WebServlet("/api/profile/*")
public class ProfileServlet extends HttpServlet {

    private AuthService authService;
    private ClientDAO clientDAO;
    private SupplierDAO supplierDAO;
    private final Gson gson = JsonUtil.GSON;

    @Override
    public void init() {
        this.authService = new AuthService();
        this.clientDAO = new ClientDAO();
        this.supplierDAO = new SupplierDAO();
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader r = req.getReader();
        while ((line = r.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject out = new JsonObject();
        try {
            HttpSession session = req.getSession(false);
            String userId = (String) session.getAttribute("userId");
            String role = (String) session.getAttribute("role");
            User user = (User) session.getAttribute("user");

            JsonObject account = new JsonObject();
            account.addProperty("userId", userId);
            account.addProperty("email", user != null ? user.getEmail() : null);
            account.addProperty("role", role);
            out.add("account", account);

            Object profile = null;
            if ("CLIENT".equals(role)) {
                profile = clientDAO.findByUserId(userId);
            } else if ("SUPPLIER".equals(role)) {
                profile = supplierDAO.findByUserId(userId);
            }
            out.add("profile", profile == null ? null : gson.toJsonTree(profile));

            out.addProperty("success", true);
            resp.setStatus(200);
        } catch (Exception e) {
            out.addProperty("success", false);
            out.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }
        resp.getWriter().print(gson.toJson(out));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject out = new JsonObject();
        try {
            HttpSession session = req.getSession(false);
            String userId = (String) session.getAttribute("userId");
            String role = (String) session.getAttribute("role");
            String pathInfo = req.getPathInfo();
            JsonObject body = JsonParser.parseString(readBody(req)).getAsJsonObject();

            // PUT /api/profile/password
            if (pathInfo != null && pathInfo.equals("/password")) {
                String oldPwd = str(body, "oldPassword");
                String newPwd = str(body, "newPassword");
                if (oldPwd == null || newPwd == null || newPwd.length() < 6) {
                    out.addProperty("success", false);
                    out.addProperty("message", "Mot de passe actuel requis et nouveau mot de passe (min. 6 caractères)");
                    resp.setStatus(400);
                    resp.getWriter().print(gson.toJson(out));
                    return;
                }
                boolean ok = authService.updatePassword(userId, oldPwd, newPwd);
                out.addProperty("success", ok);
                out.addProperty("message", ok ? "Mot de passe mis à jour" : "Mot de passe actuel incorrect");
                resp.setStatus(ok ? 200 : 400);
                resp.getWriter().print(gson.toJson(out));
                return;
            }

            // PUT /api/profile  -> update profile fields
            boolean updated = false;
            if ("CLIENT".equals(role)) {
                Client c = clientDAO.findByUserId(userId);
                if (c == null) { notFound(resp, out, "Profil client introuvable"); return; }
                if (has(body, "name")) c.setName(str(body, "name"));
                if (has(body, "phone")) c.setPhone(str(body, "phone"));
                if (has(body, "address")) c.setAddress(str(body, "address"));
                if (has(body, "email")) c.setEmail(str(body, "email"));
                if (has(body, "taxId")) c.setTaxId(str(body, "taxId"));
                updated = clientDAO.update(c);
            } else if ("SUPPLIER".equals(role)) {
                Supplier s = supplierDAO.findByUserId(userId);
                if (s == null) { notFound(resp, out, "Profil fournisseur introuvable"); return; }
                if (has(body, "companyName")) s.setCompanyName(str(body, "companyName"));
                if (has(body, "contactPerson")) s.setContactPerson(str(body, "contactPerson"));
                if (has(body, "phone")) s.setPhone(str(body, "phone"));
                if (has(body, "address")) s.setAddress(str(body, "address"));
                if (has(body, "email")) s.setEmail(str(body, "email"));
                updated = supplierDAO.update(s);
            } else {
                out.addProperty("success", false);
                out.addProperty("message", "Aucun profil modifiable pour ce rôle");
                resp.setStatus(400);
                resp.getWriter().print(gson.toJson(out));
                return;
            }

            out.addProperty("success", updated);
            out.addProperty("message", updated ? "Profil mis à jour" : "Aucune modification enregistrée");
            resp.setStatus(updated ? 200 : 400);
        } catch (Exception e) {
            out.addProperty("success", false);
            out.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(500);
        }
        resp.getWriter().print(gson.toJson(out));
    }

    private void notFound(HttpServletResponse resp, JsonObject out, String msg) throws IOException {
        out.addProperty("success", false);
        out.addProperty("message", msg);
        resp.setStatus(404);
        resp.getWriter().print(gson.toJson(out));
    }

    private static boolean has(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull();
    }

    private static String str(JsonObject o, String k) {
        return has(o, k) ? o.get(k).getAsString() : null;
    }
}
