package com.smarthome.repository;

import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    List<DeviceEntity> findByLocation(String location);
    List<DeviceEntity> findByType(DeviceType type);
    List<DeviceEntity> findByIsOn(Boolean isOn);
    List<DeviceEntity> findByLocationIgnoreCase(String location);
}
