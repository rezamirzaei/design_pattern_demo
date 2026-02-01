package com.smarthome.pattern.creational.abstractfactory;

import com.smarthome.pattern.creational.factory.Device;

/**
 * ABSTRACT FACTORY PATTERN - Abstract Factory
 *
 * Creates families of related devices for a given ecosystem (e.g., SmartThings, HomeKit).
 */
public interface SmartDeviceAbstractFactory {
    Device createLight(String name, String location);

    Device createThermostat(String name, String location);

    Device createLock(String name, String location);

    Device createSensor(String name, String location);

    String getEcosystemName();
}

