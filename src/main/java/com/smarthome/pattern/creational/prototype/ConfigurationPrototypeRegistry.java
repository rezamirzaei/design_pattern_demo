package com.smarthome.pattern.creational.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for prototype configurations
 * Stores and manages configuration templates that can be cloned
 */
public class ConfigurationPrototypeRegistry {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationPrototypeRegistry.class);
    private static final Map<String, DeviceConfiguration> prototypes = new HashMap<>();

    static {
        // Initialize with default presets
        registerPrototype("bright-light",
                DeviceConfiguration.createLightPreset("Bright Light", 100, "#FFFFFF"));
        registerPrototype("dim-light",
                DeviceConfiguration.createLightPreset("Dim Light", 30, "#FFE4B5"));
        registerPrototype("night-light",
                DeviceConfiguration.createLightPreset("Night Light", 10, "#FF6B6B"));
        registerPrototype("comfort-thermostat",
                DeviceConfiguration.createThermostatPreset("Comfort Mode", 22.0, "AUTO"));
        registerPrototype("eco-thermostat",
                DeviceConfiguration.createThermostatPreset("Eco Mode", 20.0, "ECO"));
        registerPrototype("security-camera",
                DeviceConfiguration.createCameraPreset("Security Camera", "1080p", true));
        registerPrototype("baby-monitor",
                DeviceConfiguration.createCameraPreset("Baby Monitor", "720p", false));
    }

    /**
     * Register a new prototype configuration
     */
    public static void registerPrototype(String key, DeviceConfiguration config) {
        prototypes.put(key, config);
        log.info("Registered prototype configuration: {}", key);
    }

    /**
     * Get a clone of a prototype configuration
     */
    public static DeviceConfiguration getClone(String key) {
        DeviceConfiguration prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown prototype: " + key);
        }
        return prototype.clone();
    }

    /**
     * Get all available prototype keys
     */
    public static Iterable<String> getAvailablePrototypes() {
        return prototypes.keySet();
    }

    /**
     * Check if a prototype exists
     */
    public static boolean hasPrototype(String key) {
        return prototypes.containsKey(key);
    }
}
