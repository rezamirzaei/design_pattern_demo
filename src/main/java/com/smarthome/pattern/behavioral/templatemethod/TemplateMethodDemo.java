package com.smarthome.pattern.behavioral.templatemethod;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TemplateMethodDemo {
    private TemplateMethodDemo() {}

    public static Map<String, Object> run(String deviceType) {
        String normalized = deviceType == null ? "LIGHT" : deviceType.toUpperCase(Locale.ROOT);
        DeviceInitializer initializer = switch (normalized) {
            case "THERMOSTAT" -> new ThermostatInitializer("tm-thermo-1");
            case "CAMERA" -> new CameraInitializer("tm-camera-1");
            default -> new LightInitializer("tm-light-1");
        };
        initializer.initializeDevice();

        return Map.of(
                "pattern", "Template Method",
                "initializer", initializer.getClass().getSimpleName(),
                "deviceType", initializer.getDeviceType(),
                "steps", List.of(
                        "connect",
                        "authenticate",
                        "checkFirmware",
                        "loadConfiguration",
                        "performSelfTest",
                        "additionalSetup",
                        "registerDevice",
                        "notifyReady"
                )
        );
    }
}

