package com.medsupply.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Raised when a Supabase PostgREST request returns an error status.
 *
 * <p>The full upstream response body is logged server-side and kept in {@link #getDetail()}.
 * {@link #getMessage()} returns a concise, actionable summary: the HTTP status plus the
 * PostgREST {@code message}/{@code details} (e.g. the violated constraint name) when present —
 * this is operational diagnostics (constraint/column names), not credentials.
 */
public class SupabaseException extends RuntimeException {

    private final int statusCode;
    private final String detail;

    public SupabaseException(String verb, int statusCode, String responseBody) {
        super(buildMessage(statusCode, responseBody));
        this.statusCode = statusCode;
        this.detail = responseBody;
        System.err.println("SupabaseException: " + verb + " -> " + statusCode + " - " + responseBody);
    }

    private static String buildMessage(int statusCode, String responseBody) {
        String base = "Database request failed (HTTP " + statusCode + ")";
        String pg = extractPgMessage(responseBody);
        return pg == null ? base : base + ": " + pg;
    }

    /** Pulls the human-readable message/details out of a PostgREST error JSON body. */
    private static String extractPgMessage(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            StringBuilder sb = new StringBuilder();
            if (o.has("message") && !o.get("message").isJsonNull()) {
                sb.append(o.get("message").getAsString());
            }
            if (o.has("details") && !o.get("details").isJsonNull()) {
                String d = o.get("details").getAsString();
                if (!d.isBlank()) sb.append(sb.length() > 0 ? " — " : "").append(d);
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDetail() {
        return detail;
    }
}
