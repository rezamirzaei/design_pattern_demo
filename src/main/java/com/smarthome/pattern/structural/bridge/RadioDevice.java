package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Implementor - Radio device
 */
public class RadioDevice implements DeviceImplementor {
    private static final Logger log = LoggerFactory.getLogger(RadioDevice.class);
    private boolean on = false;
    private int volume = 20;
    private int channel = 88; // FM frequency

    @Override
    public boolean isEnabled() {
        return on;
    }

    @Override
    public void enable() {
        on = true;
        log.info("Radio turned ON");
    }

    @Override
    public void disable() {
        on = false;
        log.info("Radio turned OFF");
    }

    @Override
    public int getVolume() {
        return volume;
    }

    @Override
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        log.info("Radio volume set to {}", this.volume);
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public void setChannel(int channel) {
        this.channel = channel;
        log.info("Radio frequency set to {}.{} FM", channel / 10, channel % 10);
    }

    @Override
    public String getDeviceType() {
        return "Radio";
    }
}
