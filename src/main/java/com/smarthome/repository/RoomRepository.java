package com.smarthome.repository;

import com.smarthome.domain.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    Optional<RoomEntity> findByName(String name);
    List<RoomEntity> findByFloor(String floor);
    List<RoomEntity> findByRoomType(String roomType);
}
