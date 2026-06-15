package com.medsupply.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class SupabaseClient {
    private static final String BASE_URL = "https://swpzvmogscgddwocdlwv.supabase.co/rest/v1";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN3cHp2bW9nc2NnZGR3b2NkbHd2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA5MjQxNTIsImV4cCI6MjA5NjUwMDE1Mn0.0ixPyns0UcqGrSnsQocCjzz5k_fQsbS0cJQoFu98Ujs";
    
    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    
    private static final Gson gson = new Gson();

    /**
     * GET request to Supabase
     * @param table Table name
     * @param filters Query filters (e.g., "email=eq.test@example.com")
     * @return Response body as String
     */
    public static String get(String table, String filters) throws Exception {
        String url = BASE_URL + "/" + table;
        if (filters != null && !filters.isEmpty()) {
            url += "?" + filters;
        }
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Supabase GET error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }

    /**
     * POST request to Supabase
     * @param table Table name
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String post(String table, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + table))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Supabase POST error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }

    /**
     * PATCH request to Supabase
     * @param table Table name
     * @param id Record ID
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String patch(String table, String id, String body) throws Exception {
        // Sanitize id: strip "id=eq." prefix if present to ensure raw UUID only
        String cleanId = id.startsWith("id=eq.") ? id.substring(6) : id;
        String url = BASE_URL + "/" + table + "?id=eq." + cleanId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            System.err.println("PATCH Error - URL: " + url + ", Body: " + body + ", Response: " + response.body());
            throw new RuntimeException("Supabase PATCH error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }

    /**
     * PATCH request to Supabase with custom filters
     * @param table Table name
     * @param filters Query filters
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String patchWithFilters(String table, String filters, String body) throws Exception {
        // Sanitize filters: handle "id=eq." prefix to avoid double prefix
        // Only sanitize the "id" column specifically, not other columns like "tender_id"
        String cleanFilters = filters.replaceAll("(^|&)id=eq\\.", "$1id=");
        cleanFilters = cleanFilters.replaceAll("(^|&)id=", "$1id=eq.");
        String url = BASE_URL + "/" + table + "?" + cleanFilters;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            System.err.println("PATCH Error - URL: " + url + ", Body: " + body + ", Response: " + response.body());
            throw new RuntimeException("Supabase PATCH error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }

    /**
     * DELETE request to Supabase
     * @param table Table name
     * @param id Record ID
     * @return Response body as String
     */
    public static String delete(String table, String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + table + "?id=eq." + id))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .DELETE()
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Supabase DELETE error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }

    /**
     * Helper method to convert object to JSON
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Helper method to parse JSON to object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Helper method to parse JSON array
     */
    public static JsonArray parseJsonArray(String json) {
        return gson.fromJson(json, JsonArray.class);
    }

    /**
     * Helper method to parse JSON object
     */
    public static JsonObject parseJsonObject(String json) {
        return gson.fromJson(json, JsonObject.class);
    }
}
