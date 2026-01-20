package com.example.devicemanagement.controllers;

import com.example.devicemanagement.dtos.AssociationRequest;
import com.example.devicemanagement.dtos.DeviceCreateDTO;
import com.example.devicemanagement.dtos.DeviceDTO;
import com.example.devicemanagement.dtos.DeviceUpdateDTO;
import com.example.devicemanagement.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceDTO>> getMyDevices(@RequestHeader("X-User-Id") String userIdOrUsername) {
        try {
            System.out.println("DEBUG: getMyDevices called with userIdOrUsername: " + userIdOrUsername);
            List<DeviceDTO> devices;
            
            // First try to parse as UUID (old way)
            try {
                UUID userId = UUID.fromString(userIdOrUsername);
                System.out.println("DEBUG: Parsed as UUID: " + userId);
                devices = deviceService.findByUserId(userId);
            } catch (IllegalArgumentException e) {
                // Not a UUID, treat as username
                System.out.println("DEBUG: Not a UUID, treating as username: " + userIdOrUsername);
                devices = deviceService.findByUsername(userIdOrUsername);
            }
            
            System.out.println("DEBUG: Found " + devices.size() + " devices for user: " + userIdOrUsername);
            devices.forEach(d -> System.out.println("  - Device: " + d.getName()));
            return ResponseEntity.ok(devices);
        } catch (RuntimeException e) {
            System.err.println("ERROR: Runtime exception in getMyDevices: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(deviceService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceCreateDTO dto) {
        try {
            DeviceDTO created = deviceService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable UUID id,
                                                  @RequestBody DeviceUpdateDTO dto) {
        try {
            return ResponseEntity.ok(deviceService.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        try {
            deviceService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

