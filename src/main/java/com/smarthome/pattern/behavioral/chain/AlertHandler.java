package com.smarthome.pattern.behavioral.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CHAIN OF RESPONSIBILITY PATTERN
 *
 * Intent: Avoid coupling the sender of a request to its receiver by giving
 * more than one object a chance to handle the request.
 */

/**
 * Alert levels
 */
enum AlertLevel {
    INFO(1), WARNING(2), CRITICAL(3), EMERGENCY(4);

    final int severity;
    AlertLevel(int severity) { this.severity = severity; }
}

/**
 * Alert data class
 */
class Alert {
    private final String alertId;
    private final String deviceId;
    private final AlertLevel level;
    private final String message;
    private final long timestamp;

    public Alert(String alertId, String deviceId, AlertLevel level, String message) {
        this.alertId = alertId;
        this.deviceId = deviceId;
        this.level = level;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAlertId() { return alertId; }
    public String getDeviceId() { return deviceId; }
    public AlertLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Alert[%s] %s: %s - %s", alertId, level, deviceId, message);
    }
}

/**
 * Handler interface
 */
public abstract class AlertHandler {
    protected AlertHandler nextHandler;
    protected final AlertLevel handlerLevel;
    protected static final Logger log = LoggerFactory.getLogger(AlertHandler.class);

    public AlertHandler(AlertLevel level) {
        this.handlerLevel = level;
    }

    public AlertHandler setNext(AlertHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public void handle(Alert alert) {
        if (alert.getLevel().severity >= handlerLevel.severity) {
            processAlert(alert);
        }
        if (nextHandler != null) {
            nextHandler.handle(alert);
        }
    }

    protected abstract void processAlert(Alert alert);
}

/**
 * Concrete Handler - Logs all alerts
 */
class LoggingAlertHandler extends AlertHandler {
    public LoggingAlertHandler() { super(AlertLevel.INFO); }

    @Override
    protected void processAlert(Alert alert) {
        switch (alert.getLevel()) {
            case EMERGENCY, CRITICAL -> log.error("[ALERT] {}", alert);
            case WARNING -> log.warn("[ALERT] {}", alert);
            default -> log.info("[ALERT] {}", alert);
        }
    }
}

/**
 * Concrete Handler - Sends notifications
 */
class NotificationAlertHandler extends AlertHandler {
    public NotificationAlertHandler() { super(AlertLevel.WARNING); }

    @Override
    protected void processAlert(Alert alert) {
        log.info("[NOTIFY] Sending notification: {}", alert.getMessage());
    }
}

/**
 * Concrete Handler - Triggers alarm
 */
class AlarmAlertHandler extends AlertHandler {
    public AlarmAlertHandler() { super(AlertLevel.CRITICAL); }

    @Override
    protected void processAlert(Alert alert) {
        log.warn("[ALARM] Triggering alarm for: {}", alert.getMessage());
    }
}

/**
 * Concrete Handler - Calls emergency services
 */
class EmergencyAlertHandler extends AlertHandler {
    public EmergencyAlertHandler() { super(AlertLevel.EMERGENCY); }

    @Override
    protected void processAlert(Alert alert) {
        log.error("[EMERGENCY] Calling emergency services for: {}", alert.getMessage());
    }
}

/**
 * Chain builder
 */
class AlertHandlerChain {
    private static final Logger log = LoggerFactory.getLogger(AlertHandlerChain.class);
    private final AlertHandler firstHandler;

    public AlertHandlerChain() {
        AlertHandler logging = new LoggingAlertHandler();
        AlertHandler notification = new NotificationAlertHandler();
        AlertHandler alarm = new AlarmAlertHandler();
        AlertHandler emergency = new EmergencyAlertHandler();

        logging.setNext(notification).setNext(alarm).setNext(emergency);
        this.firstHandler = logging;
        log.info("Alert handler chain initialized");
    }

    public void processAlert(Alert alert) {
        firstHandler.handle(alert);
    }

    public static Alert createAlert(String deviceId, AlertLevel level, String message) {
        return new Alert("alert-" + System.currentTimeMillis(), deviceId, level, message);
    }
}
