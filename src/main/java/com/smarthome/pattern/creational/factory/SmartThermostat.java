package com.smarthome.pattern.creational.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Product - Smart Thermostat
 */
public class SmartThermostat implements Device {
    private static final Logger log = LoggerFactory.getLogger(SmartThermostat.class);
    private final String name;
    private final String location;
    private boolean isOn = false;
    private double targetTemperature = 22.0; // Celsius
    private double currentTemperature = 20.0;
    private String mode = "AUTO"; // HEAT, COOL, AUTO, OFF

    public SmartThermostat(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void turnOn() {
        isOn = true;
        mode = "AUTO";
        log.info("Thermostat '{}' in {} turned ON (target: {}°C, current: {}°C)",
                name, location, targetTemperature, currentTemperature);
    }

    @Override
    public void turnOff() {
        isOn = false;
        mode = "OFF";
        log.info("Thermostat '{}' in {} turned OFF", name, location);
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("Thermostat %s: %s, Mode: %s, Target: %.1f°C, Current: %.1f°C",
                name, isOn ? "ON" : "OFF", mode, targetTemperature, currentTemperature);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Smart Thermostat [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        if (!isOn) return 0.0;
        double tempDiff = Math.abs(targetTemperature - currentTemperature);
        return tempDiff * 100; // Approximate watts based on temperature difference
    }

    @Override
    public void operate(String command) {
        String[] parts = command.split(":");
        switch (parts[0].toUpperCase()) {
            case "TEMPERATURE":
                setTargetTemperature(Double.parseDouble(parts[1]));
                break;
            case "MODE":
                setMode(parts[1]);
                break;
            case "CURRENT":
                updateCurrentTemperature(Double.parseDouble(parts[1]));
                break;
            default:
                log.warn("Unknown command: {}", command);
        }
    }

    public void setTargetTemperature(double temperature) {
        this.targetTemperature = Math.max(10, Math.min(35, temperature));
        log.info("Thermostat '{}' target temperature set to {}°C", name, this.targetTemperature);
    }

    public void setMode(String mode) {
        this.mode = mode;
        log.info("Thermostat '{}' mode set to {}", name, mode);
    }

    public void updateCurrentTemperature(double temperature) {
        this.currentTemperature = temperature;
        log.debug("Thermostat '{}' current temperature updated to {}°C", name, temperature);
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public String getMode() {
        return mode;
    }
}
