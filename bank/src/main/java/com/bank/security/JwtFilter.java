package com.bank.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter implements Filter {

    private JwtUtil jwtUtil;

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            jwtUtil = new JwtUtil(); // khởi tạo sau khi context load xong
            System.out.println("✅ JwtFilter initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to init JwtFilter: " + e.getMessage());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        System.out.println(">>> Request path: " + path);

        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());

        if (relativePath.startsWith("/login")
                || relativePath.startsWith("/api/auth/register")
                || relativePath.startsWith("/api/auth/login")
                || relativePath.startsWith("/api/auth/verify")
                || relativePath.startsWith("/api/auth/me")
                || relativePath.startsWith("/api/users")
                || relativePath.startsWith("/api/transaction")
                || relativePath.startsWith("/register")
                || relativePath.startsWith("/public")
                || relativePath.startsWith("/assets")
                || relativePath.startsWith("/views")) {
            chain.doFilter(request, response);
            return;
        }


        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\":\"Invalid token\"}");
                return;
            }

            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            System.out.println(">>> Authenticated user: " + username + " role=" + role);

            // Có thể gắn attribute để servlet khác lấy
            req.setAttribute("username", username);
            req.setAttribute("role", role);

            chain.doFilter(request, response);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"JWT validation failed: " + e.getMessage() + "\"}");
        }
    }
}
