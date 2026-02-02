package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Implementor - Thermostat device
 *
 * Uses Bridge "volume" as target temperature (°C) and "channel" as mode.
 */
public class ThermostatDevice implements DeviceImplementor {
    private static final Logger log = LoggerFactory.getLogger(ThermostatDevice.class);

    private boolean on = false;
    private int targetTemperature = 22; // °C
    private int mode = 0; // 0=AUTO, 1=HEAT, 2=COOL

    @Override
    public boolean isEnabled() {
        return on;
    }

    @Override
    public void enable() {
        on = true;
        log.info("Thermostat turned ON");
    }

    @Override
    public void disable() {
        on = false;
        log.info("Thermostat turned OFF");
    }

    @Override
    public int getVolume() {
        return targetTemperature;
    }

    @Override
    public void setVolume(int volume) {
        this.targetTemperature = Math.max(10, Math.min(30, volume));
        log.info("Thermostat target temperature set to {}°C", targetTemperature);
    }

    @Override
    public int getChannel() {
        return mode;
    }

    @Override
    public void setChannel(int channel) {
        this.mode = Math.max(0, Math.min(2, channel));
        log.info("Thermostat mode set to {}", switch (mode) {
            case 1 -> "HEAT";
            case 2 -> "COOL";
            default -> "AUTO";
        });
    }

    @Override
    public String getDeviceType() {
        return "Thermostat";
    }
}

