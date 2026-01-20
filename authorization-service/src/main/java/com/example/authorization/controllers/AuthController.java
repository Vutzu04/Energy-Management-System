package com.example.authorization.controllers;

import com.example.authorization.dtos.CredentialDTO;
import com.example.authorization.dtos.ErrorResponse;
import com.example.authorization.dtos.LoginRequest;
import com.example.authorization.dtos.LoginResponse;
import com.example.authorization.dtos.RegisterRequest;
import com.example.authorization.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UUID credentialId = authService.register(request);
            Map<String, Object> response = Map.of(
                "id", credentialId.toString(),
                "username", request.getUsername(),
                "role", request.getRole()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register-with-id")
    public ResponseEntity<?> registerWithId(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String role = request.get("role");
            String id = request.get("id");
            
            if (username == null || password == null || role == null || id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Missing required fields"));
            }
            
            UUID credentialId = authService.registerWithId(username, password, role, UUID.fromString(id));
            Map<String, Object> response = Map.of(
                "id", credentialId.toString(),
                "username", username,
                "role", role
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<CredentialDTO>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCredential(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            authService.updatePassword(id, password);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/update-by-username/{username}")
    public ResponseEntity<?> updateCredentialByUsername(@PathVariable String username, @RequestBody Map<String, String> request) {
        try {
            System.out.println("DEBUG: AuthController - updateCredentialByUsername called for: " + username + " with data: " + request);
            authService.updateCredentialByUsername(username, request);
            System.out.println("DEBUG: AuthController - Update successful");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("ERROR: AuthController - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/delete-by-username/{username}")
    public ResponseEntity<?> deleteCredentialByUsername(@PathVariable String username) {
        try {
            authService.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}

