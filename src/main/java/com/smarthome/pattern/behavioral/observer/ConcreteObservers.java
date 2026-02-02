package com.smarthome.pattern.behavioral.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Observer - Mobile App notification handler
 */
class MobileAppObserver implements DeviceObserver {
    private static final Logger log = LoggerFactory.getLogger(MobileAppObserver.class);
    private final String userId;
    private final String deviceToken;
    private int notificationCount = 0;

    public MobileAppObserver(String userId, String deviceToken) {
        this.userId = userId;
        this.deviceToken = deviceToken;
    }

    @Override
    public void onDeviceEvent(String eventType, String data) {
        notificationCount++;
        log.info("[MobileApp] Push notification to user '{}': {} - {}", userId, eventType, data);
    }

    @Override
    public String getObserverName() {
        return "MobileApp-" + userId;
    }

    public int getNotificationCount() {
        return notificationCount;
    }
}

/**
 * Concrete Observer - Dashboard/Web UI handler
 */
class DashboardObserver implements DeviceObserver {
    private static final Logger log = LoggerFactory.getLogger(DashboardObserver.class);
    private final String sessionId;
    private String lastEvent;

    public DashboardObserver(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void onDeviceEvent(String eventType, String data) {
        lastEvent = eventType;
        log.info("[Dashboard] Update widget for session '{}': {} - {}", sessionId, eventType, data);
    }

    @Override
    public String getObserverName() {
        return "Dashboard-" + sessionId;
    }

    public String getLastEvent() {
        return lastEvent;
    }
}

/**
 * Concrete Observer - Analytics/Logging handler
 */
class AnalyticsObserver implements DeviceObserver {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsObserver.class);
    private int eventCount = 0;

    @Override
    public void onDeviceEvent(String eventType, String data) {
        eventCount++;
        log.info("[Analytics] Event #{}: {} - {}", eventCount, eventType, data);
    }

    @Override
    public String getObserverName() {
        return "Analytics";
    }

    public int getEventCount() {
        return eventCount;
    }
}

/**
 * Concrete Observer - Email notification handler
 */
class EmailObserver implements DeviceObserver {
    private static final Logger log = LoggerFactory.getLogger(EmailObserver.class);
    private final String email;

    public EmailObserver(String email) {
        this.email = email;
    }

    @Override
    public void onDeviceEvent(String eventType, String data) {
        log.info("[Email] Sending email to '{}': {} - {}", email, eventType, data);
    }

    @Override
    public String getObserverName() {
        return "Email-" + email;
    }
}