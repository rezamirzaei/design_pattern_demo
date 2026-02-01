package com.smarthome.pattern.structural.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ADAPTER PATTERN
 *
 * Intent: Convert the interface of a class into another interface clients expect.
 * Adapter lets classes work together that couldn't otherwise because of
 * incompatible interfaces.
 *
 * Smart Home Application: Legacy devices (old thermostats, non-smart devices)
 * have different interfaces. The adapter allows them to work with our smart
 * home system by adapting their interface to the Device interface.
 */

/**
 * Legacy Thermostat - Old device with incompatible interface
 * This represents an old-school thermostat that doesn't implement our Device interface
 */
public class LegacyThermostat {
    private static final Logger log = LoggerFactory.getLogger(LegacyThermostat.class);
    private boolean powerOn = false;
    private int temperatureFahrenheit = 68; // Uses Fahrenheit
    private String currentMode = "off"; // "heat", "cool", "off"

    public void setPower(boolean on) {
        this.powerOn = on;
        log.info("[Legacy] Thermostat power: {}", on ? "ON" : "OFF");
    }

    public boolean getPower() {
        return powerOn;
    }

    public void setTemperatureFahrenheit(int temp) {
        this.temperatureFahrenheit = temp;
        log.info("[Legacy] Temperature set to {}°F", temp);
    }

    public int getTemperatureFahrenheit() {
        return temperatureFahrenheit;
    }

    public void setMode(String mode) {
        this.currentMode = mode;
        log.info("[Legacy] Mode set to: {}", mode);
    }

    public String getMode() {
        return currentMode;
    }

    public String getLegacyStatus() {
        return String.format("Legacy Thermostat: Power=%s, Temp=%d°F, Mode=%s",
                powerOn ? "ON" : "OFF", temperatureFahrenheit, currentMode);
    }
}
