package com.smarthome.pattern.structural.composite;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaf - Individual device wrapper
 * Represents a single device in the composite structure
 */
public class SingleDevice implements DeviceComponent {
    private static final Logger log = LoggerFactory.getLogger(SingleDevice.class);
    private final Device device;
    private final String name;

    public SingleDevice(String name, Device device) {
        this.name = name;
        this.device = device;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void turnOn() {
        device.turnOn();
    }

    @Override
    public void turnOff() {
        device.turnOff();
    }

    @Override
    public double getPowerConsumption() {
        return device.getPowerConsumption();
    }

    @Override
    public String getStatus() {
        return device.getStatus();
    }

    @Override
    public int getDeviceCount() {
        return 1;
    }

    @Override
    public void printStructure(String indent) {
        log.info("{}- {} [{}]", indent, name, device.isOn() ? "ON" : "OFF");
    }

    public Device getDevice() {
        return device;
    }
}
