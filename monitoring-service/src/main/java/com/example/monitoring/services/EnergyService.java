package com.example.monitoring.services;

import com.example.monitoring.dtos.EnergyReadingMessage;
import com.example.monitoring.dtos.DeviceSyncMessage;
import com.example.monitoring.entities.Measurement;
import com.example.monitoring.entities.HourlyTotal;
import com.example.monitoring.entities.Device;
import com.example.monitoring.repositories.MeasurementRepository;
import com.example.monitoring.repositories.HourlyTotalRepository;
import com.example.monitoring.repositories.DeviceRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnergyService {
    
    @Autowired
    private MeasurementRepository measurementRepository;
    
    @Autowired
    private HourlyTotalRepository hourlyTotalRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * Process incoming energy reading and store it
     */
    @Transactional
    public void processEnergyReading(EnergyReadingMessage reading) {
        System.out.println("DEBUG: Processing energy reading for device: " + reading.getDeviceId() + 
                         ", value: " + reading.getMeasurementValue() + " kWh");
        
        // 1. Save raw measurement
        Measurement measurement = new Measurement();
        measurement.setDeviceId(reading.getDeviceId());
        measurement.setTimestamp(reading.getTimestamp());
        measurement.setEnergyConsumptionKwh(reading.getMeasurementValue());
        
        measurementRepository.save(measurement);
        System.out.println("DEBUG: Measurement saved for device: " + reading.getDeviceId());
        
        // 2. Check for overconsumption and send alert if needed
        checkAndPublishOverconsumptionAlert(reading.getDeviceId(), reading.getMeasurementValue());
        
        // 3. Aggregate to hourly total if needed
        aggregateToHourlyTotal(reading.getDeviceId(), reading.getTimestamp());
    }
    
    /**
     * Check if consumption exceeds device threshold and publish alert via WebSocket
     */
    private void checkAndPublishOverconsumptionAlert(UUID deviceId, Float consumptionValue) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (deviceOpt.isEmpty()) {
                return;
            }
            
            Device device = deviceOpt.get();
            Double threshold = device.getMaximumConsumptionValue();
            
            // Only alert if threshold is set
            if (threshold == null || threshold <= 0) {
                return;
            }
            
            float consumption = consumptionValue;
            float thresh = threshold.floatValue();
            
            // Check if overconsumption
            if (consumption > thresh) {
                System.out.println("🚨 OVERCONSUMPTION DETECTED: " + device.getName() + 
                                 " (" + consumption + " kWh > " + thresh + " kWh)");
                
                // Create alert message
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "OVERCONSUMPTION");
                alert.put("deviceId", deviceId.toString());
                alert.put("deviceName", device.getName());
                alert.put("consumption", consumption);
                alert.put("threshold", thresh);
                alert.put("timestamp", LocalDateTime.now());
                
                // Calculate severity based on how much over threshold
                String severity = "MEDIUM";
                if (consumption > thresh * 1.5f) {
                    severity = "CRITICAL";
                } else if (consumption > thresh * 1.2f) {
                    severity = "HIGH";
                }
                alert.put("severity", severity);
                
                // Publish to WebSocket service via RabbitMQ
                rabbitTemplate.convertAndSend(
                    "websocket_exchange",
                    "websocket.notification",
                    alert
                );
                
                System.out.println("✅ Overconsumption alert published to WebSocket service");
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking overconsumption: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Aggregate measurements into hourly totals
     */
    @Transactional
    public void aggregateToHourlyTotal(UUID deviceId, LocalDateTime timestamp) {
        // Get the hour (start of hour)
        LocalDateTime hourStart = timestamp.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hourEnd = hourStart.plusHours(1);
        
        // Find all measurements for this device in this hour
        List<Measurement> measurements = measurementRepository
                .findByDeviceIdAndTimestampBetween(deviceId, hourStart, hourEnd);
        
        // Calculate total
        float total = (float) measurements.stream()
                .mapToDouble(m -> m.getEnergyConsumptionKwh() != null ? m.getEnergyConsumptionKwh() : 0)
                .sum();
        
        System.out.println("DEBUG: Hourly total for device " + deviceId + " at " + hourStart + 
                         ": " + total + " kWh (from " + measurements.size() + " measurements)");
        
        // Save or update hourly total
        Optional<HourlyTotal> existingTotal = hourlyTotalRepository.findByDeviceIdAndHour(deviceId, hourStart);
        
        if (existingTotal.isPresent()) {
            existingTotal.get().setTotalConsumptionKwh(total);
            hourlyTotalRepository.save(existingTotal.get());
            System.out.println("DEBUG: Updated hourly total for device " + deviceId);
        } else {
            HourlyTotal hourlyTotal = new HourlyTotal();
            hourlyTotal.setDeviceId(deviceId);
            hourlyTotal.setHour(hourStart);
            hourlyTotal.setTotalConsumptionKwh(total);
            hourlyTotalRepository.save(hourlyTotal);
            System.out.println("DEBUG: Created new hourly total for device " + deviceId);
        }
    }
    
    /**
     * Handle device synchronization event
     */
    @Transactional
    public void handleDeviceSync(DeviceSyncMessage syncMessage) {
        System.out.println("DEBUG: Processing device sync event: " + syncMessage.getEventType() + 
                         " for device: " + syncMessage.getDeviceId());
        
        // Check if device already exists
        Optional<Device> existingDevice = deviceRepository.findById(syncMessage.getDeviceId());
        
        Device device;
        if (existingDevice.isPresent()) {
            device = existingDevice.get();
            device.setName(syncMessage.getName());
            device.setMaximumConsumptionValue(syncMessage.getMaximumConsumptionValue());
            System.out.println("DEBUG: Updated existing device: " + syncMessage.getDeviceId());
        } else {
            device = new Device();
            device.setId(syncMessage.getDeviceId());
            device.setName(syncMessage.getName());
            device.setMaximumConsumptionValue(syncMessage.getMaximumConsumptionValue());
            System.out.println("DEBUG: Created new device: " + syncMessage.getDeviceId());
        }
        
        deviceRepository.save(device);
    }
    
    /**
     * Get hourly totals for a device on a specific date
     */
    public List<HourlyTotal> getHourlyTotalsForDate(UUID deviceId, LocalDateTime date) {
        return hourlyTotalRepository.findByDeviceIdAndDate(deviceId, date);
    }
    
    /**
     * Get daily total consumption for a device
     */
    public Float getDailyTotal(UUID deviceId, LocalDateTime date) {
        List<Measurement> measurements = measurementRepository.findByDeviceIdAndDate(deviceId, date);
        return (float) measurements.stream()
                .mapToDouble(m -> m.getEnergyConsumptionKwh() != null ? m.getEnergyConsumptionKwh() : 0)
                .sum();
    }
}

