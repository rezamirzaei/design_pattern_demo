package com.smarthome.pattern.creational.factory;

/**
 * Concrete Factory - Light Factory
 */
public class LightFactory extends DeviceFactory {
    
    @Override
    public Device createDevice(String name, String location) {
        return new SmartLight(name, location);
    }
}
