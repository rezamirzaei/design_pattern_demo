package com.smarthome.pattern.creational.builder;

import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.singleton.HomeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for rule evaluation and action execution
 */
public class RuleContext {
    private static final Logger log = LoggerFactory.getLogger(RuleContext.class);
    private final Map<String, Object> data = new HashMap<>();
    private final HomeController homeController = HomeController.INSTANCE;

    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public boolean checkTrigger(String type, String value) {
        switch (type) {
            case "MOTION_DETECTED":
                Boolean motion = (Boolean) data.get("motion_" + value);
                return motion != null && motion;
            case "DOOR_OPENED":
                Boolean door = (Boolean) data.get("door_" + value);
                return door != null && door;
            case "TEMP_ABOVE":
                Double currentTemp = (Double) data.get("temperature");
                return currentTemp != null && currentTemp > Double.parseDouble(value);
            case "TEMP_BELOW":
                currentTemp = (Double) data.get("temperature");
                return currentTemp != null && currentTemp < Double.parseDouble(value);
            default:
                return false;
        }
    }

    public boolean checkCondition(String type, String value) {
        switch (type) {
            case "TIME_AFTER":
                LocalTime timeAfter = LocalTime.parse(value);
                return LocalTime.now().isAfter(timeAfter);
            case "TIME_BEFORE":
                LocalTime timeBefore = LocalTime.parse(value);
                return LocalTime.now().isBefore(timeBefore);
            case "MODE_EQUALS":
                return value.equals(homeController.getHomeModeEnum().name());
            default:
                return true;
        }
    }

    public void executeAction(String type, String value) {
        switch (type) {
            case "TURN_ON":
                Device device = homeController.getDevice(value);
                if (device != null) {
                    device.turnOn();
                    log.info("Action executed: Turned ON device {}", value);
                }
                break;
            case "TURN_OFF":
                device = homeController.getDevice(value);
                if (device != null) {
                    device.turnOff();
                    log.info("Action executed: Turned OFF device {}", value);
                }
                break;
            case "SET_BRIGHTNESS":
                String[] brightParts = value.split(":");
                device = homeController.getDevice(brightParts[0]);
                if (device != null) {
                    device.operate("BRIGHTNESS:" + brightParts[1]);
                    log.info("Action executed: Set brightness of {} to {}", brightParts[0], brightParts[1]);
                }
                break;
            case "SET_TEMPERATURE":
                String[] tempParts = value.split(":");
                device = homeController.getDevice(tempParts[0]);
                if (device != null) {
                    device.operate("TEMPERATURE:" + tempParts[1]);
                    log.info("Action executed: Set temperature of {} to {}", tempParts[0], tempParts[1]);
                }
                break;
            case "NOTIFY":
                log.info("NOTIFICATION: {}", value);
                break;
            default:
                log.warn("Unknown action type: {}", type);
        }
    }
}
