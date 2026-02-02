package com.smarthome.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Room Entity - Represents a room in the smart home
 */
@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String floor;

    @Column(name = "room_type")
    private String roomType; // LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, etc.

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private List<DeviceEntity> devices = new ArrayList<>();

    public RoomEntity() {}

    public RoomEntity(Long id, String name, String floor, String roomType, List<DeviceEntity> devices) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.roomType = roomType;
        this.devices = devices == null ? new ArrayList<>() : devices;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public List<DeviceEntity> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceEntity> devices) {
        this.devices = devices == null ? new ArrayList<>() : devices;
    }
}
