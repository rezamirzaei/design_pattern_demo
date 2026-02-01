package com.smarthome.pattern.behavioral.observer;

/**
 * OBSERVER PATTERN
 *
 * Intent: Define a one-to-many dependency between objects so that when one
 * object changes state, all its dependents are notified and updated automatically.
 *
 * Smart Home Application: When a device state changes (motion detected, door opened),
 * multiple observers (mobile app, dashboard, automation engine) need to be notified.
 */

/**
 * Observer interface - receives notifications about device events
 */
public interface DeviceObserver {

    /**
     * Called when a device event occurs
     * @param eventType Type of event (e.g., "STATE_CHANGED", "MOTION_DETECTED")
     * @param data Additional event data
     */
    void onDeviceEvent(String eventType, String data);

    /**
     * Get the name of this observer (for logging)
     */
    String getObserverName();
}
