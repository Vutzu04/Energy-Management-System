package com.example.simulator.services;

import com.example.simulator.config.RabbitMQConfig;
import com.example.simulator.dtos.EnergyReadingMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class SimulatorService {

    private final RabbitTemplate rabbitTemplate;
    private boolean simulatorEnabled;

    // Device IDs - using the ones already created in the system
    private static final String[] DEVICE_IDS = {
        "f2f00df5-c2d1-49fa-8077-427de8d4b38d", // SmartTV
        "2feb3592-ce65-4316-8487-bce6b61dbe9e"  // Espressor Sage
    };

    private static final String[] DEVICE_NAMES = {
        "SmartTV", "Espressor Sage"
    };

    // For realistic consumption patterns
    private final Random random = new Random();
    private final Map<String, Double> baseLoads = new HashMap<>();
    private final Map<String, Double> lastValues = new HashMap<>();

    public SimulatorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.simulatorEnabled = true;

        // Initialize base loads for each device
        baseLoads.put(DEVICE_NAMES[0], 120.0);  // SmartTV: medium variable
        baseLoads.put(DEVICE_NAMES[1], 145.0);  // Espressor Sage: high when running

        // Initialize last values
        for (String name : DEVICE_NAMES) {
            lastValues.put(name, baseLoads.get(name));
        }
    }

    @Scheduled(fixedRate = 600000) // 10 minutes
    public void generateAndPublishReading() {
        if (!simulatorEnabled) {
            return;
        }

        // Generate a reading for each device
        for (int i = 0; i < DEVICE_IDS.length; i++) {
            generateDeviceReading(DEVICE_IDS[i], DEVICE_NAMES[i]);
        }
    }

    private void generateDeviceReading(String deviceIdStr, String deviceName) {
        try {
            UUID deviceId = UUID.fromString(deviceIdStr);
            LocalDateTime timestamp = LocalDateTime.now();

            // Generate realistic consumption value based on time of day
            Double value = generateRealisticConsumption(timestamp, deviceName);

            // Create message
            EnergyReadingMessage message = new EnergyReadingMessage(deviceId, timestamp, value);

            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.ENERGY_DATA_ROUTING_KEY,
                message
            );

            System.out.println("📤 PUBLISHED: " + deviceName + " (" + String.format("%.2f", value) + " kWh) at " + timestamp);

        } catch (Exception e) {
            System.err.println("❌ Error publishing message for device " + deviceName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double generateRealisticConsumption(LocalDateTime timestamp, String deviceName) {
        int hour = timestamp.getHour();
        double baseLoad = baseLoads.get(deviceName);
        double lastValue = lastValues.get(deviceName);

        double consumption;

        // Time-based patterns
        if (hour >= 0 && hour < 6) {
            // Night: Low consumption
            consumption = baseLoad * 0.4 + random.nextDouble() * 5;
        } else if (hour >= 6 && hour < 12) {
            // Morning: Moderate consumption
            consumption = baseLoad * 0.7 + random.nextDouble() * 10;
        } else if (hour >= 12 && hour < 18) {
            // Afternoon: Medium consumption
            consumption = baseLoad * 0.8 + random.nextDouble() * 12;
        } else {
            // Evening: High consumption
            consumption = baseLoad * 1.2 + random.nextDouble() * 15;
        }

        // Add some smoothing (don't jump too much from last value)
        consumption = (lastValue * 0.7) + (consumption * 0.3);

        // Ensure non-negative
        consumption = Math.max(0, consumption);

        // Store for next iteration
        lastValues.put(deviceName, consumption);

        return consumption;
    }

    public String getStatus() {
        return simulatorEnabled ? "RUNNING" : "STOPPED";
    }

    public void startSimulator() {
        simulatorEnabled = true;
        System.out.println("🚀 Simulator STARTED");
    }

    public void stopSimulator() {
        simulatorEnabled = false;
        System.out.println("⏹️  Simulator STOPPED");
    }

    /**
     * Send a manual energy reading (for testing purposes)
     */
    public void sendManualReading(String deviceIdStr, Double consumptionValue) {
        try {
            UUID deviceId = UUID.fromString(deviceIdStr);
            LocalDateTime timestamp = LocalDateTime.now();

            // Find the device name
            String deviceName = "Unknown";
            for (int i = 0; i < DEVICE_IDS.length; i++) {
                if (DEVICE_IDS[i].equals(deviceIdStr)) {
                    deviceName = DEVICE_NAMES[i];
                    break;
                }
            }

            // Create message
            EnergyReadingMessage message = new EnergyReadingMessage(deviceId, timestamp, consumptionValue);

            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.ENERGY_DATA_ROUTING_KEY,
                message
            );

            System.out.println("📤 MANUAL READING PUBLISHED: " + deviceName + " (" + String.format("%.2f", consumptionValue) + " kWh) at " + timestamp);
        } catch (Exception e) {
            System.err.println("❌ Error sending manual reading: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
