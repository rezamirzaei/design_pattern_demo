package com.smarthome.repository;

import com.smarthome.domain.DeviceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    List<DeviceEntity> findByLocationIgnoreCase(String location);
}

