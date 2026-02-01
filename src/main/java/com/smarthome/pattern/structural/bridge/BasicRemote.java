package com.smarthome.pattern.structural.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refined Abstraction - Basic Remote Control
 * Simple remote with basic functionality
 */
public class BasicRemote extends RemoteControl {
    private static final Logger log = LoggerFactory.getLogger(BasicRemote.class);

    public BasicRemote(DeviceImplementor device) {
        super(device);
        log.info("Basic remote connected to {}", device.getDeviceType());
    }

    @Override
    public String getRemoteType() {
        return "Basic Remote";
    }
}
