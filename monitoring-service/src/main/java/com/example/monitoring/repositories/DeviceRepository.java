package com.example.monitoring.repositories;

import com.example.monitoring.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    
    // Find device by name
    Optional<Device> findByName(String name);
}

