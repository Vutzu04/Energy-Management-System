package com.example.authorization.config;

import com.example.authorization.entities.Credential;
import com.example.authorization.repositories.CredentialRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initializeDefaultUser(CredentialRepository credentialRepository,
                                                    PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if default admin user already exists
            if (!credentialRepository.existsByUsername("admin123")) {
                Credential adminUser = new Credential();
                adminUser.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
                adminUser.setUsername("admin123");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole("Administrator");
                
                credentialRepository.save(adminUser);
                System.out.println("✅ Default admin user (admin123) created successfully!");
            } else {
                System.out.println("✅ Default admin user already exists");
            }
        };
    }
}
