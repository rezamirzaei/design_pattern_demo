package com.smarthome.pattern.behavioral.strategy;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * STRATEGY PATTERN
 *
 * Intent: Define a family of algorithms, encapsulate each one, and make them
 * interchangeable. Strategy lets the algorithm vary independently from
 * clients that use it.
 *
 * Smart Home Application: Different energy optimization strategies (Eco, Comfort,
 * Away, Party) can be applied to the home. Each strategy has different algorithms
 * for managing devices.
 */

/**
 * Strategy interface
 */
public interface EnergyStrategy {
    void apply(List<Device> devices);
    String getStrategyName();
    String getDescription();
    double getExpectedSavings(); // percentage
}

/**
 * Concrete Strategy - Eco Mode
 */
class EcoModeStrategy implements EnergyStrategy {
    private static final Logger log = LoggerFactory.getLogger(EcoModeStrategy.class);

    @Override
    public void apply(List<Device> devices) {
        log.info("Applying ECO mode strategy to {} devices", devices.size());
        for (Device device : devices) {
            String info = device.getDeviceInfo().toLowerCase();
            if (info.contains("light")) {
                device.operate("BRIGHTNESS:50");
            } else if (info.contains("thermostat")) {
                device.operate("TEMPERATURE:20");
                device.operate("MODE:ECO");
            } else if (info.contains("tv") || info.contains("entertainment")) {
                device.turnOff();
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "ECO";
    }

    @Override
    public String getDescription() {
        return "Eco Mode - Reduces energy consumption by dimming lights, lowering temperature, and turning off non-essential devices";
    }

    @Override
    public double getExpectedSavings() {
        return 30.0;
    }
}

/**
 * Concrete Strategy - Comfort Mode
 */
class ComfortModeStrategy implements EnergyStrategy {
    private static final Logger log = LoggerFactory.getLogger(ComfortModeStrategy.class);

    @Override
    public void apply(List<Device> devices) {
        log.info("Applying COMFORT mode strategy to {} devices", devices.size());
        for (Device device : devices) {
            String info = device.getDeviceInfo().toLowerCase();
            if (info.contains("light")) {
                device.turnOn();
                device.operate("BRIGHTNESS:80");
            } else if (info.contains("thermostat")) {
                device.turnOn();
                device.operate("TEMPERATURE:22");
                device.operate("MODE:AUTO");
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "COMFORT";
    }

    @Override
    public String getDescription() {
        return "Comfort Mode - Optimal settings for daily living with moderate energy use";
    }

    @Override
    public double getExpectedSavings() {
        return 10.0;
    }
}

/**
 * Concrete Strategy - Away Mode
 */
class AwayModeStrategy implements EnergyStrategy {
    private static final Logger log = LoggerFactory.getLogger(AwayModeStrategy.class);

    @Override
    public void apply(List<Device> devices) {
        log.info("Applying AWAY mode strategy to {} devices", devices.size());
        for (Device device : devices) {
            String info = device.getDeviceInfo().toLowerCase();
            if (info.contains("camera")) {
                device.turnOn();
                device.operate("MOTION:true");
                device.operate("RECORD:true");
            } else if (info.contains("thermostat")) {
                device.operate("TEMPERATURE:18");
                device.operate("MODE:ECO");
            } else if (info.contains("light")) {
                device.turnOff();
            } else if (info.contains("lock")) {
                device.operate("LOCK");
                device.operate("AUTOLOCK:true");
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "AWAY";
    }

    @Override
    public String getDescription() {
        return "Away Mode - Maximum energy savings while maintaining security";
    }

    @Override
    public double getExpectedSavings() {
        return 50.0;
    }
}

/**
 * Concrete Strategy - Party Mode
 */
class PartyModeStrategy implements EnergyStrategy {
    private static final Logger log = LoggerFactory.getLogger(PartyModeStrategy.class);

    @Override
    public void apply(List<Device> devices) {
        log.info("Applying PARTY mode strategy to {} devices", devices.size());
        for (Device device : devices) {
            String info = device.getDeviceInfo().toLowerCase();
            if (info.contains("light")) {
                device.turnOn();
                device.operate("BRIGHTNESS:100");
                device.operate("COLOR:#FF00FF");
            } else if (info.contains("thermostat")) {
                device.operate("TEMPERATURE:21");
            } else if (info.contains("camera")) {
                device.turnOff(); // Privacy
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "PARTY";
    }

    @Override
    public String getDescription() {
        return "Party Mode - Fun atmosphere with full lighting and comfort";
    }

    @Override
    public double getExpectedSavings() {
        return -20.0; // Uses more energy
    }
}

/**
 * Concrete Strategy - Night Mode
 */
class NightModeStrategy implements EnergyStrategy {
    private static final Logger log = LoggerFactory.getLogger(NightModeStrategy.class);

    @Override
    public void apply(List<Device> devices) {
        log.info("Applying NIGHT mode strategy to {} devices", devices.size());
        for (Device device : devices) {
            String info = device.getDeviceInfo().toLowerCase();
            if (info.contains("light")) {
                device.turnOff();
            } else if (info.contains("thermostat")) {
                device.operate("TEMPERATURE:19");
                device.operate("MODE:NIGHT");
            } else if (info.contains("camera")) {
                device.operate("MOTION:true");
            } else if (info.contains("lock")) {
                device.operate("LOCK");
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "NIGHT";
    }

    @Override
    public String getDescription() {
        return "Night Mode - Optimized for sleeping with lights off and lower temperature";
    }

    @Override
    public double getExpectedSavings() {
        return 40.0;
    }
}

/**
 * Context - Energy Manager that uses strategies
 */
class EnergyManager {
    private static final Logger log = LoggerFactory.getLogger(EnergyManager.class);
    private EnergyStrategy currentStrategy;
    private final Map<String, EnergyStrategy> availableStrategies;

    public EnergyManager() {
        availableStrategies = Map.of(
            "ECO", new EcoModeStrategy(),
            "COMFORT", new ComfortModeStrategy(),
            "AWAY", new AwayModeStrategy(),
            "PARTY", new PartyModeStrategy(),
            "NIGHT", new NightModeStrategy()
        );
        currentStrategy = availableStrategies.get("COMFORT");
    }

    public void setStrategy(String strategyName) {
        EnergyStrategy strategy = availableStrategies.get(strategyName.toUpperCase());
        if (strategy != null) {
            this.currentStrategy = strategy;
            log.info("Energy strategy changed to: {}", strategy.getStrategyName());
        } else {
            log.warn("Unknown strategy: {}", strategyName);
        }
    }

    public void setStrategy(EnergyStrategy strategy) {
        this.currentStrategy = strategy;
        log.info("Energy strategy changed to: {}", strategy.getStrategyName());
    }

    public void applyStrategy(List<Device> devices) {
        if (currentStrategy != null) {
            currentStrategy.apply(devices);
        }
    }

    public String getCurrentStrategyName() {
        return currentStrategy != null ? currentStrategy.getStrategyName() : "NONE";
    }

    public double getExpectedSavings() {
        return currentStrategy != null ? currentStrategy.getExpectedSavings() : 0;
    }

    public Map<String, EnergyStrategy> getAvailableStrategies() {
        return availableStrategies;
    }
}