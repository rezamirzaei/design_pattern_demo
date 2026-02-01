package com.smarthome.pattern.creational.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * PROTOTYPE PATTERN - Prototype
 *
 * Represents a configurable template for a device that can be cloned.
 */
public class DeviceConfiguration implements Cloneable {
    private final String presetName;
    private final String deviceType;
    private final Map<String, Object> settings;

    private DeviceConfiguration(String presetName, String deviceType, Map<String, Object> settings) {
        this.presetName = presetName;
        this.deviceType = deviceType;
        this.settings = new HashMap<>(settings);
    }

    public static DeviceConfiguration createLightPreset(String presetName, int brightness, String colorHex) {
        return new DeviceConfiguration(
                presetName,
                "LIGHT",
                Map.of(
                        "brightness", brightness,
                        "color", colorHex
                )
        );
    }

    public static DeviceConfiguration createThermostatPreset(String presetName, double targetTempC, String mode) {
        return new DeviceConfiguration(
                presetName,
                "THERMOSTAT",
                Map.of(
                        "targetTempC", targetTempC,
                        "mode", mode
                )
        );
    }

    public static DeviceConfiguration createCameraPreset(String presetName, String resolution, boolean motionDetectionEnabled) {
        return new DeviceConfiguration(
                presetName,
                "CAMERA",
                Map.of(
                        "resolution", resolution,
                        "motionDetection", motionDetectionEnabled
                )
        );
    }

    public String getPresetName() {
        return presetName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Map<String, Object> getSettings() {
        return Map.copyOf(settings);
    }

    @Override
    public DeviceConfiguration clone() {
        try {
            DeviceConfiguration copy = (DeviceConfiguration) super.clone();
            return new DeviceConfiguration(copy.presetName, copy.deviceType, copy.settings);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Prototype clone failed", e);
        }
    }
}

