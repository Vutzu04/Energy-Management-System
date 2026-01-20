package com.example.apigateway.controllers;

import com.example.apigateway.services.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GatewayController {
    @Autowired
    private GatewayService gatewayService;

    // Authorization Service endpoints
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Object body, HttpServletRequest request) {
        return gatewayService.forwardToAuthorizationService("/auth/login", "POST", body, request);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Object body, HttpServletRequest request) {
        return gatewayService.forwardToAuthorizationService("/auth/register", "POST", body, request);
    }

    @GetMapping("/auth/users")
    public ResponseEntity<?> getAllAuthUsers(HttpServletRequest request) {
        // Public endpoint for debugging - no auth required
        return gatewayService.forwardToAuthorizationService("/auth/users", "GET", null, request);
    }

    // User Management endpoints (Admin only)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            gatewayService.checkAdminRole(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
        return gatewayService.forwardToUserManagementService("/users", "GET", null, request);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToUserManagementService("/users/" + id, "GET", null, request);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToUserManagementService("/users", "POST", body, request);
    }

    @PostMapping("/users/admin-create")
    public ResponseEntity<?> createUserWithCredentials(@RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToUserManagementService("/users/admin-create", "POST", body, request);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToUserManagementService("/users/" + id, "PUT", body, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToUserManagementService("/users/" + id, "DELETE", null, request);
    }

    // Device Management endpoints
    @GetMapping("/devices")
    public ResponseEntity<?> getAllDevices(HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/devices", "GET", null, request);
    }

    @GetMapping("/devices/my-devices")
    public ResponseEntity<?> getMyDevices(HttpServletRequest request) {
        // Client can access their own devices
        return gatewayService.forwardToDeviceManagementService("/devices/my-devices", "GET", null, request);
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable String id, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/devices/" + id, "GET", null, request);
    }

    @PostMapping("/devices")
    public ResponseEntity<?> createDevice(@RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/devices", "POST", body, request);
    }

    @PutMapping("/devices/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable String id, @RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/devices/" + id, "PUT", body, request);
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable String id, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/devices/" + id, "DELETE", null, request);
    }

    // Association endpoints (Admin only)
    @GetMapping("/associations")
    public ResponseEntity<?> getAllAssociations(HttpServletRequest request) {
        try {
            gatewayService.checkAdminRole(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
        return gatewayService.forwardToDeviceManagementService("/associations", "GET", null, request);
    }

    @PostMapping("/associations")
    public ResponseEntity<?> associateDevice(@RequestBody Object body, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/associations", "POST", body, request);
    }

    @DeleteMapping("/associations/{userId}/{deviceId}")
    public ResponseEntity<?> removeAssociation(@PathVariable String userId, @PathVariable String deviceId, HttpServletRequest request) {
        gatewayService.checkAdminRole(request);
        return gatewayService.forwardToDeviceManagementService("/associations/" + userId + "/" + deviceId, "DELETE", null, request);
    }

    // Monitoring Service endpoints
    @GetMapping("/monitoring/health")
    public ResponseEntity<?> monitoringHealth(HttpServletRequest request) {
        return gatewayService.forwardToMonitoringService("/api/monitoring/health", "GET", null, request);
    }

    @GetMapping("/monitoring/hourly-totals/{deviceId}")
    public ResponseEntity<?> getHourlyTotals(@PathVariable String deviceId, @RequestParam(name = "date") String date, HttpServletRequest request) {
        return gatewayService.forwardToMonitoringService("/api/monitoring/hourly-totals/" + deviceId + "?date=" + date, "GET", null, request);
    }

    @GetMapping("/monitoring/daily-total/{deviceId}")
    public ResponseEntity<?> getDailyTotal(@PathVariable String deviceId, @RequestParam(name = "date") String date, HttpServletRequest request) {
        return gatewayService.forwardToMonitoringService("/api/monitoring/daily-total/" + deviceId + "?date=" + date, "GET", null, request);
    }
}

