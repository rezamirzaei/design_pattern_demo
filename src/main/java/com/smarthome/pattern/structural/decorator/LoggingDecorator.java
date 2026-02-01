package com.smarthome.pattern.structural.decorator;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete Decorator - Adds logging to device operations
 */
public class LoggingDecorator extends DeviceDecorator {
    private static final Logger log = LoggerFactory.getLogger(LoggingDecorator.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoggingDecorator(Device device) {
        super(device);
    }

    @Override
    public void turnOn() {
        logOperation("turnOn");
        super.turnOn();
        logResult("Device turned ON");
    }

    @Override
    public void turnOff() {
        logOperation("turnOff");
        super.turnOff();
        logResult("Device turned OFF");
    }

    @Override
    public void operate(String command) {
        logOperation("operate(" + command + ")");
        super.operate(command);
        logResult("Command executed: " + command);
    }

    @Override
    public String getDeviceInfo() {
        return "[Logged] " + super.getDeviceInfo();
    }

    private void logOperation(String operation) {
        String timestamp = LocalDateTime.now().format(formatter);
        log.info("[LOG] {} - Device: {} - Operation: {}",
                timestamp, wrappedDevice.getDeviceInfo(), operation);
    }

    private void logResult(String result) {
        log.info("[LOG] Result: {}", result);
    }
}
