package com.smarthome.pattern.creational.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Product - Smart Camera
 */
public class SmartCamera implements Device {
    private static final Logger log = LoggerFactory.getLogger(SmartCamera.class);
    private final String name;
    private final String location;
    private boolean isOn = false;
    private boolean isRecording = false;
    private boolean motionDetectionEnabled = true;
    private String resolution = "1080p";

    public SmartCamera(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void turnOn() {
        isOn = true;
        log.info("Camera '{}' in {} turned ON (resolution: {}, motion detection: {})",
                name, location, resolution, motionDetectionEnabled ? "enabled" : "disabled");
    }

    @Override
    public void turnOff() {
        isOn = false;
        isRecording = false;
        log.info("Camera '{}' in {} turned OFF", name, location);
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("Camera %s: %s, Recording: %s, Motion Detection: %s, Resolution: %s",
                name, isOn ? "ON" : "OFF", isRecording ? "YES" : "NO",
                motionDetectionEnabled ? "ON" : "OFF", resolution);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Smart Camera [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        if (!isOn) return 0.0;
        return isRecording ? 8.0 : 5.0; // Watts
    }

    @Override
    public void operate(String command) {
        String[] parts = command.split(":");
        switch (parts[0].toUpperCase()) {
            case "RECORD":
                setRecording(Boolean.parseBoolean(parts[1]));
                break;
            case "MOTION":
                setMotionDetection(Boolean.parseBoolean(parts[1]));
                break;
            case "RESOLUTION":
                setResolution(parts[1]);
                break;
            case "SNAPSHOT":
                takeSnapshot();
                break;
            default:
                log.warn("Unknown command: {}", command);
        }
    }

    public void startRecording() {
        if (isOn) {
            isRecording = true;
            log.info("Camera '{}' started recording", name);
        }
    }

    public void stopRecording() {
        isRecording = false;
        log.info("Camera '{}' stopped recording", name);
    }

    public void setRecording(boolean recording) {
        if (recording) startRecording();
        else stopRecording();
    }

    public void setMotionDetection(boolean enabled) {
        this.motionDetectionEnabled = enabled;
        log.info("Camera '{}' motion detection {}", name, enabled ? "enabled" : "disabled");
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
        log.info("Camera '{}' resolution set to {}", name, resolution);
    }

    public void takeSnapshot() {
        if (isOn) {
            log.info("Camera '{}' snapshot taken", name);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isMotionDetectionEnabled() {
        return motionDetectionEnabled;
    }
}
