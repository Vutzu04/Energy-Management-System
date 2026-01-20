package com.example.apigateway.services;

import com.example.apigateway.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GatewayService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${authorization.service.url}")
    private String authorizationServiceUrl;

    @Value("${user.management.service.url}")
    private String userManagementServiceUrl;

    @Value("${device.management.service.url}")
    private String deviceManagementServiceUrl;

    @Value("${monitoring.service.url}")
    private String monitoringServiceUrl;

    public ResponseEntity<?> forwardToAuthorizationService(String path, String method, Object body, HttpServletRequest request) {
        String url = authorizationServiceUrl + path;
        return forwardRequest(url, method, body, request);
    }

    public ResponseEntity<?> forwardToUserManagementService(String path, String method, Object body, HttpServletRequest request) {
        String url = userManagementServiceUrl + path;
        return forwardRequest(url, method, body, request);
    }

    public ResponseEntity<?> forwardToDeviceManagementService(String path, String method, Object body, HttpServletRequest request) {
        String url = deviceManagementServiceUrl + path;
        
        // For my-devices endpoint, add username from token (not userId, because IDs may not match)
        if (path.equals("/devices/my-devices")) {
            String token = (String) request.getAttribute("token");
            if (token == null) {
                System.err.println("ERROR: No token found in request for /devices/my-devices");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"No token in request\"}");
            }
            try {
                String username = jwtUtil.extractUsername(token);
                System.out.println("DEBUG: Forwarding /devices/my-devices for username: " + username);
                HttpHeaders headers = createHeaders(request);
                headers.set("X-User-Id", username);  // Send username instead of userId
                return forwardRequestWithHeaders(url, method, body, headers);
            } catch (Exception e) {
                System.err.println("ERROR: Failed to extract username from token: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
            }
        }
        
        return forwardRequest(url, method, body, request);
    }

    public ResponseEntity<?> forwardToMonitoringService(String path, String method, Object body, HttpServletRequest request) {
        String url = monitoringServiceUrl + path;
        return forwardRequest(url, method, body, request);
    }

    private ResponseEntity<?> forwardRequest(String url, String method, Object body, HttpServletRequest request) {
        HttpHeaders headers = createHeaders(request);
        return forwardRequestWithHeaders(url, method, body, headers);
    }

    private HttpHeaders createHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<?> forwardRequestWithHeaders(String url, String method, Object body, HttpHeaders headers) {
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        
        try {
            switch (method.toUpperCase()) {
                case "GET":
                    return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
                case "POST":
                    return restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
                case "PUT":
                    return restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
                case "DELETE":
                    return restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
                default:
                    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
            }
        } catch (HttpClientErrorException e) {
            // Forward the actual error response from the microservice (400, 403, 404, etc.)
            System.err.println("Backend service error: " + e.getStatusCode() + " - " + e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("ERROR forwarding request to " + url + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    public void checkAdminRole(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new RuntimeException("No role found in request. User might not be authenticated.");
        }
        if (!role.equals("Administrator")) {
            throw new RuntimeException("Access denied: Only Administrator role can perform this action. Your role: " + role);
        }
    }
}

