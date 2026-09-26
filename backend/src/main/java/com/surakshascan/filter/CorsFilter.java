package com.surakshascan.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");
        String allowedOrigin = System.getenv("FRONTEND_URL");

        // IMPORTANT: Access-Control-Allow-Credentials: true is INCOMPATIBLE with
        // Access-Control-Allow-Origin: * — browsers will block the response.
        // We must always reflect a specific origin when credentials are involved.
        if (allowedOrigin != null && !allowedOrigin.trim().isEmpty()) {
            // Explicit whitelist via FRONTEND_URL env var (set this on Render)
            res.setHeader("Access-Control-Allow-Origin", allowedOrigin.trim());
        } else if (origin != null && !origin.isEmpty()) {
            // Reflect the request origin — safe because credentials: include is required
            res.setHeader("Access-Control-Allow-Origin", origin);
        }
        // If no origin at all (e.g. direct server-to-server), omit the header entirely.

        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
