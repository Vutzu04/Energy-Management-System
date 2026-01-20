package com.example.customersupport.services;

import com.example.customersupport.entities.SupportRule;
import com.example.customersupport.repositories.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RuleEngine {

    @Autowired
    private RuleRepository ruleRepository;

    /**
     * Try to match user message against defined rules
     * Returns the response if a rule matches, null otherwise
     */
    public RuleMatchResult matchRule(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Get all active rules sorted by priority (highest first)
        List<SupportRule> rules = ruleRepository.findByEnabledOrderByPriorityDesc(true);

        for (SupportRule rule : rules) {
            // Try keyword matching
            if (rule.getKeyword() != null && 
                lowerMessage.contains(rule.getKeyword().toLowerCase())) {
                
                System.out.println("✅ Rule matched: " + rule.getKeyword());
                return new RuleMatchResult(
                    rule.getId(),
                    rule.getResponse(),
                    rule.getKeyword()
                );
            }

            // Try regex pattern matching
            if (rule.getPattern() != null && 
                !rule.getPattern().isEmpty()) {
                
                try {
                    if (lowerMessage.matches(rule.getPattern().toLowerCase())) {
                        System.out.println("✅ Pattern matched: " + rule.getPattern());
                        return new RuleMatchResult(
                            rule.getId(),
                            rule.getResponse(),
                            rule.getPattern()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Invalid regex pattern: " + rule.getPattern() + " - " + e.getMessage());
                }
            }
        }

        return null;  // No match found
    }

    /**
     * Result object for rule matching
     */
    public static class RuleMatchResult {
        public UUID ruleId;
        public String response;
        public String matchedBy;

        public RuleMatchResult(UUID ruleId, String response, String matchedBy) {
            this.ruleId = ruleId;
            this.response = response;
            this.matchedBy = matchedBy;
        }
    }

}

