package com.smarthome.pattern.structural.flyweight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FlyweightDemo {
    private FlyweightDemo() {}

    public static Map<String, Object> demo() {
        return demo(3);
    }

    public static Map<String, Object> stats(int instances) {
        int count = Math.max(1, instances);
        int flyweights = DeviceTypeFactory.getFlyweightCount();
        int memorySaved = (int) Math.round((1.0 - (double) flyweights / count) * 100);
        if (memorySaved < 0) {
            memorySaved = 0;
        } else if (memorySaved > 100) {
            memorySaved = 100;
        }
        return Map.of(
                "pattern", "Flyweight",
                "instances", count,
                "sharedTypes", flyweights,
                "memorySaved", memorySaved
        );
    }

    public static Map<String, Object> demo(int count) {
        List<DeviceInstance> instances = new ArrayList<>();
        String[] types = {"LIGHT", "CAMERA", "THERMOSTAT", "LOCK", "SENSOR"};
        String[] rooms = {"Living Room", "Bedroom", "Kitchen", "Front Yard", "Garage"};

        int actualCount = count > 0 ? count : 3;
        for (int i = 0; i < actualCount; i++) {
            String type = types[i % types.length];
            String room = rooms[i % rooms.length];
            DeviceInstance instance = new DeviceInstance("fw-" + i, type + " " + i, room, type);
            instance.setOn(i % 2 == 0);
            instances.add(instance);
        }

        double total = instances.stream().mapToDouble(DeviceInstance::getEstimatedPower).sum();

        List<Map<String, Object>> instanceMaps = instances.stream()
                .map(inst -> Map.<String, Object>of(
                        "id", inst.getDeviceId(),
                        "type", inst.getType().getTypeName(),
                        "power", inst.getEstimatedPower(),
                        "isOn", inst.isOn()
                ))
                .toList();

        return Map.of(
                "pattern", "Flyweight",
                "flyweightCount", DeviceTypeFactory.getFlyweightCount(),
                "totalInstances", actualCount,
                "availableTypes", DeviceTypeFactory.getAvailableTypes().stream().sorted().toList(),
                "instances", instanceMaps,
                "estimatedTotalPower", total,
                "memorySaved", String.format("%.0f%%", (1.0 - (double) DeviceTypeFactory.getFlyweightCount() / actualCount) * 100)
        );
    }
}
