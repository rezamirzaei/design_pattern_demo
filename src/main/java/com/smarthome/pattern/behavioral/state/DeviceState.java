package com.smarthome.pattern.behavioral.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATE PATTERN
 *
 * Intent: Allow an object to alter its behavior when its internal state changes.
 * The object will appear to change its class.
 */

/**
 * State interface
 */
public interface DeviceState {
    void turnOn(StatefulDevice device);
    void turnOff(StatefulDevice device);
    void standby(StatefulDevice device);
    void error(StatefulDevice device, String errorMessage);
    void operate(StatefulDevice device, String command);
    String getStateName();
}

/**
 * Context - Device that has state
 */
class StatefulDevice {
    private static final Logger log = LoggerFactory.getLogger(StatefulDevice.class);
    private DeviceState state;
    private final String name;
    private String lastError;

    public StatefulDevice(String name) {
        this.name = name;
        this.state = new OffState();
        log.info("Device '{}' created in OFF state", name);
    }

    public void setState(DeviceState state) {
        log.info("Device '{}' transitioning from {} to {}",
                name, this.state.getStateName(), state.getStateName());
        this.state = state;
    }

    public void turnOn() { state.turnOn(this); }
    public void turnOff() { state.turnOff(this); }
    public void standby() { state.standby(this); }
    public void reportError(String message) { state.error(this, message); }
    public void operate(String command) { state.operate(this, command); }

    public String getStateName() { return state.getStateName(); }
    public String getName() { return name; }
    public String getLastError() { return lastError; }
    public void setLastError(String error) { this.lastError = error; }
}

/**
 * Concrete State - Off
 */
class OffState implements DeviceState {
    private static final Logger log = LoggerFactory.getLogger(OffState.class);

    @Override
    public void turnOn(StatefulDevice device) {
        log.info("[{}] Turning on from OFF state", device.getName());
        device.setState(new OnState());
    }

    @Override
    public void turnOff(StatefulDevice device) {
        log.info("[{}] Already OFF", device.getName());
    }

    @Override
    public void standby(StatefulDevice device) {
        log.warn("[{}] Cannot standby from OFF - turning on first", device.getName());
        device.setState(new StandbyState());
    }

    @Override
    public void error(StatefulDevice device, String errorMessage) {
        log.error("[{}] Error while OFF: {}", device.getName(), errorMessage);
        device.setLastError(errorMessage);
        device.setState(new ErrorState());
    }

    @Override
    public void operate(StatefulDevice device, String command) {
        log.warn("[{}] Cannot operate while OFF", device.getName());
    }

    @Override
    public String getStateName() { return "OFF"; }
}

/**
 * Concrete State - On
 */
class OnState implements DeviceState {
    private static final Logger log = LoggerFactory.getLogger(OnState.class);

    @Override
    public void turnOn(StatefulDevice device) {
        log.info("[{}] Already ON", device.getName());
    }

    @Override
    public void turnOff(StatefulDevice device) {
        log.info("[{}] Turning off from ON state", device.getName());
        device.setState(new OffState());
    }

    @Override
    public void standby(StatefulDevice device) {
        log.info("[{}] Going to standby from ON state", device.getName());
        device.setState(new StandbyState());
    }

    @Override
    public void error(StatefulDevice device, String errorMessage) {
        log.error("[{}] Error while ON: {}", device.getName(), errorMessage);
        device.setLastError(errorMessage);
        device.setState(new ErrorState());
    }

    @Override
    public void operate(StatefulDevice device, String command) {
        log.info("[{}] Executing command: {}", device.getName(), command);
    }

    @Override
    public String getStateName() { return "ON"; }
}

/**
 * Concrete State - Standby
 */
class StandbyState implements DeviceState {
    private static final Logger log = LoggerFactory.getLogger(StandbyState.class);

    @Override
    public void turnOn(StatefulDevice device) {
        log.info("[{}] Waking up from standby", device.getName());
        device.setState(new OnState());
    }

    @Override
    public void turnOff(StatefulDevice device) {
        log.info("[{}] Turning off from standby", device.getName());
        device.setState(new OffState());
    }

    @Override
    public void standby(StatefulDevice device) {
        log.info("[{}] Already in standby", device.getName());
    }

    @Override
    public void error(StatefulDevice device, String errorMessage) {
        log.error("[{}] Error in standby: {}", device.getName(), errorMessage);
        device.setLastError(errorMessage);
        device.setState(new ErrorState());
    }

    @Override
    public void operate(StatefulDevice device, String command) {
        log.info("[{}] Waking up to execute: {}", device.getName(), command);
        device.setState(new OnState());
    }

    @Override
    public String getStateName() { return "STANDBY"; }
}

/**
 * Concrete State - Error
 */
class ErrorState implements DeviceState {
    private static final Logger log = LoggerFactory.getLogger(ErrorState.class);

    @Override
    public void turnOn(StatefulDevice device) {
        log.warn("[{}] Cannot turn on in error state - reset required", device.getName());
    }

    @Override
    public void turnOff(StatefulDevice device) {
        log.info("[{}] Resetting error state", device.getName());
        device.setLastError(null);
        device.setState(new OffState());
    }

    @Override
    public void standby(StatefulDevice device) {
        log.warn("[{}] Cannot standby in error state", device.getName());
    }

    @Override
    public void error(StatefulDevice device, String errorMessage) {
        log.error("[{}] Additional error: {}", device.getName(), errorMessage);
        device.setLastError(errorMessage);
    }

    @Override
    public void operate(StatefulDevice device, String command) {
        log.warn("[{}] Cannot operate in error state", device.getName());
    }

    @Override
    public String getStateName() { return "ERROR"; }
}
