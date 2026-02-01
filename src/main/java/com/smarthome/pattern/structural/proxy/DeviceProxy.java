package com.smarthome.pattern.structural.proxy;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PROXY PATTERN
 *
 * Intent: Provide a surrogate or placeholder for another object to control access to it.
 *
 * Smart Home Application: The DeviceProxy controls access to remote/expensive devices:
 * - Virtual Proxy: Lazy initialization of device connection
 * - Protection Proxy: Access control based on user permissions
 * - Remote Proxy: Represents a device on another network/hub
 * - Caching Proxy: Caches device state to reduce network calls
 */

/**
 * Remote Device Interface - represents a device that might be remote/expensive to access
 */
interface RemoteDevice extends Device {
    boolean isConnected();
    void connect();
    void disconnect();
    String getRemoteAddress();
}

/**
 * Real Subject - Actual remote device implementation
 */
class RealRemoteDevice implements RemoteDevice {
    private static final Logger log = LoggerFactory.getLogger(RealRemoteDevice.class);
    private final String deviceId;
    private final String name;
    private final String remoteAddress;
    private boolean isConnected = false;
    private boolean isOn = false;

    public RealRemoteDevice(String deviceId, String name, String remoteAddress) {
        this.deviceId = deviceId;
        this.name = name;
        this.remoteAddress = remoteAddress;
        // Simulate expensive initialization
        log.info("Initializing connection to remote device at {}", remoteAddress);
        simulateNetworkDelay();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(100); // Simulate network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void connect() {
        log.info("Connecting to {} at {}", name, remoteAddress);
        simulateNetworkDelay();
        isConnected = true;
        log.info("Connected to {}", name);
    }

    @Override
    public void disconnect() {
        log.info("Disconnecting from {}", name);
        isConnected = false;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public void turnOn() {
        if (!isConnected) connect();
        log.info("Turning ON remote device: {}", name);
        isOn = true;
    }

    @Override
    public void turnOff() {
        if (!isConnected) connect();
        log.info("Turning OFF remote device: {}", name);
        isOn = false;
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getStatus() {
        return String.format("Remote Device %s: %s, Connected: %s",
                name, isOn ? "ON" : "OFF", isConnected);
    }

    @Override
    public String getDeviceInfo() {
        return String.format("Remote Device [%s] @ %s", name, remoteAddress);
    }

    @Override
    public double getPowerConsumption() {
        return isOn ? 10.0 : 0.0;
    }

    @Override
    public void operate(String command) {
        if (!isConnected) connect();
        log.info("Remote device {} executing command: {}", name, command);
    }
}

/**
 * Proxy - Controls access to the real remote device
 */
public class DeviceProxy implements RemoteDevice {
    private static final Logger log = LoggerFactory.getLogger(DeviceProxy.class);
    private final String deviceId;
    private final String name;
    private final String remoteAddress;
    private RealRemoteDevice realDevice; // Lazy initialization
    private String cachedStatus;
    private long lastStatusTime = 0;
    private static final long CACHE_DURATION_MS = 5000;

    // Access control
    private String currentUser;
    private AccessLevel accessLevel = AccessLevel.GUEST;

    public enum AccessLevel {
        GUEST(false, true),    // Can view only
        USER(true, true),      // Can control
        ADMIN(true, true);     // Full access

        final boolean canControl;
        final boolean canView;

        AccessLevel(boolean canControl, boolean canView) {
            this.canControl = canControl;
            this.canView = canView;
        }
    }

    public DeviceProxy(String deviceId, String name, String remoteAddress) {
        this.deviceId = deviceId;
        this.name = name;
        this.remoteAddress = remoteAddress;
        log.info("Created proxy for device: {} (lazy initialization)", name);
    }

    /**
     * Set the access level for the current session
     */
    public void setAccess(String user, AccessLevel level) {
        this.currentUser = user;
        this.accessLevel = level;
        log.info("Access set for user '{}': {}", user, level);
    }

    /**
     * Lazy initialization - create real device only when needed
     */
    private RealRemoteDevice getRealDevice() {
        if (realDevice == null) {
            log.info("Lazy initialization: Creating real device connection");
            realDevice = new RealRemoteDevice(deviceId, name, remoteAddress);
        }
        return realDevice;
    }

    private boolean checkControlAccess(String operation) {
        if (!accessLevel.canControl) {
            log.warn("Access denied: User '{}' with level {} cannot perform: {}",
                    currentUser, accessLevel, operation);
            return false;
        }
        return true;
    }

    @Override
    public void turnOn() {
        if (checkControlAccess("turnOn")) {
            getRealDevice().turnOn();
            invalidateCache();
        }
    }

    @Override
    public void turnOff() {
        if (checkControlAccess("turnOff")) {
            getRealDevice().turnOff();
            invalidateCache();
        }
    }

    @Override
    public boolean isOn() {
        return getRealDevice().isOn();
    }

    @Override
    public String getStatus() {
        // Caching proxy - return cached status if still valid
        if (cachedStatus != null && System.currentTimeMillis() - lastStatusTime < CACHE_DURATION_MS) {
            log.debug("Returning cached status for {}", name);
            return "[Cached] " + cachedStatus;
        }

        cachedStatus = getRealDevice().getStatus();
        lastStatusTime = System.currentTimeMillis();
        return cachedStatus;
    }

    @Override
    public String getDeviceInfo() {
        return String.format("[Proxy] Remote Device [%s] @ %s", name, remoteAddress);
    }

    @Override
    public double getPowerConsumption() {
        return getRealDevice().getPowerConsumption();
    }

    @Override
    public void operate(String command) {
        if (checkControlAccess("operate:" + command)) {
            getRealDevice().operate(command);
            invalidateCache();
        }
    }

    @Override
    public boolean isConnected() {
        return realDevice != null && realDevice.isConnected();
    }

    @Override
    public void connect() {
        getRealDevice().connect();
    }

    @Override
    public void disconnect() {
        if (realDevice != null) {
            realDevice.disconnect();
        }
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    private void invalidateCache() {
        cachedStatus = null;
        lastStatusTime = 0;
    }

    public boolean isInitialized() {
        return realDevice != null;
    }
}
