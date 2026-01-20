package com.example.devicemanagement.repositories;

import com.example.devicemanagement.entities.Device;
import com.example.devicemanagement.entities.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    @Query("SELECT ud.device FROM UserDevice ud WHERE ud.userId = :userId")
    List<Device> findDevicesByUserId(@Param("userId") UUID userId);

    @Query("SELECT ud.device FROM UserDevice ud WHERE ud.username = :username")
    List<Device> findDevicesByUsername(@Param("username") String username);

    Optional<UserDevice> findByUserIdAndDeviceId(UUID userId, UUID deviceId);

    boolean existsByUserIdAndDeviceId(UUID userId, UUID deviceId);
}

