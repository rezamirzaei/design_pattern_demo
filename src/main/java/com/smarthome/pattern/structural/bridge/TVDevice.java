package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Implementor - TV device
 */
public class TVDevice implements DeviceImplementor {
    private static final Logger log = LoggerFactory.getLogger(TVDevice.class);
    private boolean on = false;
    private int volume = 30;
    private int channel = 1;

    @Override
    public boolean isEnabled() {
        return on;
    }

    @Override
    public void enable() {
        on = true;
        log.info("TV turned ON");
    }

    @Override
    public void disable() {
        on = false;
        log.info("TV turned OFF");
    }

    @Override
    public int getVolume() {
        return volume;
    }

    @Override
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        log.info("TV volume set to {}", this.volume);
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public void setChannel(int channel) {
        this.channel = channel;
        log.info("TV channel set to {}", channel);
    }

    @Override
    public String getDeviceType() {
        return "TV";
    }
}
