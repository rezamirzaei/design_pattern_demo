package com.smarthome.pattern.creational.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Product - Smart Light
 */
public class SmartLight implements Device {
    private static final Logger log = LoggerFactory.getLogger(SmartLight.class);
    private final String name;
    private final String location;
    private boolean isOn = false;
    private int brightness = 100; // 0-100
    private String color = "#FFFFFF"; // RGB hex

    public SmartLight(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void turnOn() {
        isOn = true;
        log.info("Light '{}' in {} turned ON (brightness: {}%, color: {})",
                name, location, brightness, color);
    }

    @Override
    public void turnOff() {
        isOn = false;
        log.info("Light '{}' in {} turned OFF", name, location);
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("Light %s: %s, Brightness: %d%%, Color: %s",
                name, isOn ? "ON" : "OFF", brightness, color);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Smart Light [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        return isOn ? (brightness / 100.0) * 10.0 : 0.0; // Max 10W LED
    }

    @Override
    public void operate(String command) {
        String[] parts = command.split(":");
        switch (parts[0].toUpperCase()) {
            case "BRIGHTNESS":
                setBrightness(Integer.parseInt(parts[1]));
                break;
            case "COLOR":
                setColor(parts[1]);
                break;
            case "TOGGLE":
                if (isOn) turnOff(); else turnOn();
                break;
            default:
                log.warn("Unknown command: {}", command);
        }
    }

    public void setBrightness(int brightness) {
        this.brightness = Math.max(0, Math.min(100, brightness));
        log.info("Light '{}' brightness set to {}%", name, this.brightness);
    }

    public void setColor(String color) {
        this.color = color;
        log.info("Light '{}' color set to {}", name, color);
    }

    public int getBrightness() {
        return brightness;
    }

    public String getColor() {
        return color;
    }
}
