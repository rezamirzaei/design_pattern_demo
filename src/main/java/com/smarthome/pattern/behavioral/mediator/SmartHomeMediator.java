package com.smarthome.pattern.behavioral.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * MEDIATOR PATTERN
 *
 * Intent: Define an object that encapsulates how a set of objects interact.
 * Mediator promotes loose coupling by keeping objects from referring to each
 * other explicitly, and it lets you vary their interaction independently.
 *
 * Smart Home Application: Devices need to communicate and coordinate. Instead of
 * each device knowing about others, they communicate through a central mediator
 * (SmartHomeMediator) that coordinates their interactions.
 */

/**
 * Mediator interface
 */
public interface SmartHomeMediator {
    void registerDevice(String id, DeviceColleague device);
    void unregisterDevice(String id);
    void sendMessage(String fromDevice, String toDevice, String message);
    void broadcast(String fromDevice, String message);
    void notify(String deviceId, String event);
}

/**
 * Colleague interface - devices that communicate via mediator
 */
interface DeviceColleague {
    void setMediator(SmartHomeMediator mediator);
    void send(String toDevice, String message);
    void receive(String fromDevice, String message);
    String getDeviceId();
    String getDeviceType();
}

/**
 * Concrete Mediator - Central hub for device communication
 */
class CentralHubMediator implements SmartHomeMediator {
    private static final Logger log = LoggerFactory.getLogger(CentralHubMediator.class);
    private final Map<String, DeviceColleague> devices = new HashMap<>();

    @Override
    public void registerDevice(String id, DeviceColleague device) {
        devices.put(id, device);
        device.setMediator(this);
        log.info("[Mediator] Device registered: {} ({})", id, device.getDeviceType());
    }

    @Override
    public void unregisterDevice(String id) {
        devices.remove(id);
        log.info("[Mediator] Device unregistered: {}", id);
    }

    @Override
    public void sendMessage(String fromDevice, String toDevice, String message) {
        DeviceColleague target = devices.get(toDevice);
        if (target != null) {
            log.info("[Mediator] Routing message from {} to {}: {}", fromDevice, toDevice, message);
            target.receive(fromDevice, message);
        } else {
            log.warn("[Mediator] Target device not found: {}", toDevice);
        }
    }

    @Override
    public void broadcast(String fromDevice, String message) {
        log.info("[Mediator] Broadcasting from {}: {}", fromDevice, message);
        for (Map.Entry<String, DeviceColleague> entry : devices.entrySet()) {
            if (!entry.getKey().equals(fromDevice)) {
                entry.getValue().receive(fromDevice, message);
            }
        }
    }

    @Override
    public void notify(String deviceId, String event) {
        log.info("[Mediator] Event from {}: {}", deviceId, event);

        // Coordinate devices based on events
        switch (event) {
            case "MOTION_DETECTED":
                handleMotionDetected(deviceId);
                break;
            case "DOOR_OPENED":
                handleDoorOpened(deviceId);
                break;
            case "TEMPERATURE_HIGH":
                handleHighTemperature(deviceId);
                break;
            case "SMOKE_DETECTED":
                handleSmokeDetected(deviceId);
                break;
            default:
                log.debug("[Mediator] Unhandled event: {}", event);
        }
    }

    private void handleMotionDetected(String sensorId) {
        log.info("[Mediator] Coordinating response to motion detection");
        // Turn on lights in the same area
        for (DeviceColleague device : devices.values()) {
            if (device.getDeviceType().equals("LIGHT")) {
                device.receive(sensorId, "TURN_ON");
            }
            if (device.getDeviceType().equals("CAMERA")) {
                device.receive(sensorId, "START_RECORDING");
            }
        }
    }

    private void handleDoorOpened(String lockId) {
        log.info("[Mediator] Coordinating response to door opened");
        for (DeviceColleague device : devices.values()) {
            if (device.getDeviceType().equals("LIGHT")) {
                device.receive(lockId, "TURN_ON");
            }
            if (device.getDeviceType().equals("CAMERA")) {
                device.receive(lockId, "TAKE_SNAPSHOT");
            }
        }
    }

    private void handleHighTemperature(String thermostatId) {
        log.info("[Mediator] Coordinating response to high temperature");
        for (DeviceColleague device : devices.values()) {
            if (device.getDeviceType().equals("THERMOSTAT")) {
                device.receive(thermostatId, "ENABLE_COOLING");
            }
        }
    }

    private void handleSmokeDetected(String sensorId) {
        log.warn("[Mediator] SMOKE DETECTED - Emergency coordination!");
        for (DeviceColleague device : devices.values()) {
            device.receive(sensorId, "EMERGENCY_ALERT");
            if (device.getDeviceType().equals("LOCK")) {
                device.receive(sensorId, "UNLOCK_FOR_EVACUATION");
            }
            if (device.getDeviceType().equals("LIGHT")) {
                device.receive(sensorId, "EMERGENCY_LIGHTING");
            }
        }
    }

    public int getDeviceCount() {
        return devices.size();
    }
}

/**
 * Concrete Colleague - Smart Light
 */
class SmartLightColleague implements DeviceColleague {
    private static final Logger log = LoggerFactory.getLogger(SmartLightColleague.class);
    private final String deviceId;
    private SmartHomeMediator mediator;
    private boolean isOn = false;

    public SmartLightColleague(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void setMediator(SmartHomeMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void send(String toDevice, String message) {
        mediator.sendMessage(deviceId, toDevice, message);
    }

    @Override
    public void receive(String fromDevice, String message) {
        log.info("[Light {}] Received from {}: {}", deviceId, fromDevice, message);
        switch (message) {
            case "TURN_ON":
                isOn = true;
                log.info("[Light {}] Turned ON", deviceId);
                break;
            case "TURN_OFF":
                isOn = false;
                log.info("[Light {}] Turned OFF", deviceId);
                break;
            case "EMERGENCY_LIGHTING":
                isOn = true;
                log.warn("[Light {}] EMERGENCY MODE - Full brightness", deviceId);
                break;
        }
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getDeviceType() {
        return "LIGHT";
    }

    public boolean isOn() {
        return isOn;
    }
}

/**
 * Concrete Colleague - Motion Sensor
 */
class MotionSensorColleague implements DeviceColleague {
    private static final Logger log = LoggerFactory.getLogger(MotionSensorColleague.class);
    private final String deviceId;
    private SmartHomeMediator mediator;

    public MotionSensorColleague(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void setMediator(SmartHomeMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void send(String toDevice, String message) {
        mediator.sendMessage(deviceId, toDevice, message);
    }

    @Override
    public void receive(String fromDevice, String message) {
        log.info("[Sensor {}] Received from {}: {}", deviceId, fromDevice, message);
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getDeviceType() {
        return "SENSOR";
    }

    public void detectMotion() {
        log.info("[Sensor {}] Motion detected!", deviceId);
        mediator.notify(deviceId, "MOTION_DETECTED");
    }
}

/**
 * Concrete Colleague - Camera
 */
class CameraColleague implements DeviceColleague {
    private static final Logger log = LoggerFactory.getLogger(CameraColleague.class);
    private final String deviceId;
    private SmartHomeMediator mediator;
    private boolean isRecording = false;

    public CameraColleague(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void setMediator(SmartHomeMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void send(String toDevice, String message) {
        mediator.sendMessage(deviceId, toDevice, message);
    }

    @Override
    public void receive(String fromDevice, String message) {
        log.info("[Camera {}] Received from {}: {}", deviceId, fromDevice, message);
        switch (message) {
            case "START_RECORDING":
                isRecording = true;
                log.info("[Camera {}] Recording started", deviceId);
                break;
            case "STOP_RECORDING":
                isRecording = false;
                log.info("[Camera {}] Recording stopped", deviceId);
                break;
            case "TAKE_SNAPSHOT":
                log.info("[Camera {}] Snapshot taken", deviceId);
                break;
        }
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getDeviceType() {
        return "CAMERA";
    }

    public boolean isRecording() {
        return isRecording;
    }
}
