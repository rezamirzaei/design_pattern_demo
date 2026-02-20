package com.smarthome.service;

import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.RoomEntity;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.RoomRepository;
import com.smarthome.web.viewmodel.DeviceView;
import com.smarthome.web.viewmodel.RoomView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages Room CRUD and device-to-room assignment.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    public RoomService(RoomRepository roomRepository,
                       DeviceRepository deviceRepository,
                       DeviceService deviceService) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
    }

    @Transactional(readOnly = true)
    public List<RoomView> getRoomViews() {
        return roomRepository.findAll().stream()
                .sorted(Comparator.comparing(RoomEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getRoomNames() {
        List<String> rooms = roomRepository.findAll().stream()
                .map(RoomEntity::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return rooms.isEmpty() ? deviceService.getLocations() : rooms;
    }

    @Transactional
    public RoomView createRoom(String name, String floor, String roomType) {
        String normalized = ServiceUtils.requireText(name, "Room name is required");
        roomRepository.findByName(normalized).ifPresent(r -> {
            throw new IllegalArgumentException("Room already exists: " + normalized);
        });

        RoomEntity room = new RoomEntity();
        room.setName(normalized);
        room.setFloor(ServiceUtils.blankToNull(floor));
        room.setRoomType(ServiceUtils.blankToNull(roomType));
        roomRepository.save(room);
        return toView(room);
    }

    @Transactional
    public RoomView assignDeviceToRoom(Long roomId, String deviceId) {
        if (roomId == null) throw new IllegalArgumentException("Room id is required");
        String dId = ServiceUtils.requireText(deviceId, "Device id is required");

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        DeviceEntity device = deviceService.findOrThrow(dId);
        device.setLocation(room.getName());
        deviceRepository.save(device);

        boolean already = room.getDevices().stream().anyMatch(d -> dId.equals(d.getId()));
        if (!already) room.getDevices().add(device);
        roomRepository.save(room);
        return toView(room);
    }

    @Transactional
    public RoomView unassignDeviceFromRoom(Long roomId, String deviceId) {
        if (roomId == null) throw new IllegalArgumentException("Room id is required");
        String dId = ServiceUtils.requireText(deviceId, "Device id is required");

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        boolean removed = room.getDevices().removeIf(d -> dId.equals(d.getId()));
        if (removed) {
            DeviceEntity device = deviceService.findOrThrow(dId);
            device.setLocation("Unassigned");
            deviceRepository.save(device);
            roomRepository.save(room);
        }
        return toView(room);
    }

    private RoomView toView(RoomEntity room) {
        List<DeviceView> devices = Optional.ofNullable(room.getDevices()).orElse(List.of()).stream()
                .sorted(Comparator.comparing(DeviceEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(deviceService::toView)
                .toList();
        return new RoomView(room.getId(), room.getName(), room.getFloor(),
                room.getRoomType(), devices.size(), devices);
    }
}

