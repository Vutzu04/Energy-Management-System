package com.example.authorization.services;

import com.example.authorization.dtos.CredentialDTO;
import com.example.authorization.dtos.LoginRequest;
import com.example.authorization.dtos.LoginResponse;
import com.example.authorization.dtos.RegisterRequest;
import com.example.authorization.entities.Credential;
import com.example.authorization.repositories.CredentialRepository;
import com.example.authorization.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(CredentialRepository credentialRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(credential.getUsername(), credential.getRole(), credential.getId().toString());
        return new LoginResponse(token, credential.getRole(), credential.getUsername(), credential.getId().toString());
    }

    @Transactional
    public UUID register(RegisterRequest request) {
        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!request.getRole().equals("Administrator") && !request.getRole().equals("Client")) {
            throw new RuntimeException("Role must be either 'Administrator' or 'Client'");
        }

        Credential credential = new Credential();
        credential.setUsername(request.getUsername());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setRole(request.getRole());

        Credential saved = credentialRepository.save(credential);
        System.out.println("DEBUG: Registered credential with ID: " + saved.getId());
        return saved.getId();
    }

    @Transactional
    public UUID registerWithId(String username, String password, String role, UUID providedId) {
        // Check if username already exists - this is the unique constraint
        var existingCred = credentialRepository.findByUsername(username);
        if (existingCred.isPresent()) {
            System.out.println("DEBUG: Username '" + username + "' already exists in credential_db");
            // Return existing credential ID
            return existingCred.get().getId();
        }

        if (!role.equals("Administrator") && !role.equals("Client")) {
            throw new RuntimeException("Role must be either 'Administrator' or 'Client'");
        }

        // Use the provided ID from user_db to ensure consistency across databases
        Credential credential = new Credential();
        credential.setId(providedId);  // Set the ID from user_db
        credential.setUsername(username);
        credential.setPassword(passwordEncoder.encode(password));
        credential.setRole(role);

        try {
            Credential saved = credentialRepository.save(credential);
            System.out.println("DEBUG: Registered credential for username '" + username + "' with provided ID: " + saved.getId());
            return saved.getId();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to save credential for username '" + username + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to register credential: " + e.getMessage());
        }
    }

    public List<CredentialDTO> getAllUsers() {
        return credentialRepository.findAll().stream()
                .map(credential -> new CredentialDTO(
                        credential.getId(),
                        credential.getUsername(),
                        credential.getRole()
                ))
                .toList();
    }

    @Transactional
    public void updatePassword(UUID userId, String newPassword) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        credential.setPassword(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
    }

    @Transactional
    public void updatePasswordByUsername(String username, String newPassword) {
        Credential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        credential.setPassword(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
    }

    @Transactional
    public void updateCredentialByUsername(String username, Map<String, String> updates) {
        Credential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Update password if provided
        if (updates.containsKey("password")) {
            credential.setPassword(passwordEncoder.encode(updates.get("password")));
            System.out.println("DEBUG: Updated password for username: " + username);
        }
        
        // Update username if provided (rename)
        if (updates.containsKey("newUsername")) {
            String newUsername = updates.get("newUsername");
            credential.setUsername(newUsername);
            System.out.println("DEBUG: Updated username from " + username + " to " + newUsername);
        }
        
        credentialRepository.save(credential);
    }

    @Transactional
    public void deleteByUsername(String username) {
        Credential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        credentialRepository.delete(credential);
        System.out.println("DEBUG: Credential deleted for username: " + username);
    }
}

