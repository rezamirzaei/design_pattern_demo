package com.smarthome.pattern.structural.decorator;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Decorator - Adds security/authentication check to device operations
 */
public class SecurityDecorator extends DeviceDecorator {
    private static final Logger log = LoggerFactory.getLogger(SecurityDecorator.class);
    private String authorizedUser;
    private boolean isAuthenticated = false;

    public SecurityDecorator(Device device) {
        super(device);
    }

    /**
     * Authenticate a user before allowing operations
     */
    public void authenticate(String user, String password) {
        // Simplified authentication - in real world, use proper auth
        if (password != null && password.length() >= 4) {
            this.authorizedUser = user;
            this.isAuthenticated = true;
            log.info("[SECURITY] User '{}' authenticated for device: {}",
                    user, wrappedDevice.getDeviceInfo());
        } else {
            log.warn("[SECURITY] Authentication failed for user: {}", user);
        }
    }

    /**
     * Logout current user
     */
    public void logout() {
        log.info("[SECURITY] User '{}' logged out", authorizedUser);
        this.authorizedUser = null;
        this.isAuthenticated = false;
    }

    @Override
    public void turnOn() {
        if (checkAccess("turnOn")) {
            super.turnOn();
        }
    }

    @Override
    public void turnOff() {
        if (checkAccess("turnOff")) {
            super.turnOff();
        }
    }

    @Override
    public void operate(String command) {
        if (checkAccess("operate")) {
            super.operate(command);
        }
    }

    @Override
    public String getDeviceInfo() {
        return "[Secured] " + super.getDeviceInfo();
    }

    private boolean checkAccess(String operation) {
        if (!isAuthenticated) {
            log.warn("[SECURITY] Access denied - not authenticated. Device: {}, Operation: {}",
                    wrappedDevice.getDeviceInfo(), operation);
            return false;
        }
        log.debug("[SECURITY] Access granted to '{}' for operation: {}", authorizedUser, operation);
        return true;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String getAuthorizedUser() {
        return authorizedUser;
    }
}
