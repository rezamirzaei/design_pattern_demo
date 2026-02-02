package com.smarthome.pattern.behavioral.iterator;

import com.smarthome.pattern.creational.factory.Device;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class IteratorDemo {
    private IteratorDemo() {}

    public static Map<String, Object> iterate(List<DeviceSeed> devices, String filterType, String filterValue) {
        SmartHomeDeviceCollection collection = new SmartHomeDeviceCollection();
        if (devices != null) {
            for (DeviceSeed seed : devices) {
                collection.addDevice(seed.device(), seed.room(), seed.type());
            }
        }

        DeviceIterator iterator = (filterType == null || filterType.isBlank())
                ? collection.createIterator()
                : collection.createIterator(filterType, filterValue == null ? "" : filterValue);

        List<String> visited = new ArrayList<>();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            visited.add(device.getDeviceInfo());
        }

        return Map.of(
                "pattern", "Iterator",
                "collectionSize", collection.size(),
                "filterType", filterType == null ? "ALL" : filterType,
                "filterValue", filterValue == null ? "" : filterValue,
                "visited", visited
        );
    }

    public record DeviceSeed(Device device, String room, String type) {}
}
