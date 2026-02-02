package com.smarthome.pattern.creational.abstractfactory;

import com.smarthome.pattern.creational.factory.Device;

/**
 * ABSTRACT FACTORY PATTERN
 * 
 * Intent: Provide an interface for creating families of related or dependent
 * objects without specifying their concrete classes.
 * 
 * Smart Home Application: Different smart home ecosystems (SmartThings, HomeKit, 
 * Google Home) have their own device families. The abstract factory allows creating
 * compatible device sets within each ecosystem.
 */
public interface SmartDeviceAbstractFactory {
    
    /**
     * Create a light device for this ecosystem
     */
    Device createLight(String name, String location);
    
    /**
     * Create a thermostat device for this ecosystem
     */
    Device createThermostat(String name, String location);
    
    /**
     * Create a lock device for this ecosystem
     */
    Device createLock(String name, String location);
    
    /**
     * Create a sensor device for this ecosystem
     */
    Device createSensor(String name, String location);
    
    /**
     * Get the ecosystem name
     */
    String getEcosystemName();
}
