package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Implementor - Light device
 *
 * Reuses Bridge "volume/channel" knobs as brightness and color temperature.
 */
public class LightDevice implements DeviceImplementor {
    private static final Logger log = LoggerFactory.getLogger(LightDevice.class);

    private boolean on = false;
    private int brightness = 50; // 0-100
    private int colorTemperature = 2700; // Kelvin

    @Override
    public boolean isEnabled() {
        return on;
    }

    @Override
    public void enable() {
        on = true;
        log.info("Light turned ON");
    }

    @Override
    public void disable() {
        on = false;
        log.info("Light turned OFF");
    }

    @Override
    public int getVolume() {
        return brightness;
    }

    @Override
    public void setVolume(int volume) {
        this.brightness = Math.max(0, Math.min(100, volume));
        log.info("Light brightness set to {}%", brightness);
    }

    @Override
    public int getChannel() {
        return colorTemperature;
    }

    @Override
    public void setChannel(int channel) {
        // Keep within a reasonable Kelvin range
        this.colorTemperature = Math.max(2000, Math.min(6500, channel));
        log.info("Light color temperature set to {}K", colorTemperature);
    }

    @Override
    public String getDeviceType() {
        return "Light";
    }
}

