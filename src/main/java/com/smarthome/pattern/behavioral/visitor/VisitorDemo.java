package com.smarthome.pattern.behavioral.visitor;

import java.util.Locale;
import java.util.Map;

public final class VisitorDemo {
    private VisitorDemo() {}

    public static Map<String, Object> audit(String type) {
        String normalized = type == null ? "SECURITY" : type.toUpperCase(Locale.ROOT);
        DeviceVisitor visitor = switch (normalized) {
            case "ENERGY" -> new EnergyAuditVisitor();
            case "MAINTENANCE" -> new MaintenanceVisitor();
            case "SECURITY" -> new SecurityAuditVisitor();
            default -> new SecurityAuditVisitor();
        };

        VisitableLight light = new VisitableLight("Living Light", "Living Room");
        VisitableThermostat thermostat = new VisitableThermostat("Main Thermostat", "Living Room");
        VisitableCamera camera = new VisitableCamera("Front Camera", "Front Yard");
        VisitableLock lock = new VisitableLock("Front Door Lock", "Hallway");

        light.accept(visitor);
        thermostat.accept(visitor);
        camera.accept(visitor);
        lock.accept(visitor);

        return Map.of(
                "pattern", "Visitor",
                "type", normalized,
                "report", visitor.getReport()
        );
    }
}

