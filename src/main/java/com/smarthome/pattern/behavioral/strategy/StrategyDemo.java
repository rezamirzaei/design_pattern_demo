package com.smarthome.pattern.behavioral.strategy;

import com.smarthome.pattern.creational.factory.Device;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StrategyDemo {
    private StrategyDemo() {}

    public static Map<String, Object> apply(String strategyName, List<Device> devices) {
        EnergyManager manager = new EnergyManager();
        if (strategyName != null && !strategyName.isBlank()) {
            manager.setStrategy(strategyName.toUpperCase(Locale.ROOT));
        }
        manager.applyStrategy(devices);

        return Map.of(
                "pattern", "Strategy",
                "strategy", manager.getCurrentStrategyName(),
                "expectedSavingsPercent", manager.getExpectedSavings(),
                "deviceCount", devices == null ? 0 : devices.size()
        );
    }
}

