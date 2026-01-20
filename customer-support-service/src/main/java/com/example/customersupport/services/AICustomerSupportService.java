package com.example.customersupport.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * AI-Driven Customer Support Service
 * Uses Google Gemini API to generate intelligent responses
 * when no rule matches the user message
 */
@Service
public class AICustomerSupportService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.enabled:false}")
    private Boolean geminiEnabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    /**
     * Generate AI response for user message using Gemini API
     * Falls back to default message if API is not configured
     *
     * @param userMessage The user's message
     * @return AI-generated response
     */
    public String generateAIResponse(String userMessage) {
        System.out.println("\n🤖 Attempting to generate AI response...");
        System.out.println("   User Message: " + userMessage);

        // Check if Gemini is enabled and configured
        if (!geminiEnabled || geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.out.println("ℹ️  Gemini API not configured. Using fallback response.");
            return getFallbackResponse(userMessage);
        }

        try {
            return callGeminiAPI(userMessage);
        } catch (Exception e) {
            System.err.println("❌ Error calling Gemini API: " + e.getMessage());
            System.out.println("Using fallback response...");
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * Call Google Gemini API to generate response
     */
    private String callGeminiAPI(String userMessage) throws Exception {
        System.out.println("📤 Calling Gemini API...");

        // Create request
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        // System prompt for energy management context
        String systemPrompt = "You are a helpful customer support assistant for an Energy Management System. " +
                "You help customers with questions about energy consumption, device management, billing, and system usage. " +
                "Keep responses concise (under 150 words) and helpful. If you don't know, ask the user to contact support.";

        part.put("text", systemPrompt + "\n\nUser: " + userMessage);
        content.put("parts", new Map[]{part});
        request.put("contents", new Map[]{content});

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        // Call API
        String urlWithKey = GEMINI_API_URL + "?key=" + geminiApiKey;
        String response = restTemplate.postForObject(urlWithKey, entity, String.class);

        // Parse response
        if (response != null) {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidates = rootNode.get("candidates");

            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content2 = firstCandidate.get("content");
                JsonNode parts = content2.get("parts");

                if (parts != null && parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String generatedText = firstPart.get("text").asText();

                    System.out.println("✅ AI Response generated successfully");
                    System.out.println("   Response: " + generatedText);
                    return generatedText;
                }
            }
        }

        System.err.println("❌ Failed to parse Gemini API response");
        return getFallbackResponse(userMessage);
    }

    /**
     * Fallback response generator (rule-based heuristics)
     * Used when Gemini API is not available
     */
    private String getFallbackResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Heuristic responses based on keywords
        if (lowerMessage.contains("energy") || lowerMessage.contains("consumption")) {
            return "Energy consumption tracking is available in your dashboard. " +
                    "You can view hourly, daily, and monthly consumption data for each device. " +
                    "Set up alerts for overconsumption limits.";
        }

        if (lowerMessage.contains("bill") || lowerMessage.contains("price") || lowerMessage.contains("cost")) {
            return "Billing information is typically available in your account settings. " +
                    "For specific billing questions, please contact our support team directly.";
        }

        if (lowerMessage.contains("device") || lowerMessage.contains("add") || lowerMessage.contains("connect")) {
            return "To add a device, go to Device Management in your dashboard. " +
                    "Click 'Add Device', enter the device details, and associate it with your account. " +
                    "Once added, you'll see real-time consumption data.";
        }

        if (lowerMessage.contains("alert") || lowerMessage.contains("notification") || lowerMessage.contains("overconsumption")) {
            return "Overconsumption alerts notify you when a device exceeds its maximum consumption limit. " +
                    "Set your device limits in the Device Settings. You'll receive instant notifications.";
        }

        if (lowerMessage.contains("help") || lowerMessage.contains("support") || lowerMessage.contains("issue")) {
            return "We're here to help! Please describe your issue in detail, and our support team will assist you shortly. " +
                    "For urgent issues, please use the priority support option.";
        }

        if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey")) {
            return "Hello! 👋 Welcome to Energy Management Support. How can we assist you today? " +
                    "Feel free to ask about energy tracking, device management, or any other questions.";
        }

        // Default fallback response
        return "Thank you for your inquiry. Our support team is reviewing your message and will provide " +
                "a detailed response shortly. In the meantime, please check our FAQ or documentation for quick answers.";
    }

    /**
     * Check if Gemini API is properly configured
     */
    public Boolean isGeminiConfigured() {
        return geminiEnabled && geminiApiKey != null && !geminiApiKey.isEmpty();
    }

    /**
     * Get configuration status
     */
    public Map<String, Object> getConfigurationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("geminiEnabled", geminiEnabled);
        status.put("geminiConfigured", isGeminiConfigured());
        status.put("fallbackEnabled", true);
        return status;
    }
}
