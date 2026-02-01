package com.smarthome.pattern.creational.abstractfactory;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SmartThings ecosystem sensor
 */
public class SmartThingsSensor implements Device {
    private static final Logger log = LoggerFactory.getLogger(SmartThingsSensor.class);
    private final String name;
    private final String location;
    private boolean isOn = false;
    private boolean motionDetected = false;
    private double temperature = 22.0;
    private double humidity = 45.0;

    public SmartThingsSensor(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void turnOn() {
        isOn = true;
        log.info("[SmartThings] Sensor '{}' activated", name);
    }

    @Override
    public void turnOff() {
        isOn = false;
        log.info("[SmartThings] Sensor '{}' deactivated", name);
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("[SmartThings] Sensor %s: Motion=%s, Temp=%.1f°C, Humidity=%.1f%%",
                name, motionDetected, temperature, humidity);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("SmartThings Sensor [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        return isOn ? 0.5 : 0.0;
    }

    @Override
    public void operate(String command) {
        log.info("[SmartThings] Sensor '{}' received command: {}", name, command);
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(boolean motionDetected) {
        this.motionDetected = motionDetected;
    }
}
