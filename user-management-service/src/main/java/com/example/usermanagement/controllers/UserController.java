package com.example.usermanagement.controllers;

import com.example.usermanagement.dtos.UserCreateDTO;
import com.example.usermanagement.dtos.UserDTO;
import com.example.usermanagement.dtos.UserUpdateDTO;
import com.example.usermanagement.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final RestTemplate restTemplate;

    public UserController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(userService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        try {
            UserDTO created = userService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/admin-create")
    public ResponseEntity<UserDTO> createUserWithCredentials(@Valid @RequestBody UserCreateDTO dto) {
        try {
            // Step 1: Create user in user_db first (with auto-generated ID)
            System.out.println("DEBUG: Creating user in user_db: " + dto.getUsername());
            UserDTO created = userService.create(dto);
            String userId = created.getId().toString();
            System.out.println("DEBUG: User created with ID: " + userId);
            
            // Step 2: Register in credential_db with the SAME ID
            try {
                Map<String, Object> credentialRequest = new HashMap<>();
                credentialRequest.put("username", dto.getUsername());
                credentialRequest.put("password", dto.getPassword());
                credentialRequest.put("role", dto.getRole() != null ? dto.getRole() : "Client");
                credentialRequest.put("id", userId);  // Send the user_db ID
                
                System.out.println("DEBUG: Creating credential for: " + dto.getUsername() + " with ID: " + userId);
                Map<String, Object> credentialResponse = restTemplate.postForObject(
                    "http://authorization-service:8081/auth/register-with-id",
                    credentialRequest,
                    Map.class
                );
                System.out.println("DEBUG: Credential created successfully with ID: " + userId);
            } catch (Exception credError) {
                System.err.println("WARNING: Credential registration failed: " + credError.getMessage());
                System.err.println("User was created in user_db but credential creation failed");
                // User is already created in user_db, so we still return success
                // The user can be used even without credential entry
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UserDTO(new UUID(0, 0), "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, 
                                              @RequestBody UserUpdateDTO dto) {
        try {
            System.out.println("DEBUG: Updating user " + id + " with data: " + dto.getUsername());
            
            // First, get the OLD username before updating (we'll need it to find the credential)
            UserDTO oldUser = userService.findById(id);
            String oldUsername = oldUser.getUsername();
            System.out.println("DEBUG: Old username: " + oldUsername);
            
            // Now update in user_db
            UserDTO updated = userService.update(id, dto);
            System.out.println("DEBUG: User updated in user_db. New username: " + updated.getUsername());
            System.out.println("DEBUG: Now syncing to credential_db...");
            
            // ALWAYS sync to credential_db if anything changed (username or password)
            // Use the OLD username to find the credential, then update it
            if ((dto.getPassword() != null && !dto.getPassword().isEmpty()) || 
                (dto.getUsername() != null && !dto.getUsername().isEmpty())) {
                
                Map<String, Object> credentialUpdate = new HashMap<>();
                String newUsername = updated.getUsername();
                
                // If username changed, we need to handle it specially
                if (dto.getUsername() != null && !dto.getUsername().isEmpty() && !oldUsername.equals(dto.getUsername())) {
                    // Username changed - we need to rename the credential
                    System.out.println("DEBUG: Username changed from " + oldUsername + " to " + dto.getUsername());
                    credentialUpdate.put("newUsername", dto.getUsername());
                    
                    // ALSO update the associations in device_db
                    System.out.println("DEBUG: Updating associations for user " + id);
                    try {
                        String updateAssocUrl = "http://device-management-service:8083/associations/user/" + id + "/username" +
                                                "?oldUsername=" + oldUsername + "&newUsername=" + newUsername;
                        System.out.println("DEBUG: Calling device service at: " + updateAssocUrl);
                        restTemplate.put(updateAssocUrl, null);
                        System.out.println("DEBUG: Associations updated successfully");
                    } catch (Exception assocError) {
                        System.err.println("WARNING: Failed to update associations: " + assocError.getMessage());
                        // Continue - associations can be fixed manually
                    }
                }
                
                // Add password if provided
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    credentialUpdate.put("password", dto.getPassword());
                }
                
                System.out.println("DEBUG: Syncing update to credential_db for username: " + oldUsername + " with data: " + credentialUpdate);
                try {
                    restTemplate.put(
                        "http://authorization-service:8081/auth/update-by-username/" + oldUsername,
                        credentialUpdate
                    );
                    System.out.println("DEBUG: Credential synced successfully");
                } catch (Exception credError) {
                    System.err.println("ERROR: Failed to sync credential update: " + credError.getMessage());
                    credError.printStackTrace();
                    // Log but don't fail - user is already updated
                }
            } else {
                System.out.println("DEBUG: No changes to sync");
            }
            
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("ERROR updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        try {
            System.out.println("DEBUG: Deleting user " + id);
            
            // First, get the user to find out their username
            UserDTO user = userService.findById(id);
            String username = user.getUsername();
            System.out.println("DEBUG: User username: " + username);
            
            // Delete from user_db
            userService.delete(id);
            System.out.println("DEBUG: User deleted from user_db. Now syncing to credential_db and device_db...");
            
            // Also delete from credential_db
            try {
                System.out.println("DEBUG: Deleting credential for username: " + username);
                restTemplate.delete("http://authorization-service:8081/auth/delete-by-username/" + username);
                System.out.println("DEBUG: Credential deleted successfully");
            } catch (Exception credError) {
                System.err.println("ERROR: Failed to delete credential: " + credError.getMessage());
                credError.printStackTrace();
                // User is already deleted from user_db, so we continue
            }
            
            // Also delete associations from device_db
            try {
                System.out.println("DEBUG: Deleting associations for user " + id);
                restTemplate.delete("http://device-management-service:8083/associations/user/" + id);
                System.out.println("DEBUG: Associations deleted successfully");
            } catch (Exception assocError) {
                System.err.println("ERROR: Failed to delete associations: " + assocError.getMessage());
                assocError.printStackTrace();
                // User is already deleted from user_db, so we continue
            }
            
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("ERROR deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}

