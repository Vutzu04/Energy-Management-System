package com.example.usermanagement.services;

import com.example.usermanagement.config.RabbitMQConfig;
import com.example.usermanagement.dtos.UserCreateDTO;
import com.example.usermanagement.dtos.UserDTO;
import com.example.usermanagement.dtos.UserSyncMessage;
import com.example.usermanagement.dtos.UserUpdateDTO;
import com.example.usermanagement.entities.User;
import com.example.usermanagement.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }

    public UserDTO findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getId(), user.getUsername());
    }

    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());

        User saved = userRepository.save(user);
        
        // Publish USER_CREATED event to RabbitMQ
        try {
            UserSyncMessage syncMessage = new UserSyncMessage(
                saved.getId(),
                saved.getUsername(),
                "Client", // Default role
                "USER_CREATED"
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.SYNC_EVENT_ROUTING_KEY,
                syncMessage
            );
            System.out.println("📤 PUBLISHED: USER_CREATED event for " + saved.getUsername());
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to publish USER_CREATED event: " + e.getMessage());
            // Don't fail the entire operation if event publishing fails
        }
        
        return new UserDTO(saved.getId(), saved.getUsername());
    }


    @Transactional
    public UserDTO update(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            if (!user.getUsername().equals(dto.getUsername()) && 
                userRepository.existsByUsername(dto.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(dto.getPassword());
        }

        User updated = userRepository.save(user);
        return new UserDTO(updated.getId(), updated.getUsername());
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String username = user.getUsername();
        userRepository.deleteById(id);
        
        // Publish USER_DELETED event to RabbitMQ
        try {
            UserSyncMessage syncMessage = new UserSyncMessage(
                id,
                username,
                "Client",
                "USER_DELETED"
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.SYNC_EVENT_ROUTING_KEY,
                syncMessage
            );
            System.out.println("📤 PUBLISHED: USER_DELETED event for " + username);
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to publish USER_DELETED event: " + e.getMessage());
        }
    }
}

