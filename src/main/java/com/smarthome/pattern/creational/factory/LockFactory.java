package com.smarthome.pattern.creational.factory;

/**
 * Concrete Factory - Lock Factory
 */
public class LockFactory extends DeviceFactory {
    
    @Override
    public Device createDevice(String name, String location) {
        return new SmartLock(name, location);
    }
}
