package com.medsupply.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * SupabaseClient - thin HTTP wrapper over the Supabase PostgREST API.
 *
 * <p>Configuration (URL + anon key) is loaded, in order of precedence, from:
 * <ol>
 *   <li>Environment variables {@code SUPABASE_URL} / {@code SUPABASE_ANON_KEY}</li>
 *   <li>JVM system properties {@code supabase.url} / {@code supabase.anon.key}</li>
 *   <li>A classpath {@code supabase.properties} file (keys {@code supabase.url} / {@code supabase.anon.key})</li>
 * </ol>
 * Secrets must NOT be hard-coded here. Provide {@code src/main/resources/supabase.properties}
 * (git-ignored) for local development. See {@code supabase.properties.example}.
 */
public class SupabaseClient {

    private static final String BASE_URL;
    private static final String SUPABASE_ANON_KEY;

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    private static final Gson gson = new Gson();

    static {
        Properties props = new Properties();
        try (InputStream in = SupabaseClient.class.getClassLoader().getResourceAsStream("supabase.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("SupabaseClient: failed to read supabase.properties - " + e.getMessage());
        }

        String url = firstNonBlank(
            System.getenv("SUPABASE_URL"),
            System.getProperty("supabase.url"),
            props.getProperty("supabase.url"));
        String key = firstNonBlank(
            System.getenv("SUPABASE_ANON_KEY"),
            System.getProperty("supabase.anon.key"),
            props.getProperty("supabase.anon.key"));

        if (url == null || key == null) {
            throw new ExceptionInInitializerError(
                "Supabase configuration missing. Set SUPABASE_URL/SUPABASE_ANON_KEY env vars, "
                + "the supabase.url/supabase.anon.key system properties, or provide a classpath "
                + "supabase.properties file (see supabase.properties.example).");
        }

        // Accept either the bare project URL or one that already includes /rest/v1.
        url = url.replaceAll("/+$", "");
        BASE_URL = url.endsWith("/rest/v1") ? url : url + "/rest/v1";
        SUPABASE_ANON_KEY = key;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }

    /**
     * URL-encodes a single dynamic value for safe inclusion in a PostgREST filter.
     * Encode only VALUES (e.g. the part after {@code eq.}), never the structural
     * column/operator portion of a filter string.
     */
    public static String enc(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static HttpRequest.Builder baseRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json");
    }

    /**
     * GET request to Supabase.
     * @param table Table name
     * @param filters PostgREST query string (e.g. "email=eq.test%40example.com"); values should be enc()'d by the caller
     * @return Response body as String
     */
    public static String get(String table, String filters) throws Exception {
        String url = BASE_URL + "/" + table;
        if (filters != null && !filters.isEmpty()) {
            url += "?" + filters;
        }

        HttpRequest request = baseRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new SupabaseException("GET", response.statusCode(), response.body());
        }
        return response.body();
    }

    /**
     * POST request to Supabase.
     * @param table Table name
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String post(String table, String body) throws Exception {
        HttpRequest request = baseRequest(BASE_URL + "/" + table)
            .header("Prefer", "return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new SupabaseException("POST", response.statusCode(), response.body());
        }
        return response.body();
    }

    /**
     * PATCH request to Supabase by primary-key id.
     * @param table Table name
     * @param id Record id (raw UUID; a leading "id=eq." prefix is tolerated)
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String patch(String table, String id, String body) throws Exception {
        String cleanId = id.startsWith("id=eq.") ? id.substring(6) : id;
        String url = BASE_URL + "/" + table + "?id=eq." + enc(cleanId);

        HttpRequest request = baseRequest(url)
            .header("Prefer", "return=representation")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new SupabaseException("PATCH", response.statusCode(), response.body());
        }
        return response.body();
    }

    /**
     * PATCH request to Supabase with custom filters.
     * @param table Table name
     * @param filters PostgREST query string (values should be enc()'d by the caller)
     * @param body JSON body as String
     * @return Response body as String
     */
    public static String patchWithFilters(String table, String filters, String body) throws Exception {
        String url = BASE_URL + "/" + table + "?" + filters;

        HttpRequest request = baseRequest(url)
            .header("Prefer", "return=representation")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new SupabaseException("PATCH", response.statusCode(), response.body());
        }
        return response.body();
    }

    /**
     * DELETE request to Supabase by primary-key id.
     * @param table Table name
     * @param id Record id (raw UUID)
     * @return Response body as String
     */
    public static String delete(String table, String id) throws Exception {
        return deleteWithFilters(table, "id=eq." + enc(id));
    }

    /**
     * DELETE request to Supabase with custom filters (e.g. "order_id=eq." + enc(orderId)).
     * Uses {@code Prefer: return=representation} so the response contains the deleted rows,
     * enabling affected-row detection via {@link #affectedRows(String)}.
     * @param table Table name
     * @param filters PostgREST query string (values should be enc()'d by the caller)
     * @return Response body as String
     */
    public static String deleteWithFilters(String table, String filters) throws Exception {
        String url = BASE_URL + "/" + table + "?" + filters;

        HttpRequest request = baseRequest(url)
            .header("Prefer", "return=representation")
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new SupabaseException("DELETE", response.statusCode(), response.body());
        }
        return response.body();
    }

    /**
     * Returns the number of rows in a {@code return=representation} response body.
     * Use this for PATCH/DELETE success detection instead of {@code !body.isEmpty()}:
     * PostgREST returns an empty JSON array {@code "[]"} (which is non-empty as a String)
     * when zero rows matched.
     */
    public static int affectedRows(String representationBody) {
        if (representationBody == null || representationBody.isBlank()) {
            return 0;
        }
        try {
            JsonArray arr = parseJsonArray(representationBody);
            return arr == null ? 0 : arr.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Helper method to convert object to JSON */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /** Helper method to parse JSON to object */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /** Helper method to parse JSON array */
    public static JsonArray parseJsonArray(String json) {
        return gson.fromJson(json, JsonArray.class);
    }

    /** Helper method to parse JSON object */
    public static JsonObject parseJsonObject(String json) {
        return gson.fromJson(json, JsonObject.class);
    }
}
