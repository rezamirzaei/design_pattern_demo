package com.smarthome.pattern.structural.bridge;

/**
 * Abstraction - Remote Control base class
 * This is the "abstraction" part of the bridge pattern
 */
public abstract class RemoteControl {
    protected DeviceImplementor device;

    public RemoteControl(DeviceImplementor device) {
        this.device = device;
    }

    public void togglePower() {
        if (device.isEnabled()) {
            device.disable();
        } else {
            device.enable();
        }
    }

    public void volumeUp() {
        device.setVolume(device.getVolume() + 10);
    }

    public void volumeDown() {
        device.setVolume(device.getVolume() - 10);
    }

    public void channelUp() {
        device.setChannel(device.getChannel() + 1);
    }

    public void channelDown() {
        device.setChannel(device.getChannel() - 1);
    }

    public abstract String getRemoteType();

    public String getStatus() {
        return String.format("%s controlling %s - Power: %s, Volume: %d, Channel: %d",
                getRemoteType(), device.getDeviceType(),
                device.isEnabled() ? "ON" : "OFF",
                device.getVolume(), device.getChannel());
    }
}
