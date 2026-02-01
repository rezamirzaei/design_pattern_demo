package com.smarthome.pattern.creational.abstractfactory;

import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartLock;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Factory - Apple HomeKit Ecosystem
 * Creates devices compatible with Apple HomeKit platform
 */
public class HomeKitFactory implements SmartDeviceAbstractFactory {
    private static final Logger log = LoggerFactory.getLogger(HomeKitFactory.class);

    @Override
    public Device createLight(String name, String location) {
        log.info("[HomeKit] Creating light: {} at {}", name, location);
        return new SmartLight("[HK] " + name, location);
    }

    @Override
    public Device createThermostat(String name, String location) {
        log.info("[HomeKit] Creating thermostat: {} at {}", name, location);
        return new SmartThermostat("[HK] " + name, location);
    }

    @Override
    public Device createLock(String name, String location) {
        log.info("[HomeKit] Creating lock: {} at {}", name, location);
        return new SmartLock("[HK] " + name, location);
    }

    @Override
    public Device createSensor(String name, String location) {
        log.info("[HomeKit] Creating sensor: {} at {}", name, location);
        return new HomeKitSensor(name, location);
    }

    @Override
    public String getEcosystemName() {
        return "Apple HomeKit";
    }
}
