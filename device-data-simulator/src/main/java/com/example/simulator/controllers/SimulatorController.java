package com.example.simulator.controllers;

import com.example.simulator.services.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    @Autowired
    private SimulatorService simulatorService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Device Data Simulator is UP!");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", simulatorService.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> start() {
        simulatorService.startSimulator();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Simulator started");
        response.put("status", simulatorService.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stop() {
        simulatorService.stopSimulator();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Simulator stopped");
        response.put("status", simulatorService.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-reading")
    public ResponseEntity<Map<String, String>> sendReading(@RequestBody Map<String, Object> request) {
        try {
            String deviceIdStr = (String) request.get("deviceId");
            Number measurementValue = (Number) request.get("measurementValue");

            if (deviceIdStr == null || measurementValue == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing deviceId or measurementValue");
                return ResponseEntity.badRequest().body(error);
            }

            simulatorService.sendManualReading(deviceIdStr, measurementValue.doubleValue());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Reading sent successfully");
            response.put("deviceId", deviceIdStr);
            response.put("value", measurementValue.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

