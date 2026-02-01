package com.smarthome.domain;

public enum DeviceType {
    LIGHT("💡", "Lighting", 12),
    THERMOSTAT("🌡️", "Climate", 5),
    CAMERA("📷", "Security", 8),
    LOCK("🔒", "Security", 2),
    SENSOR("🧭", "Sensors", 1);

    private final String icon;
    private final String category;
    private final int defaultRatedPowerWatts;

    DeviceType(String icon, String category, int defaultRatedPowerWatts) {
        this.icon = icon;
        this.category = category;
        this.defaultRatedPowerWatts = defaultRatedPowerWatts;
    }

    public String getIcon() {
        return icon;
    }

    public String getCategory() {
        return category;
    }

    public int getDefaultRatedPowerWatts() {
        return defaultRatedPowerWatts;
    }
}

