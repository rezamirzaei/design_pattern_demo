package com.smarthome.pattern.structural.flyweight;

import java.util.List;
import java.util.Map;

public final class FlyweightDemo {
    private FlyweightDemo() {}

    public static Map<String, Object> demo() {
        DeviceInstance livingLight1 = new DeviceInstance("fw-1", "Living Light 1", "Living Room", "LIGHT");
        DeviceInstance livingLight2 = new DeviceInstance("fw-2", "Living Light 2", "Living Room", "LIGHT");
        DeviceInstance camera = new DeviceInstance("fw-3", "Front Camera", "Front Yard", "CAMERA");

        livingLight1.setOn(true);
        livingLight2.setOn(false);
        camera.setOn(true);

        double total = livingLight1.getEstimatedPower() + livingLight2.getEstimatedPower() + camera.getEstimatedPower();

        return Map.of(
                "pattern", "Flyweight",
                "flyweightCount", DeviceTypeFactory.getFlyweightCount(),
                "availableTypes", DeviceTypeFactory.getAvailableTypes().stream().sorted().toList(),
                "instances", List.of(
                        Map.of("id", livingLight1.getDeviceId(), "type", livingLight1.getType().getTypeName(), "power", livingLight1.getEstimatedPower()),
                        Map.of("id", livingLight2.getDeviceId(), "type", livingLight2.getType().getTypeName(), "power", livingLight2.getEstimatedPower()),
                        Map.of("id", camera.getDeviceId(), "type", camera.getType().getTypeName(), "power", camera.getEstimatedPower())
                ),
                "estimatedTotalPower", total
        );
    }
}

