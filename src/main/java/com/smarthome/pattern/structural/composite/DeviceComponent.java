package com.smarthome.pattern.structural.composite;

/**
 * COMPOSITE PATTERN
 *
 * Intent: Compose objects into tree structures to represent part-whole hierarchies.
 * Composite lets clients treat individual objects and compositions uniformly.
 *
 * Smart Home Application: Devices can be grouped into rooms, and rooms can be
 * grouped into zones (floors, areas). Operations like "turn off all lights"
 * can be applied to individual devices, rooms, or entire zones uniformly.
 */

/**
 * Component interface - common interface for both leaf and composite
 */
public interface DeviceComponent {

    /**
     * Get the name of this component
     */
    String getName();

    /**
     * Turn on all devices in this component
     */
    void turnOn();

    /**
     * Turn off all devices in this component
     */
    void turnOff();

    /**
     * Get total power consumption
     */
    double getPowerConsumption();

    /**
     * Get status of this component
     */
    String getStatus();

    /**
     * Get the number of devices in this component
     */
    int getDeviceCount();

    /**
     * Print structure (for debugging)
     */
    void printStructure(String indent);
}
