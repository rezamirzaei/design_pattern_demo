package com.smarthome.pattern.structural.decorator;

import com.smarthome.pattern.creational.factory.Device;

/**
 * DECORATOR PATTERN
 * 
 * Intent: Attach additional responsibilities to an object dynamically.
 * Decorators provide a flexible alternative to subclassing for extending functionality.
 * 
 * Smart Home Application: We can add features to devices dynamically:
 * - Logging: Log all device operations
 * - Security: Add authentication before operations
 * - Caching: Cache device status to reduce API calls
 * - Monitoring: Track usage statistics
 */

/**
 * Base Decorator - implements Device and wraps another Device
 */
public abstract class DeviceDecorator implements Device {
    protected final Device wrappedDevice;

    public DeviceDecorator(Device device) {
        this.wrappedDevice = device;
    }

    @Override
    public void turnOn() {
        wrappedDevice.turnOn();
    }

    @Override
    public void turnOff() {
        wrappedDevice.turnOff();
    }

    @Override
    public boolean isOn() {
        return wrappedDevice.isOn();
    }

    @Override
    public String getStatus() {
        return wrappedDevice.getStatus();
    }

    @Override
    public String getDeviceInfo() {
        return wrappedDevice.getDeviceInfo();
    }

    @Override
    public double getPowerConsumption() {
        return wrappedDevice.getPowerConsumption();
    }

    @Override
    public void operate(String command) {
        wrappedDevice.operate(command);
    }
}
