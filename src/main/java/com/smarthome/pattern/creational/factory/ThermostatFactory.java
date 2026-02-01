package com.smarthome.pattern.creational.factory;

/**
 * Concrete Factory - Thermostat Factory
 */
public class ThermostatFactory extends DeviceFactory {

    @Override
    public Device createDevice(String name, String location) {
        return new SmartThermostat(name, location);
    }
}
