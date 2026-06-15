package com.medsupply.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthFilter - Session-based authentication filter
 * Protects routes that require authentication
 * Configure which paths to protect in web.xml or via @WebFilter
 */
@WebFilter(urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

    // Paths that don't require authentication
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/verify",
        "/api/test"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Check if path is public
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check for valid session
        HttpSession session = httpRequest.getSession(false);
        Object userId = session == null ? null : session.getAttribute("userId");
        if (!(userId instanceof String) || ((String) userId).trim().isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"success\": false, \"message\": \"Unauthorized - Please login\"}");
            return;
        }

        // User is authenticated, proceed
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }

    /**
     * Check if the requested path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}
