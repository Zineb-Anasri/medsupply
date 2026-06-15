package com.medsupply.dao;

import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.User;
import com.medsupply.utils.SupabaseClient;

public class UserDAO {

    public User authenticate(String email, String password) throws Exception {
        String filters = "email=eq." + SupabaseClient.enc(email) + "&is_active=eq.true";
        String response = SupabaseClient.get("users", filters);

        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            JsonObject userJson = jsonArray.get(0).getAsJsonObject();

            String storedHash = null;
            if (userJson.has("password_hash") && !userJson.get("password_hash").isJsonNull()) {
                storedHash = userJson.get("password_hash").getAsString();
            }

            if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                return mapJsonToUser(userJson);
            }
        }
        return null;
    }

    public User findByEmail(String email) throws Exception {
        String filters = "email=eq." + SupabaseClient.enc(email);
        String response = SupabaseClient.get("users", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToUser(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    public User findById(String userId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(userId);
        String response = SupabaseClient.get("users", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToUser(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    public User create(User user) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("email", user.getEmail());
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        body.addProperty("password_hash", hashedPassword);
        body.addProperty("role", user.getRole());
        body.addProperty("is_active", true);
        body.addProperty("email_verified", false);

        if (user.getVerificationToken() != null) {
            body.addProperty("verification_token", user.getVerificationToken());
        }
        if (user.getTokenExpiry() != null) {
            body.addProperty("token_expiry", user.getTokenExpiry().toString());
        }

        String response = SupabaseClient.post("users", body.toString());
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToUser(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    public boolean updatePassword(String userId, String newPassword) throws Exception {
        JsonObject body = new JsonObject();
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        body.addProperty("password_hash", hashedPassword);
        String response = SupabaseClient.patch("users", userId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    public boolean deactivate(String userId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_active", false);
        String response = SupabaseClient.patch("users", userId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    public User findByVerificationToken(String token) throws Exception {
        String filters = "verification_token=eq." + SupabaseClient.enc(token);
        String response = SupabaseClient.get("users", filters);
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToUser(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    public boolean setVerificationToken(String userId, String token, LocalDateTime expiry) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("verification_token", token);
        body.addProperty("token_expiry", expiry.toString());
        String response = SupabaseClient.patch("users", userId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    public boolean verifyEmail(String userId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("email_verified", true);
        body.addProperty("verification_token", (String) null);
        body.addProperty("token_expiry", (String) null);
        String response = SupabaseClient.patch("users", userId, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    private User mapJsonToUser(JsonObject json) {
        User user = new User();

        // id is UUID string
        if (json.has("id") && !json.get("id").isJsonNull()) {
            user.setUserId(json.get("id").getAsString());
        }

        user.setEmail(json.get("email").getAsString());

        if (json.has("password_hash") && !json.get("password_hash").isJsonNull()) {
            user.setPassword(json.get("password_hash").getAsString());
        }

        user.setRole(json.get("role").getAsString());
        user.setIsActive(json.get("is_active").getAsBoolean());

        if (json.has("email_verified") && !json.get("email_verified").isJsonNull()) {
            user.setEmailVerified(json.get("email_verified").getAsBoolean());
        } else {
            user.setEmailVerified(true); // default to true if column missing
        }

        if (json.has("verification_token") && !json.get("verification_token").isJsonNull()) {
            user.setVerificationToken(json.get("verification_token").getAsString());
        }

        if (json.has("token_expiry") && !json.get("token_expiry").isJsonNull()) {
            try {
                String dateStr = json.get("token_expiry").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                user.setTokenExpiry(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }

        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                user.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }

        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                user.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }

        return user;
    }
}