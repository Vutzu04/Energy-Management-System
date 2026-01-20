package com.example.customersupport.repositories;

import com.example.customersupport.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<ChatMessage> findByUserIdAndIsFromAdminFalseOrderByCreatedAtDesc(UUID userId);

    // Get all messages forwarded to admin (pending admin review)
    List<ChatMessage> findByResponseTypeOrderByCreatedAtDesc(String responseType);

    // Get all pending admin messages
    List<ChatMessage> findByResponseType(String responseType);
}


