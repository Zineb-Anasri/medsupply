package com.medsupply.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CorsFilter - Handles CORS for development
 * Supports both localhost:5500 and 127.0.0.1:5500 for development
 * TODO: Restrict to specific origins in production
 */
@WebFilter(urlPatterns = {"/*"})
public class CorsFilter implements Filter {
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) res;
    HttpServletRequest request = (HttpServletRequest) req;
    
    // Get the origin from the request
    String origin = request.getHeader("Origin");
    
    // Allow both localhost:5500 and 127.0.0.1:5500 for development
    if (origin != null && (origin.equals("http://localhost:5500") || origin.equals("http://127.0.0.1:5500"))) {
      response.setHeader("Access-Control-Allow-Origin", origin);
    } else {
      // Fallback for development - allow localhost
      response.setHeader("Access-Control-Allow-Origin", "http://localhost:5500");
    }
    
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Max-Age", "3600");
    
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }
    chain.doFilter(req, res);
  }
  @Override public void init(FilterConfig fc) {}
  @Override public void destroy() {}
}
