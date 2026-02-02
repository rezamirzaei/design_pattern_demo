package com.smarthome.pattern.behavioral.iterator;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * ITERATOR PATTERN
 *
 * Intent: Provide a way to access the elements of an aggregate object sequentially
 * without exposing its underlying representation.
 */

/**
 * Iterator interface
 */
public interface DeviceIterator {
    boolean hasNext();
    Device next();
    void reset();
}

/**
 * Aggregate interface
 */
interface DeviceCollection {
    DeviceIterator createIterator();
    DeviceIterator createIterator(String filterType, String filterValue);
    void addDevice(Device device, String room, String type);
    int size();
}

/**
 * Device with metadata for iteration
 */
class DeviceEntry {
    final Device device;
    final String room;
    final String type;

    DeviceEntry(Device device, String room, String type) {
        this.device = device;
        this.room = room;
        this.type = type;
    }
}

/**
 * Concrete Collection - Smart Home Device Collection
 */
class SmartHomeDeviceCollection implements DeviceCollection {
    private static final Logger log = LoggerFactory.getLogger(SmartHomeDeviceCollection.class);
    private final List<DeviceEntry> devices = new ArrayList<>();

    @Override
    public void addDevice(Device device, String room, String type) {
        devices.add(new DeviceEntry(device, room, type));
        log.debug("Added device to collection: {} in {} (type: {})",
                device.getDeviceInfo(), room, type);
    }

    @Override
    public DeviceIterator createIterator() {
        return new AllDevicesIterator(devices);
    }

    @Override
    public DeviceIterator createIterator(String filterType, String filterValue) {
        return switch (filterType.toUpperCase()) {
            case "ROOM" -> new RoomFilterIterator(devices, filterValue);
            case "TYPE" -> new TypeFilterIterator(devices, filterValue);
            case "STATUS" -> new StatusFilterIterator(devices, filterValue);
            default -> new AllDevicesIterator(devices);
        };
    }

    @Override
    public int size() {
        return devices.size();
    }

    List<DeviceEntry> getDevices() {
        return devices;
    }
}

/**
 * Concrete Iterator - All devices
 */
class AllDevicesIterator implements DeviceIterator {
    private final List<DeviceEntry> devices;
    private int position = 0;

    AllDevicesIterator(List<DeviceEntry> devices) {
        this.devices = devices;
    }

    @Override
    public boolean hasNext() {
        return position < devices.size();
    }

    @Override
    public Device next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more devices");
        }
        return devices.get(position++).device;
    }

    @Override
    public void reset() {
        position = 0;
    }
}

/**
 * Concrete Iterator - Filter by room
 */
class RoomFilterIterator implements DeviceIterator {
    private static final Logger log = LoggerFactory.getLogger(RoomFilterIterator.class);
    private final List<DeviceEntry> filteredDevices;
    private int position = 0;

    RoomFilterIterator(List<DeviceEntry> devices, String room) {
        this.filteredDevices = devices.stream()
                .filter(e -> e.room.equalsIgnoreCase(room))
                .toList();
        log.debug("Room filter '{}' found {} devices", room, filteredDevices.size());
    }

    @Override
    public boolean hasNext() {
        return position < filteredDevices.size();
    }

    @Override
    public Device next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more devices in this room");
        }
        return filteredDevices.get(position++).device;
    }

    @Override
    public void reset() {
        position = 0;
    }
}

/**
 * Concrete Iterator - Filter by type
 */
class TypeFilterIterator implements DeviceIterator {
    private static final Logger log = LoggerFactory.getLogger(TypeFilterIterator.class);
    private final List<DeviceEntry> filteredDevices;
    private int position = 0;

    TypeFilterIterator(List<DeviceEntry> devices, String type) {
        this.filteredDevices = devices.stream()
                .filter(e -> e.type.equalsIgnoreCase(type))
                .toList();
        log.debug("Type filter '{}' found {} devices", type, filteredDevices.size());
    }

    @Override
    public boolean hasNext() {
        return position < filteredDevices.size();
    }

    @Override
    public Device next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more devices of this type");
        }
        return filteredDevices.get(position++).device;
    }

    @Override
    public void reset() {
        position = 0;
    }
}

/**
 * Concrete Iterator - Filter by status (ON/OFF)
 */
class StatusFilterIterator implements DeviceIterator {
    private static final Logger log = LoggerFactory.getLogger(StatusFilterIterator.class);
    private final List<DeviceEntry> filteredDevices;
    private int position = 0;

    StatusFilterIterator(List<DeviceEntry> devices, String status) {
        boolean targetStatus = "ON".equalsIgnoreCase(status);
        this.filteredDevices = devices.stream()
                .filter(e -> e.device.isOn() == targetStatus)
                .toList();
        log.debug("Status filter '{}' found {} devices", status, filteredDevices.size());
    }

    @Override
    public boolean hasNext() {
        return position < filteredDevices.size();
    }

    @Override
    public Device next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more devices with this status");
        }
        return filteredDevices.get(position++).device;
    }

    @Override
    public void reset() {
        position = 0;
    }
}