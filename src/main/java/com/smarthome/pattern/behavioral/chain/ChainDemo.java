package com.smarthome.pattern.behavioral.chain;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ChainDemo {
    private ChainDemo() {}

    public static Map<String, Object> process(String deviceId, String level, String message) {
        AlertLevel alertLevel = parseLevel(level);
        Alert alert = AlertHandlerChain.createAlert(deviceId, alertLevel, message);
        new AlertHandlerChain().processAlert(alert);

        return Map.of(
                "pattern", "Chain of Responsibility",
                "alertId", alert.getAlertId(),
                "deviceId", alert.getDeviceId(),
                "level", alert.getLevel().name(),
                "message", alert.getMessage(),
                "handledBy", handlersFor(alertLevel),
                "timestamp", alert.getTimestamp()
        );
    }

    private static AlertLevel parseLevel(String level) {
        String normalized = level == null ? "INFO" : level.toUpperCase(Locale.ROOT);
        try {
            return AlertLevel.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return AlertLevel.INFO;
        }
    }

    private static List<String> handlersFor(AlertLevel level) {
        return switch (level) {
            case INFO -> List.of("LoggingAlertHandler");
            case WARNING -> List.of("LoggingAlertHandler", "NotificationAlertHandler");
            case CRITICAL -> List.of("LoggingAlertHandler", "NotificationAlertHandler", "AlarmAlertHandler");
            case EMERGENCY -> List.of("LoggingAlertHandler", "NotificationAlertHandler", "AlarmAlertHandler", "EmergencyAlertHandler");
        };
    }
}

