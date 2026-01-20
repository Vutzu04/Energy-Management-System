package com.example.customersupport.controllers;

import com.example.customersupport.dtos.ChatMessageDTO;
import com.example.customersupport.entities.ChatMessage;
import com.example.customersupport.services.ChatService;
import com.example.customersupport.services.AICustomerSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AICustomerSupportService aiCustomerSupportService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "✅ Customer Support Service is running";
    }

    /**
     * Send a chat message
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDTO message) {
        try {
            System.out.println("📨 Received chat message from: " + message.getUsername());
            
            // Process the message
            chatService.processChatMessage(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message processed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error processing chat message: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get chat history for a user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getChatHistory(@PathVariable UUID userId) {
        try {
            List<ChatMessage> history = chatService.getChatHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("messageCount", history.size());
            response.put("messages", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching chat history: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all pending messages awaiting admin review
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingAdminMessages() {
        try {
            List<ChatMessage> pendingMessages = chatService.getPendingAdminMessages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("pendingCount", pendingMessages.size());
            response.put("messages", pendingMessages);
            
            System.out.println("📋 Admin requested pending messages: " + pendingMessages.size() + " messages waiting");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching pending admin messages: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Admin responds to a client message
     */
    @PostMapping("/admin/respond/{messageId}")
    public ResponseEntity<?> respondToMessage(
            @PathVariable UUID messageId,
            @RequestBody Map<String, String> request) {
        try {
            String adminResponse = request.get("response");
            if (adminResponse == null || adminResponse.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "Response cannot be empty");
                        }});
            }

            ChatMessage updatedMessage = chatService.respondToMessage(messageId, adminResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("messageId", messageId);
            response.put("message", updatedMessage.getMessage());
            response.put("response", updatedMessage.getResponse());
            response.put("responseType", updatedMessage.getResponseType());
            response.put("responseTime", updatedMessage.getResponseTime());
            
            System.out.println("✅ Admin response sent to client");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error processing admin response: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get AI configuration status
     */
    @GetMapping("/ai/status")
    public ResponseEntity<?> getAIStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("aiEnabled", aiCustomerSupportService.isGeminiConfigured());
            status.put("configuration", aiCustomerSupportService.getConfigurationStatus());
            status.put("message", "AI-Driven Customer Support is " + 
                    (aiCustomerSupportService.isGeminiConfigured() ? "ENABLED" : "using fallback responses"));
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("❌ Error getting AI status: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test AI response generation
     */
    @PostMapping("/ai/test")
    public ResponseEntity<?> testAIResponse(@RequestBody Map<String, String> request) {
        try {
            String testMessage = request.get("message");
            if (testMessage == null || testMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "Message cannot be empty");
                        }});
            }

            String aiResponse = aiCustomerSupportService.generateAIResponse(testMessage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("input", testMessage);
            response.put("output", aiResponse);
            response.put("aiEnabled", aiCustomerSupportService.isGeminiConfigured());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error testing AI response: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

}

