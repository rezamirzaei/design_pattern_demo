package com.smarthome.pattern.creational.factory;

/**
 * FACTORY METHOD PATTERN - Creator
 *
 * Concrete factories decide which concrete Device to create.
 */
public abstract class DeviceFactory {

    /**
     * Factory Method - implemented by subclasses.
     */
    public abstract Device createDevice(String name, String location);
}

