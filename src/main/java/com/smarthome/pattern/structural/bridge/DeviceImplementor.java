package com.smarthome.pattern.structural.bridge;

/**
 * BRIDGE PATTERN
 * 
 * Intent: Decouple an abstraction from its implementation so that the two
 * can vary independently.
 * 
 * Smart Home Application: Remote controls (abstraction) can control different
 * device types (implementation). A basic remote and advanced remote can both
 * control lights, thermostats, etc. The bridge separates the remote UI from
 * the device control logic.
 */

/**
 * Implementor interface - Device operations that remotes can use
 */
public interface DeviceImplementor {
    boolean isEnabled();
    void enable();
    void disable();
    int getVolume();
    void setVolume(int volume);
    int getChannel();
    void setChannel(int channel);
    String getDeviceType();
}
