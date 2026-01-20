package com.example.apigateway.filters;

import com.example.apigateway.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Allow auth endpoints without token
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow monitoring endpoints - they require authentication but will be filtered in controller
        if (path.startsWith("/api/monitoring/")) {
            // Extract token and validate
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);
                        String role = jwtUtil.extractRole(token);
                        String userId = jwtUtil.extractUserId(token);
                        request.setAttribute("username", username);
                        request.setAttribute("role", role);
                        request.setAttribute("userId", userId);
                        request.setAttribute("token", token);
                    }
                } catch (Exception e) {
                    // Token invalid, but we'll let it through to monitoring service
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Check for token in Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Add user info to request attributes for use in controllers
        try {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            String userId = jwtUtil.extractUserId(token);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            request.setAttribute("userId", userId);
            request.setAttribute("token", token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

