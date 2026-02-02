package com.smarthome.pattern.creational.factory;

/**
 * FACTORY METHOD PATTERN - Creator (Abstract Factory)
 * 
 * The DeviceFactory declares the factory method that returns Device objects.
 * Subclasses will override this method to create specific device types.
 */
public abstract class DeviceFactory {
    
    /**
     * Factory Method - subclasses implement this to create specific devices
     */
    public abstract Device createDevice(String name, String location);
    
    /**
     * Template method that uses the factory method
     */
    public Device createAndRegisterDevice(String name, String location) {
        Device device = createDevice(name, location);
        // Could add registration logic here
        return device;
    }
}
