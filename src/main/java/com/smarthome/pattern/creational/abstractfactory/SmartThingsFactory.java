package com.smarthome.pattern.creational.abstractfactory;

import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartLock;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Factory - SmartThings Ecosystem
 * Creates devices compatible with Samsung SmartThings platform
 */
public class SmartThingsFactory implements SmartDeviceAbstractFactory {
    private static final Logger log = LoggerFactory.getLogger(SmartThingsFactory.class);

    @Override
    public Device createLight(String name, String location) {
        log.info("[SmartThings] Creating light: {} at {}", name, location);
        return new SmartLight("[ST] " + name, location);
    }

    @Override
    public Device createThermostat(String name, String location) {
        log.info("[SmartThings] Creating thermostat: {} at {}", name, location);
        return new SmartThermostat("[ST] " + name, location);
    }

    @Override
    public Device createLock(String name, String location) {
        log.info("[SmartThings] Creating lock: {} at {}", name, location);
        return new SmartLock("[ST] " + name, location);
    }

    @Override
    public Device createSensor(String name, String location) {
        log.info("[SmartThings] Creating sensor: {} at {}", name, location);
        return new SmartThingsSensor(name, location);
    }

    @Override
    public String getEcosystemName() {
        return "Samsung SmartThings";
    }
}
