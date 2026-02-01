package com.smarthome.pattern.creational.factory;

/**
 * Concrete Factory - Camera Factory
 */
public class CameraFactory extends DeviceFactory {

    @Override
    public Device createDevice(String name, String location) {
        return new SmartCamera(name, location);
    }
}
