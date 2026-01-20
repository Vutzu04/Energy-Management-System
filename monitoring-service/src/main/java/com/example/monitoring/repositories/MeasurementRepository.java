package com.example.monitoring.repositories;

import com.example.monitoring.entities.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {
    
    // Find measurements for a specific device in a time range
    List<Measurement> findByDeviceIdAndTimestampBetween(
        UUID deviceId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    // Find measurements for a specific device on a specific date
    @Query("SELECT m FROM Measurement m WHERE m.deviceId = :deviceId " +
           "AND DATE(m.timestamp) = DATE(:date)")
    List<Measurement> findByDeviceIdAndDate(
        @Param("deviceId") UUID deviceId,
        @Param("date") LocalDateTime date
    );
}

