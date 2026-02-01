package com.smarthome.pattern.behavioral.observer;

import java.util.Map;

public final class ObserverDemo {
    private ObserverDemo() {}

    public static Map<String, Object> demo() {
        ObservableDevice device = new ObservableDevice("obs-1", "Front Door Sensor");

        MobileAppObserver mobile = new MobileAppObserver("user-1", "token-abc");
        DashboardObserver dashboard = new DashboardObserver("session-1");
        AnalyticsObserver analytics = new AnalyticsObserver();
        EmailObserver email = new EmailObserver("alerts@example.com");

        device.addObserver(mobile);
        device.addObserver(dashboard);
        device.addObserver(analytics);
        device.addObserver(email);

        device.triggerMotion();
        device.setState("ON");

        return Map.of(
                "pattern", "Observer",
                "device", device.getName(),
                "observerCount", device.getObserverCount(),
                "mobileNotifications", mobile.getNotificationCount(),
                "analyticsEvents", analytics.getEventCount(),
                "dashboardLastEvent", dashboard.getLastEvent()
        );
    }
}

