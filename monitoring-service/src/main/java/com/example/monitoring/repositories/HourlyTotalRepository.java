package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyTotalRepository extends JpaRepository<HourlyTotal, UUID> {
    
    // Find hourly total for a device in a specific hour
    Optional<HourlyTotal> findByDeviceIdAndHour(UUID deviceId, LocalDateTime hour);
    
    // Find all hourly totals for a device on a specific date
    @Query("SELECT ht FROM HourlyTotal ht WHERE ht.deviceId = :deviceId " +
           "AND DATE(ht.hour) = DATE(:date) ORDER BY ht.hour ASC")
    List<HourlyTotal> findByDeviceIdAndDate(
        @Param("deviceId") UUID deviceId,
        @Param("date") LocalDateTime date
    );
    
    // Find hourly totals in a time range
    List<HourlyTotal> findByDeviceIdAndHourBetween(
        UUID deviceId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
}

