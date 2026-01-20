package com.example.devicemanagement.services;

import com.example.devicemanagement.config.RabbitMQConfig;
import com.example.devicemanagement.dtos.AssociationRequest;
import com.example.devicemanagement.dtos.DeviceCreateDTO;
import com.example.devicemanagement.dtos.DeviceDTO;
import com.example.devicemanagement.dtos.DeviceSyncMessage;
import com.example.devicemanagement.dtos.DeviceUpdateDTO;
import com.example.devicemanagement.entities.Device;
import com.example.devicemanagement.entities.UserDevice;
import com.example.devicemanagement.repositories.DeviceRepository;
import com.example.devicemanagement.repositories.UserDeviceRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RabbitTemplate rabbitTemplate;

    public DeviceService(DeviceRepository deviceRepository, UserDeviceRepository userDeviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findAll() {
        return deviceRepository.findAll().stream()
                .map(device -> new DeviceDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue()))
                .collect(Collectors.toList());
    }

    public DeviceDTO findById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return new DeviceDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue());
    }

    public List<DeviceDTO> findByUserId(UUID userId) {
        List<Device> devices = userDeviceRepository.findDevicesByUserId(userId);
        return devices.stream()
                .map(device -> new DeviceDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue()))
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findByUsername(String username) {
        System.out.println("INFO: findByUsername called with username: " + username);
        List<Device> devices = userDeviceRepository.findDevicesByUsername(username);
        System.out.println("INFO: Found " + devices.size() + " devices for username: " + username);
        return devices.stream()
                .map(device -> new DeviceDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceDTO create(DeviceCreateDTO dto) {
        Device device = new Device();
        device.setName(dto.getName());
        device.setMaximumConsumptionValue(dto.getMaximumConsumptionValue());

        Device saved = deviceRepository.save(device);
        
        // Publish DEVICE_CREATED event to RabbitMQ
        try {
            DeviceSyncMessage syncMessage = new DeviceSyncMessage(
                saved.getId(),
                saved.getName(),
                saved.getMaximumConsumptionValue(),
                "DEVICE_CREATED"
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.SYNC_EVENT_ROUTING_KEY,
                syncMessage
            );
            System.out.println("📤 PUBLISHED: DEVICE_CREATED event for " + saved.getName());
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to publish DEVICE_CREATED event: " + e.getMessage());
        }
        
        return new DeviceDTO(saved.getId(), saved.getName(), saved.getMaximumConsumptionValue());
    }

    @Transactional
    public DeviceDTO update(UUID id, DeviceUpdateDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (dto.getName() != null && !dto.getName().isEmpty()) {
            device.setName(dto.getName());
        }

        if (dto.getMaximumConsumptionValue() != null) {
            device.setMaximumConsumptionValue(dto.getMaximumConsumptionValue());
        }

        Device updated = deviceRepository.save(device);
        return new DeviceDTO(updated.getId(), updated.getName(), updated.getMaximumConsumptionValue());
    }

    @Transactional
    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        
        String deviceName = device.getName();
        
        // Also delete associations
        userDeviceRepository.findAll().stream()
                .filter(ud -> ud.getDevice().getId().equals(id))
                .forEach(userDeviceRepository::delete);
        deviceRepository.deleteById(id);
        
        // Publish DEVICE_DELETED event to RabbitMQ
        try {
            DeviceSyncMessage syncMessage = new DeviceSyncMessage(
                id,
                deviceName,
                device.getMaximumConsumptionValue(),
                "DEVICE_DELETED"
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MONITORING_EXCHANGE,
                RabbitMQConfig.SYNC_EVENT_ROUTING_KEY,
                syncMessage
            );
            System.out.println("📤 PUBLISHED: DEVICE_DELETED event for " + deviceName);
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to publish DEVICE_DELETED event: " + e.getMessage());
        }
    }

    @Transactional
    public void associateDeviceToUser(AssociationRequest request) {
        UUID userId = request.getUserIdAsUUID();
        UUID deviceId = request.getDeviceIdAsUUID();
        String username = request.getUsername();  // Get username if provided
        
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (userDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId)) {
            throw new RuntimeException("Device already associated with user");
        }

        // Create association with both user_id and username for flexible lookups
        UserDevice userDevice = new UserDevice(userId, username, device);
        userDeviceRepository.save(userDevice);
    }

    @Transactional
    public void removeAssociation(UUID userId, UUID deviceId) {
        UserDevice userDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new RuntimeException("Association not found"));
        userDeviceRepository.delete(userDevice);
    }

    public List<?> getAllAssociations() {
        List<UserDevice> userDevices = userDeviceRepository.findAll();
        return userDevices.stream()
                .map(ud -> java.util.Map.of(
                    "userId", ud.getUserId().toString(),
                    "deviceId", ud.getDevice().getId().toString(),
                    "deviceName", ud.getDevice().getName(),
                    "maximumConsumptionValue", ud.getDevice().getMaximumConsumptionValue()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAssociationUsername(UUID userId, String oldUsername, String newUsername) {
        System.out.println("DEBUG: Updating associations for user " + userId + " from username '" + oldUsername + "' to '" + newUsername + "'");
        
        // Find all associations for this user with the old username
        List<UserDevice> associations = userDeviceRepository.findAll().stream()
                .filter(ud -> ud.getUserId().equals(userId) && oldUsername.equals(ud.getUsername()))
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: Found " + associations.size() + " associations to update");
        
        // Update each association with the new username
        for (UserDevice ud : associations) {
            ud.setUsername(newUsername);
            userDeviceRepository.save(ud);
            System.out.println("DEBUG: Updated association " + ud.getId() + " with new username: " + newUsername);
        }
    }

    @Transactional
    public void deleteAssociationsByUserId(UUID userId) {
        System.out.println("DEBUG: Deleting all associations for user " + userId);
        
        List<UserDevice> associations = userDeviceRepository.findAll().stream()
                .filter(ud -> ud.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: Found " + associations.size() + " associations to delete");
        
        for (UserDevice ud : associations) {
            userDeviceRepository.delete(ud);
            System.out.println("DEBUG: Deleted association " + ud.getId());
        }
    }
}

