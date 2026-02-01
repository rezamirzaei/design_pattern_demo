package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refined Abstraction - Advanced Remote Control
 * Smart remote with additional features like mute, favorites, voice control
 */
public class AdvancedRemote extends RemoteControl {
    private static final Logger log = LoggerFactory.getLogger(AdvancedRemote.class);
    private boolean muted = false;
    private int savedVolume = 0;

    public AdvancedRemote(DeviceImplementor device) {
        super(device);
        log.info("Advanced remote connected to {}", device.getDeviceType());
    }

    @Override
    public String getRemoteType() {
        return "Advanced Remote";
    }

    /**
     * Mute/unmute functionality
     */
    public void mute() {
        if (!muted) {
            savedVolume = device.getVolume();
            device.setVolume(0);
            muted = true;
            log.info("Device muted");
        } else {
            device.setVolume(savedVolume);
            muted = false;
            log.info("Device unmuted, volume restored to {}", savedVolume);
        }
    }

    /**
     * Jump to a specific channel
     */
    public void goToChannel(int channel) {
        device.setChannel(channel);
        log.info("Jumped to channel {}", channel);
    }

    /**
     * Set volume to exact level
     */
    public void setVolume(int volume) {
        device.setVolume(volume);
        log.info("Volume set to {}", volume);
    }

    /**
     * Voice command simulation
     */
    public void voiceCommand(String command) {
        log.info("Voice command received: '{}'", command);
        command = command.toLowerCase();

        if (command.contains("turn on") || command.contains("power on")) {
            device.enable();
        } else if (command.contains("turn off") || command.contains("power off")) {
            device.disable();
        } else if (command.contains("mute")) {
            mute();
        } else if (command.contains("volume up")) {
            volumeUp();
        } else if (command.contains("volume down")) {
            volumeDown();
        } else if (command.startsWith("channel ")) {
            try {
                int ch = Integer.parseInt(command.substring(8).trim());
                goToChannel(ch);
            } catch (NumberFormatException e) {
                log.warn("Could not parse channel number from: {}", command);
            }
        }
    }

    public boolean isMuted() {
        return muted;
    }
}
