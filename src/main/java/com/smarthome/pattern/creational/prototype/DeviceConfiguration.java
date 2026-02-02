package com.smarthome.pattern.creational.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * PROTOTYPE PATTERN
 *
 * Intent: Specify the kinds of objects to create using a prototypical instance,
 * and create new objects by copying this prototype.
 *
 * Smart Home Application: Device configurations can be complex. Instead of
 * configuring each device from scratch, we can clone existing configurations
 * as templates and modify them as needed.
 */
public class DeviceConfiguration implements Cloneable {
    private static final Logger log = LoggerFactory.getLogger(DeviceConfiguration.class);
    private String name;
    private String deviceType;
    private Map<String, Object> settings;
    private Map<String, String> schedules;
    private boolean isTemplate;

    public DeviceConfiguration() {
        this.settings = new HashMap<>();
        this.schedules = new HashMap<>();
        this.isTemplate = false;
    }

    public DeviceConfiguration(String name, String deviceType) {
        this();
        this.name = name;
        this.deviceType = deviceType;
    }

    public DeviceConfiguration(String name) {
        this(name, "GENERIC");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings == null ? new HashMap<>() : settings;
    }

    public Map<String, String> getSchedules() {
        return schedules;
    }

    public void setSchedules(Map<String, String> schedules) {
        this.schedules = schedules == null ? new HashMap<>() : schedules;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    /**
     * Clone this configuration (Prototype pattern)
     */
    @Override
    public DeviceConfiguration clone() {
        try {
            DeviceConfiguration cloned = (DeviceConfiguration) super.clone();
            // Deep copy the maps
            cloned.settings = new HashMap<>(this.settings);
            cloned.schedules = new HashMap<>(this.schedules);
            cloned.isTemplate = false; // Cloned config is not a template
            log.info("Configuration '{}' cloned", name);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    public void addSetting(String key, Object value) {
        settings.put(key, value);
    }

    public void setProperty(String key, Object value) {
        addSetting(key, value);
    }

    public void addSchedule(String eventName, String cronExpression) {
        schedules.put(eventName, cronExpression);
    }

    public Object getSetting(String key) {
        return settings.get(key);
    }

    public Object getProperty(String key) {
        return getSetting(key);
    }

    public Map<String, Object> getAllProperties() {
        return settings == null ? Map.of() : new HashMap<>(settings);
    }

    /**
     * Create a preset configuration for lights
     */
    public static DeviceConfiguration createLightPreset(String name, int brightness, String color) {
        DeviceConfiguration config = new DeviceConfiguration(name, "LIGHT");
        config.addSetting("brightness", brightness);
        config.addSetting("color", color);
        config.addSetting("transitionTime", 500); // ms
        config.isTemplate = true;
        return config;
    }

    /**
     * Create a preset configuration for thermostats
     */
    public static DeviceConfiguration createThermostatPreset(String name, double targetTemp, String mode) {
        DeviceConfiguration config = new DeviceConfiguration(name, "THERMOSTAT");
        config.addSetting("targetTemperature", targetTemp);
        config.addSetting("mode", mode);
        config.addSetting("fanSpeed", "AUTO");
        config.isTemplate = true;
        return config;
    }

    /**
     * Create a preset configuration for cameras
     */
    public static DeviceConfiguration createCameraPreset(String name, String resolution, boolean motionDetection) {
        DeviceConfiguration config = new DeviceConfiguration(name, "CAMERA");
        config.addSetting("resolution", resolution);
        config.addSetting("motionDetection", motionDetection);
        config.addSetting("nightVision", true);
        config.addSetting("recordingQuality", "HIGH");
        config.isTemplate = true;
        return config;
    }

    // Add getPresetName for Service compatibility
    public String getPresetName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("DeviceConfiguration[name=%s, type=%s, settings=%d, isTemplate=%s]",
                name, deviceType, settings.size(), isTemplate);
    }
}
