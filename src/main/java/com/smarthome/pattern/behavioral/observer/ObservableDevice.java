package com.smarthome.pattern.behavioral.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject - Observable device that notifies observers of state changes
 */
public class ObservableDevice {
    private static final Logger log = LoggerFactory.getLogger(ObservableDevice.class);
    private final String deviceId;
    private final String name;
    private final List<DeviceObserver> observers = new ArrayList<>();
    private String state = "OFF";

    public ObservableDevice(String deviceId, String name) {
        this.deviceId = deviceId;
        this.name = name;
    }

    public void addObserver(DeviceObserver observer) {
        observers.add(observer);
        log.info("Observer '{}' added to device '{}'", observer.getObserverName(), name);
    }

    public void removeObserver(DeviceObserver observer) {
        observers.remove(observer);
        log.info("Observer '{}' removed from device '{}'", observer.getObserverName(), name);
    }

    public void setState(String newState) {
        String oldState = this.state;
        this.state = newState;
        log.info("Device '{}' state changed: {} -> {}", name, oldState, newState);
        notifyObservers("STATE_CHANGED", String.format("%s: %s -> %s", deviceId, oldState, newState));
    }

    public void triggerMotion() {
        log.info("Motion detected by device '{}'", name);
        notifyObservers("MOTION_DETECTED", deviceId);
    }

    public void triggerAlert(String alertType, String message) {
        log.info("Alert from device '{}': {} - {}", name, alertType, message);
        notifyObservers(alertType, message);
    }

    protected void notifyObservers(String eventType, String data) {
        log.debug("Notifying {} observers of event: {}", observers.size(), eventType);
        for (DeviceObserver observer : observers) {
            observer.onDeviceEvent(eventType, data);
        }
    }

    public String getState() {
        return state;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public int getObserverCount() {
        return observers.size();
    }
}
