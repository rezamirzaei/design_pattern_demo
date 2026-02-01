package com.smarthome.pattern.structural.adapter;

import com.smarthome.pattern.creational.factory.Device;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter that makes LegacyThermostat compatible with Device interface
 * Uses Object Adapter pattern (composition over inheritance)
 */
@Slf4j
public class LegacyThermostatAdapter implements Device {
    private final LegacyThermostat legacyThermostat;
    private final String name;
    private final String location;

    public LegacyThermostatAdapter(LegacyThermostat legacyThermostat, String name, String location) {
        this.legacyThermostat = legacyThermostat;
        this.name = name;
        this.location = location;
        log.info("Legacy thermostat '{}' adapted to smart device interface", name);
    }

    @Override
    public void turnOn() {
        legacyThermostat.setPower(true);
        legacyThermostat.setMode("auto");
    }

    @Override
    public void turnOff() {
        legacyThermostat.setPower(false);
        legacyThermostat.setMode("off");
    }

    @Override
    public boolean isOn() {
        return legacyThermostat.getPower();
    }

    @Override
    public String getStatus() {
        double celsius = fahrenheitToCelsius(legacyThermostat.getTemperatureFahrenheit());
        return String.format("Adapted Thermostat %s: %s, Temp=%.1f°C, Mode=%s",
                name, isOn() ? "ON" : "OFF", celsius, legacyThermostat.getMode());
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Legacy Thermostat Adapter [%s] @ %s", name, location);
    }

    @Override
    public double getPowerConsumption() {
        return isOn() ? 200.0 : 0.0; // Legacy devices less efficient
    }

    @Override
    public void operate(String command) {
        String[] parts = command.split(":");
        switch (parts[0].toUpperCase()) {
            case "TEMPERATURE":
                // Convert Celsius to Fahrenheit for legacy device
                double celsius = Double.parseDouble(parts[1]);
                int fahrenheit = celsiusToFahrenheit(celsius);
                legacyThermostat.setTemperatureFahrenheit(fahrenheit);
                break;
            case "MODE":
                String mode = parts[1].toLowerCase();
                legacyThermostat.setMode(mode);
                break;
            default:
                log.warn("Unknown command for legacy thermostat: {}", command);
        }
    }

    // Helper conversion methods
    private double fahrenheitToCelsius(int fahrenheit) {
        return (fahrenheit - 32) * 5.0 / 9.0;
    }

    private int celsiusToFahrenheit(double celsius) {
        return (int) Math.round(celsius * 9.0 / 5.0 + 32);
    }
}
