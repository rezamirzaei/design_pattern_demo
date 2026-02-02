package com.smarthome.pattern.structural.flyweight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FLYWEIGHT PATTERN
 *
 * Intent: Use sharing to support large numbers of fine-grained objects efficiently.
 *
 * Smart Home Application: Device types share common data (icon, category, capabilities).
 * Instead of storing this data in every device instance, we share DeviceType flyweight
 * objects that contain the common intrinsic state.
 */

/**
 * Flyweight - Shared device type information
 * Contains intrinsic state (shared across all devices of this type)
 */
public class DeviceType {
    private static final Logger log = LoggerFactory.getLogger(DeviceType.class);
    // Intrinsic state - shared and immutable
    private final String typeName;
    private final String icon;
    private final String category;
    private final Set<String> capabilities;
    private final double averagePowerWatts;

    public DeviceType(String typeName, String icon, String category,
                      Set<String> capabilities, double averagePowerWatts) {
        this.typeName = typeName;
        this.icon = icon;
        this.category = category;
        this.capabilities = Set.copyOf(capabilities);
        this.averagePowerWatts = averagePowerWatts;
        log.debug("Created DeviceType flyweight: {}", typeName);
    }

    public String getTypeName() {
        return typeName;
    }

    public String getIcon() {
        return icon;
    }

    public String getCategory() {
        return category;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public double getAveragePowerWatts() {
        return averagePowerWatts;
    }

    /**
     * Operation using both intrinsic and extrinsic state
     */
    public void displayInfo(String deviceName, String location) {
        // deviceName and location are extrinsic state (passed in)
        log.info("{} {} at {} - Category: {}, Power: {}W, Capabilities: {}",
                icon, deviceName, location, category, averagePowerWatts, capabilities);
    }

    public boolean hasCapability(String capability) {
        return capabilities.contains(capability);
    }
}

/**
 * Flyweight Factory - Creates and manages flyweight objects
 */
class DeviceTypeFactory {
    private static final Logger log = LoggerFactory.getLogger(DeviceTypeFactory.class);
    private static final Map<String, DeviceType> deviceTypes = new HashMap<>();

    static {
        // Pre-create common device types
        deviceTypes.put("LIGHT", new DeviceType(
                "LIGHT", "💡", "Lighting",
                Set.of("ON_OFF", "BRIGHTNESS", "COLOR"),
                10.0
        ));
        deviceTypes.put("THERMOSTAT", new DeviceType(
                "THERMOSTAT", "🌡️", "Climate",
                Set.of("ON_OFF", "TEMPERATURE", "MODE", "SCHEDULE"),
                100.0
        ));
        deviceTypes.put("CAMERA", new DeviceType(
                "CAMERA", "📷", "Security",
                Set.of("ON_OFF", "RECORD", "MOTION_DETECT", "NIGHT_VISION"),
                8.0
        ));
        deviceTypes.put("LOCK", new DeviceType(
                "LOCK", "🔒", "Security",
                Set.of("LOCK", "UNLOCK", "AUTO_LOCK", "HISTORY"),
                0.5
        ));
        deviceTypes.put("SENSOR", new DeviceType(
                "SENSOR", "📊", "Monitoring",
                Set.of("TEMPERATURE", "HUMIDITY", "MOTION", "LIGHT_LEVEL"),
                0.2
        ));
        deviceTypes.put("SPEAKER", new DeviceType(
                "SPEAKER", "🔊", "Entertainment",
                Set.of("ON_OFF", "VOLUME", "PLAY", "PAUSE"),
                15.0
        ));
        deviceTypes.put("TV", new DeviceType(
                "TV", "📺", "Entertainment",
                Set.of("ON_OFF", "VOLUME", "CHANNEL", "INPUT"),
                100.0
        ));
        deviceTypes.put("PLUG", new DeviceType(
                "PLUG", "🔌", "Power",
                Set.of("ON_OFF", "POWER_MONITORING", "SCHEDULE"),
                1.0
        ));
    }

    /**
     * Get or create a device type flyweight
     */
    public static DeviceType getDeviceType(String type) {
        DeviceType deviceType = deviceTypes.get(type.toUpperCase());
        if (deviceType == null) {
            log.warn("Unknown device type: {}, creating generic", type);
            deviceType = new DeviceType(
                    type, "❓", "Unknown",
                    Set.of("ON_OFF"),
                    5.0
            );
            deviceTypes.put(type.toUpperCase(), deviceType);
        }
        return deviceType;
    }

    /**
     * Get count of flyweight objects (for demonstrating sharing)
     */
    public static int getFlyweightCount() {
        return deviceTypes.size();
    }

    /**
     * Get all available types
     */
    public static Set<String> getAvailableTypes() {
        return deviceTypes.keySet();
    }
}

/**
 * Context class - Uses flyweight with extrinsic state
 */
class DeviceInstance {
    private static final Logger log = LoggerFactory.getLogger(DeviceInstance.class);

    // Extrinsic state - unique to each instance
    private final String deviceId;
    private final String name;
    private final String location;
    private boolean isOn;

    // Flyweight reference - shared intrinsic state
    private final DeviceType type;

    public DeviceInstance(String deviceId, String name, String location, String typeName) {
        this.deviceId = deviceId;
        this.name = name;
        this.location = location;
        this.isOn = false;
        this.type = DeviceTypeFactory.getDeviceType(typeName);
        log.debug("Created DeviceInstance: {} using shared type: {}", name, typeName);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isOn() {
        return isOn;
    }

    public DeviceType getType() {
        return type;
    }

    public void displayInfo() {
        type.displayInfo(name, location);
    }

    public boolean supportsCapability(String capability) {
        return type.hasCapability(capability);
    }

    public double getEstimatedPower() {
        return isOn ? type.getAveragePowerWatts() : 0;
    }

    public void setOn(boolean on) {
        this.isOn = on;
    }
}
