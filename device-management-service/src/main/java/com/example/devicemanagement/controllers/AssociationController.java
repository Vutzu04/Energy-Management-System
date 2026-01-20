package com.example.devicemanagement.controllers;

import com.example.devicemanagement.dtos.AssociationRequest;
import com.example.devicemanagement.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/associations")
@CrossOrigin(origins = "*")
public class AssociationController {
    private final DeviceService deviceService;

    public AssociationController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAssociations() {
        try {
            List<?> associations = deviceService.getAllAssociations();
            return ResponseEntity.ok(associations);
        } catch (RuntimeException e) {
            System.err.println("ERROR fetching associations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping
    public ResponseEntity<?> associateDeviceToUser(@Valid @RequestBody AssociationRequest request) {
        try {
            deviceService.associateDeviceToUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (RuntimeException e) {
            System.err.println("ERROR creating association: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{userId}/{deviceId}")
    public ResponseEntity<Void> removeAssociation(@PathVariable UUID userId,
                                                     @PathVariable UUID deviceId) {
        try {
            deviceService.removeAssociation(userId, deviceId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/user/{userId}/username")
    public ResponseEntity<Void> updateAssociationUsername(
            @PathVariable UUID userId,
            @RequestParam String oldUsername,
            @RequestParam String newUsername) {
        try {
            System.out.println("DEBUG: AssociationController received request to update associations");
            deviceService.updateAssociationUsername(userId, oldUsername, newUsername);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("ERROR updating association username: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAssociationsByUserId(@PathVariable UUID userId) {
        try {
            System.out.println("DEBUG: AssociationController received request to delete associations for user " + userId);
            deviceService.deleteAssociationsByUserId(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("ERROR deleting associations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

