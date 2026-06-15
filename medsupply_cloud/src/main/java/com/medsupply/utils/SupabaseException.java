package com.medsupply.utils;

/**
 * Raised when a Supabase PostgREST request returns an error status.
 *
 * <p>The full upstream response body (which may contain schema/constraint/RLS detail)
 * is logged server-side and kept in {@link #getDetail()}, but {@link #getMessage()}
 * deliberately returns only a generic, client-safe string so controllers that echo
 * {@code e.getMessage()} cannot leak backend internals to API consumers.
 */
public class SupabaseException extends RuntimeException {

    private final int statusCode;
    private final String detail;

    public SupabaseException(String verb, int statusCode, String responseBody) {
        super("Database request failed (HTTP " + statusCode + ")");
        this.statusCode = statusCode;
        this.detail = responseBody;
        // Server-side log only — never returned to the client.
        System.err.println("SupabaseException: " + verb + " -> " + statusCode + " - " + responseBody);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDetail() {
        return detail;
    }
}
