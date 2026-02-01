package com.smarthome.pattern.creational.factory;

/**
 * FACTORY METHOD PATTERN - Product Interface
 *
 * Intent: Define an interface for creating an object, but let subclasses decide
 * which class to instantiate. Factory Method lets a class defer instantiation
 * to subclasses.
 *
 * Smart Home Application: Different device types (Light, Thermostat, Camera, Lock)
 * are created through their respective factories, allowing the system to be
 * extended with new device types without modifying existing code.
 */
public interface Device {

    /**
     * Turn the device on
     */
    void turnOn();

    /**
     * Turn the device off
     */
    void turnOff();

    /**
     * Check if device is on
     */
    boolean isOn();

    /**
     * Get device status information
     */
    String getStatus();

    /**
     * Get device information (type, model, etc.)
     */
    String getDeviceInfo();

    /**
     * Get current power consumption in watts
     */
    double getPowerConsumption();

    /**
     * Perform device-specific operation
     */
    void operate(String command);
}
