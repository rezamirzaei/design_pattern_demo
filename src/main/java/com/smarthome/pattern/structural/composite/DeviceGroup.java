package com.smarthome.pattern.structural.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite - Device Group (can represent a room, floor, or zone)
 * Can contain both individual devices and other groups
 */
public class DeviceGroup implements DeviceComponent {
    private static final Logger log = LoggerFactory.getLogger(DeviceGroup.class);
    private final String name;
    private final List<DeviceComponent> components = new ArrayList<>();

    public DeviceGroup(String name) {
        this.name = name;
    }

    /**
     * Add a component (device or group) to this group
     */
    public void add(DeviceComponent component) {
        components.add(component);
        log.info("Added '{}' to group '{}'", component.getName(), name);
    }

    /**
     * Remove a component from this group
     */
    public void remove(DeviceComponent component) {
        components.remove(component);
        log.info("Removed '{}' from group '{}'", component.getName(), name);
    }

    /**
     * Get all children
     */
    public List<DeviceComponent> getChildren() {
        return new ArrayList<>(components);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void turnOn() {
        log.info("Turning ON all devices in '{}'", name);
        for (DeviceComponent component : components) {
            component.turnOn();
        }
    }

    @Override
    public void turnOff() {
        log.info("Turning OFF all devices in '{}'", name);
        for (DeviceComponent component : components) {
            component.turnOff();
        }
    }

    @Override
    public double getPowerConsumption() {
        return components.stream()
                .mapToDouble(DeviceComponent::getPowerConsumption)
                .sum();
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Group '%s' (%d devices, %.1fW):\n",
                name, getDeviceCount(), getPowerConsumption()));
        for (DeviceComponent component : components) {
            sb.append("  - ").append(component.getStatus()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int getDeviceCount() {
        return components.stream()
                .mapToInt(DeviceComponent::getDeviceCount)
                .sum();
    }

    @Override
    public void printStructure(String indent) {
        log.info("{}+ {} (Group)", indent, name);
        for (DeviceComponent component : components) {
            component.printStructure(indent + "  ");
        }
    }
}
