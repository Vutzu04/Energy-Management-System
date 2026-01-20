package com.example.customersupport.services;

import com.example.customersupport.dtos.ChatMessageDTO;
import com.example.customersupport.entities.ChatMessage;
import com.example.customersupport.repositories.ChatRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private AICustomerSupportService aiCustomerSupportService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Process incoming chat message from user
     * 1. Check if it matches a rule
     * 2. If yes: respond with rule-based answer
     * 3. If no: try AI-driven response
     * 4. If AI unavailable: forward to admin
     * 5. Send response back via WebSocket
     */
    @Transactional
    public void processChatMessage(ChatMessageDTO incomingMsg) {
        System.out.println("\n====================================================");
        System.out.println("📨 Processing chat message from: " + incomingMsg.getUsername());
        System.out.println("   Message: " + incomingMsg.getMessage());
        System.out.println("====================================================\n");

        // 1. Save incoming message
        ChatMessage message = new ChatMessage();
        message.setUserId(incomingMsg.getUserId());
        message.setUsername(incomingMsg.getUsername());
        message.setMessage(incomingMsg.getMessage());
        message.setIsFromAdmin(false);
        message.setCreatedAt(LocalDateTime.now());

        // 2. Try to match rules first (highest priority)
        RuleEngine.RuleMatchResult ruleMatch = ruleEngine.matchRule(incomingMsg.getMessage());

        if (ruleMatch != null) {
            // Rule matched! Respond immediately
            System.out.println("✅ Rule matched!");
            System.out.println("   Matched by: " + ruleMatch.matchedBy);
            System.out.println("   Response: " + ruleMatch.response);

            message.setResponse(ruleMatch.response);
            message.setResponseType("RULE_BASED");
            message.setMatchedRuleId(ruleMatch.ruleId);
            message.setResponseTime(LocalDateTime.now());
        } else {
            // No rule matched - try AI-driven response
            System.out.println("❌ No rule matched.");
            System.out.println("   Attempting AI-driven response...");
            
            try {
                String aiResponse = aiCustomerSupportService.generateAIResponse(incomingMsg.getMessage());
                System.out.println("✅ AI response generated!");
                System.out.println("   Response: " + aiResponse);
                
                message.setResponse(aiResponse);
                message.setResponseType("AI_GENERATED");
                message.setResponseTime(LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("❌ AI response failed: " + e.getMessage());
                System.out.println("   Forwarding to admin...");
                
                message.setResponse("Thank you for your message. An administrator will review it shortly.");
                message.setResponseType("FORWARDED_TO_ADMIN");
                message.setResponseTime(LocalDateTime.now());
            }
        }

        // 3. Save message with response
        ChatMessage savedMessage = chatRepository.save(message);
        System.out.println("✅ Message saved with ID: " + savedMessage.getId());

        // 4. Send response back to user via WebSocket
        ChatMessageDTO responseDTO = new ChatMessageDTO();
        responseDTO.setId(savedMessage.getId());
        responseDTO.setUserId(message.getUserId());
        responseDTO.setUsername(message.getUsername());
        responseDTO.setMessage(message.getMessage());
        responseDTO.setResponse(message.getResponse());
        responseDTO.setResponseType(message.getResponseType());
        responseDTO.setCreatedAt(message.getCreatedAt());
        responseDTO.setResponseTime(message.getResponseTime());

        // Publish to WebSocket service via RabbitMQ
        try {
            rabbitTemplate.convertAndSend(
                "websocket_exchange",
                "websocket.chat",
                responseDTO
            );
            System.out.println("📤 Response sent to WebSocket service");
        } catch (Exception e) {
            System.err.println("❌ Error sending response to WebSocket: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n====================================================\n");
    }

    /**
     * Get chat history for a user
     */
    public List<ChatMessage> getChatHistory(UUID userId) {
        return chatRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get user messages only (not admin responses)
     */
    public List<ChatMessage> getUserMessages(UUID userId) {
        return chatRepository.findByUserIdAndIsFromAdminFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all messages awaiting admin response (forwarded messages)
     */
    public List<ChatMessage> getPendingAdminMessages() {
        return chatRepository.findByResponseTypeOrderByCreatedAtDesc("FORWARDED_TO_ADMIN");
    }

    /**
     * Get all chat messages (for admin dashboard)
     */
    public List<ChatMessage> getAllMessages() {
        return chatRepository.findAll();
    }

    /**
     * Admin responds to a client message
     */
    @Transactional
    public ChatMessage respondToMessage(UUID messageId, String adminResponse) {
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        System.out.println("\n====================================================");
        System.out.println("📨 Admin responding to message from: " + message.getUsername());
        System.out.println("   Original message: " + message.getMessage());
        System.out.println("   Admin response: " + adminResponse);
        System.out.println("====================================================\n");

        // Update message with admin response
        message.setResponse(adminResponse);
        message.setResponseType("ADMIN_RESPONSE");
        message.setIsFromAdmin(true);
        message.setResponseTime(LocalDateTime.now());

        ChatMessage savedMessage = chatRepository.save(message);
        System.out.println("✅ Admin response saved!");

        return savedMessage;
    }

}

