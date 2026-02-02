package com.smarthome.repository;

import com.smarthome.domain.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    List<DeviceEntity> findByRoomId(Long roomId);
    List<DeviceEntity> findByStatus(String status);
    List<DeviceEntity> findByDeviceType(String deviceType);
    List<DeviceEntity> findByIsOnline(Boolean isOnline);
    List<DeviceEntity> findByLocationIgnoreCase(String location);
}
