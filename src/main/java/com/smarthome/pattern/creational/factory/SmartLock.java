package com.smarthome.pattern.creational.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Product - Smart Lock
 */
public class SmartLock implements Device {
    private static final Logger log = LoggerFactory.getLogger(SmartLock.class);
    private final String name;
    private final String location;
    private boolean isOn = true; // Lock is always powered
    private boolean isLocked = true;
    private boolean autoLockEnabled = true;
    private int autoLockDelay = 30; // seconds

    public SmartLock(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void turnOn() {
        isOn = true;
        log.info("Lock '{}' in {} activated", name, location);
    }

    @Override
    public void turnOff() {
        // Locks don't really turn off, but we can disable auto-lock
        autoLockEnabled = false;
        log.info("Lock '{}' in {} auto-lock disabled", name, location);
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("Lock %s: %s, Auto-lock: %s (%ds delay)",
                name, isLocked ? "LOCKED" : "UNLOCKED",
                autoLockEnabled ? "ON" : "OFF", autoLockDelay);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Smart Lock [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        return 0.5; // Minimal power for smart lock
    }

    @Override
    public void operate(String command) {
        String[] parts = command.split(":");
        switch (parts[0].toUpperCase()) {
            case "LOCK":
                lock();
                break;
            case "UNLOCK":
                unlock();
                break;
            case "AUTOLOCK":
                setAutoLock(Boolean.parseBoolean(parts[1]));
                break;
            case "DELAY":
                setAutoLockDelay(Integer.parseInt(parts[1]));
                break;
            default:
                log.warn("Unknown command: {}", command);
        }
    }

    public void lock() {
        isLocked = true;
        log.info("Lock '{}' LOCKED", name);
    }

    public void unlock() {
        isLocked = false;
        log.info("Lock '{}' UNLOCKED", name);
        if (autoLockEnabled) {
            log.info("Lock '{}' will auto-lock in {} seconds", name, autoLockDelay);
        }
    }

    public void setAutoLock(boolean enabled) {
        this.autoLockEnabled = enabled;
        log.info("Lock '{}' auto-lock {}", name, enabled ? "enabled" : "disabled");
    }

    public void setAutoLockDelay(int seconds) {
        this.autoLockDelay = seconds;
        log.info("Lock '{}' auto-lock delay set to {} seconds", name, seconds);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isAutoLockEnabled() {
        return autoLockEnabled;
    }
}
