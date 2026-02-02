package com.smarthome.pattern.creational.singleton;

import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.behavioral.observer.DeviceObserver;
import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SINGLETON PATTERN
 *
 * Intent: Ensure a class only has one instance and provide a global point of access to it.
 *
 * Smart Home Application: The HomeController is the central coordinator for all smart
 * home devices. There should only be one controller managing all devices, ensuring
 * consistent state and coordination across the entire home.
 *
 * Implementation: Enum-based singleton (thread-safe and serialization-safe)
 */
public enum HomeController {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final Map<String, Device> deviceRegistry = new ConcurrentHashMap<>();
    private final List<DeviceObserver> observers = new ArrayList<>();
    private HomeMode homeMode = HomeMode.NORMAL; // NORMAL, AWAY, NIGHT, VACATION

    /**
     * Register a device with the central controller
     */
    public void registerDevice(String deviceId, Device device) {
        deviceRegistry.put(deviceId, device);
        log.info("Device registered: {} ({})", deviceId, device.getDeviceInfo());
        notifyObservers("DEVICE_REGISTERED", deviceId);
    }

    /**
     * Unregister a device from the controller
     */
    public void unregisterDevice(String deviceId) {
        Device removed = deviceRegistry.remove(deviceId);
        if (removed != null) {
            log.info("Device unregistered: {}", deviceId);
            notifyObservers("DEVICE_UNREGISTERED", deviceId);
        }
    }

    /**
     * Get a device by its ID
     */
    public Device getDevice(String deviceId) {
        return deviceRegistry.get(deviceId);
    }

    /**
     * Get all registered devices
     */
    public Map<String, Device> getAllDevices() {
        return new ConcurrentHashMap<>(deviceRegistry);
    }

    /**
     * Set the home mode (affects all devices)
     */
    public void setHomeMode(HomeMode mode) {
        HomeMode oldMode = this.homeMode;
        this.homeMode = mode;
        log.info("Home mode changed from {} to {}", oldMode, mode);
        notifyObservers("MODE_CHANGED", mode.name());
        applyModeToAllDevices(mode.name());
    }

    /**
     * Get the current home mode
     */
    public HomeMode getHomeModeEnum() {
        return homeMode;
    }

    /**
     * Add an observer to receive notifications
     */
    public void addObserver(DeviceObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer
     */
    public void removeObserver(DeviceObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of an event
     */
    private void notifyObservers(String eventType, String data) {
        for (DeviceObserver observer : observers) {
            observer.onDeviceEvent(eventType, data);
        }
    }

    /**
     * Apply mode-specific settings to all devices
     */
    private void applyModeToAllDevices(String mode) {
        switch (mode) {
            case "AWAY":
                deviceRegistry.values().forEach(device -> {
                    if (device.getDeviceInfo().contains("Light")) {
                        device.turnOff();
                    }
                });
                break;
            case "NIGHT":
                deviceRegistry.values().forEach(device -> {
                    if (device.getDeviceInfo().contains("Light")) {
                        device.turnOff();
                    }
                });
                break;
            case "VACATION":
                // Simulate presence with random light patterns
                log.info("Vacation mode: Presence simulation activated");
                break;
            default:
                log.info("Normal mode: All devices operating normally");
        }
    }

    /**
     * Get system status summary
     */
    public String getSystemStatus() {
        int totalDevices = deviceRegistry.size();
        long onDevices = deviceRegistry.values().stream()
                .filter(Device::isOn)
                .count();
        return String.format("Smart Home Status: Mode=%s, Total Devices=%d, Active=%d",
                homeMode, totalDevices, onDevices);
    }

    /**
     * Emergency shutdown - turn off all devices
     */
    public void emergencyShutdown() {
        log.warn("EMERGENCY SHUTDOWN INITIATED");
        deviceRegistry.values().forEach(Device::turnOff);
        notifyObservers("EMERGENCY_SHUTDOWN", "All devices turned off");
    }

    /**
     * Get a snapshot of all devices (for patterns like Iterator/Visitor/Facade)
     */
    public Map<String, Device> getDevicesSnapshot() {
        return new ConcurrentHashMap<>(deviceRegistry);
    }
}
