package com.smarthome.pattern.creational.singleton;

import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.creational.factory.Device;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class HomeController {
    public static final HomeController INSTANCE = new HomeController();

    private final AtomicReference<HomeMode> homeMode = new AtomicReference<>(HomeMode.NORMAL);
    private final Map<String, Device> devices = new ConcurrentHashMap<>();

    private HomeController() {}

    public HomeMode getHomeModeEnum() {
        return homeMode.get();
    }

    public String getHomeMode() {
        return homeMode.get().name();
    }

    public void setHomeMode(HomeMode mode) {
        homeMode.set(mode);
    }

    public void setHomeMode(String mode) {
        if (mode == null) {
            homeMode.set(HomeMode.NORMAL);
            return;
        }
        try {
            homeMode.set(HomeMode.valueOf(mode.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            homeMode.set(HomeMode.NORMAL);
        }
    }

    public void registerDevice(String id, Device device) {
        if (id == null || id.isBlank() || device == null) {
            return;
        }
        devices.put(id, device);
    }

    public Device getDevice(String id) {
        return devices.get(id);
    }

    public Map<String, Device> getDevicesSnapshot() {
        return Map.copyOf(devices);
    }
}
