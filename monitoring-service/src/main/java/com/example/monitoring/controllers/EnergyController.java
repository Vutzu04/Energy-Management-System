package com.example.monitoring.controllers;

import com.example.monitoring.entities.HourlyTotal;
import com.example.monitoring.services.EnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class EnergyController {
    
    @Autowired
    private EnergyService energyService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Monitoring Microservice");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get hourly energy totals for a specific device on a specific date
     * 
     * Example: GET /api/monitoring/hourly-totals/550e8400-e29b-41d4-a716-446655440000?date=2025-11-13
     */
    @GetMapping("/hourly-totals/{deviceId}")
    public ResponseEntity<?> getHourlyTotals(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            // If no date provided, use today
            LocalDateTime dateTime;
            if (date == null) {
                dateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            } else {
                dateTime = date.atStartOfDay();
            }
            
            System.out.println("DEBUG: Fetching hourly totals for device: " + deviceId + ", date: " + dateTime);
            
            List<HourlyTotal> totals = energyService.getHourlyTotalsForDate(deviceId, dateTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("date", dateTime.toLocalDate());
            response.put("totalCount", totals.size());
            response.put("data", totals);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching hourly totals: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch hourly totals: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get daily total consumption for a device
     * 
     * Example: GET /api/monitoring/daily-total/550e8400-e29b-41d4-a716-446655440000?date=2025-11-13
     */
    @GetMapping("/daily-total/{deviceId}")
    public ResponseEntity<?> getDailyTotal(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            // If no date provided, use today
            LocalDateTime dateTime;
            if (date == null) {
                dateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            } else {
                dateTime = date.atStartOfDay();
            }
            
            System.out.println("DEBUG: Fetching daily total for device: " + deviceId + ", date: " + dateTime);
            
            Float dailyTotal = energyService.getDailyTotal(deviceId, dateTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("date", dateTime.toLocalDate());
            response.put("totalConsumptionKwh", dailyTotal);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching daily total: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch daily total: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

