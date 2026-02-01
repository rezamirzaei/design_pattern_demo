package com.smarthome.pattern.behavioral.visitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * VISITOR PATTERN
 *
 * Intent: Represent an operation to be performed on the elements of an object
 * structure. Visitor lets you define a new operation without changing the
 * classes of the elements on which it operates.
 */

/**
 * Visitor interface
 */
public interface DeviceVisitor {
    void visitLight(VisitableLight light);
    void visitThermostat(VisitableThermostat thermostat);
    void visitCamera(VisitableCamera camera);
    void visitLock(VisitableLock lock);
    String getReport();
}

/**
 * Element interface - Visitable device
 */
interface VisitableDevice {
    void accept(DeviceVisitor visitor);
    String getName();
    String getLocation();
    boolean isOn();
    double getPowerConsumption();
}

/**
 * Concrete Element - Visitable Light
 */
class VisitableLight implements VisitableDevice {
    private final String name;
    private final String location;
    private boolean isOn;
    private int brightness = 80;
    private int hoursUsed = 5000;
    private int bulbLifeHours = 25000;

    public VisitableLight(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void accept(DeviceVisitor visitor) { visitor.visitLight(this); }
    @Override
    public String getName() { return name; }
    @Override
    public String getLocation() { return location; }
    @Override
    public boolean isOn() { return isOn; }
    @Override
    public double getPowerConsumption() { return isOn ? brightness * 0.1 : 0; }

    public int getBrightness() { return brightness; }
    public int getHoursUsed() { return hoursUsed; }
    public double getRemainingLifePercent() {
        return ((bulbLifeHours - hoursUsed) * 100.0) / bulbLifeHours;
    }
}

/**
 * Concrete Element - Visitable Thermostat
 */
class VisitableThermostat implements VisitableDevice {
    private final String name;
    private final String location;
    private boolean isOn;
    private double targetTemp = 22.0;
    private int filterDaysRemaining = 45;

    public VisitableThermostat(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void accept(DeviceVisitor visitor) { visitor.visitThermostat(this); }
    @Override
    public String getName() { return name; }
    @Override
    public String getLocation() { return location; }
    @Override
    public boolean isOn() { return isOn; }
    @Override
    public double getPowerConsumption() { return isOn ? 50 : 5; }

    public double getTargetTemp() { return targetTemp; }
    public int getFilterDaysRemaining() { return filterDaysRemaining; }
}

/**
 * Concrete Element - Visitable Camera
 */
class VisitableCamera implements VisitableDevice {
    private final String name;
    private final String location;
    private boolean isOn;
    private String firmwareVersion = "2.1.0";
    private boolean firmwareUpToDate = true;
    private int storageUsedPercent = 65;

    public VisitableCamera(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void accept(DeviceVisitor visitor) { visitor.visitCamera(this); }
    @Override
    public String getName() { return name; }
    @Override
    public String getLocation() { return location; }
    @Override
    public boolean isOn() { return isOn; }
    @Override
    public double getPowerConsumption() { return isOn ? 15 : 2; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public boolean isFirmwareUpToDate() { return firmwareUpToDate; }
    public int getStorageUsedPercent() { return storageUsedPercent; }
}

/**
 * Concrete Element - Visitable Lock
 */
class VisitableLock implements VisitableDevice {
    private final String name;
    private final String location;
    private boolean isLocked = true;
    private int batteryPercent = 75;
    private int failedAttempts = 2;

    public VisitableLock(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public void accept(DeviceVisitor visitor) { visitor.visitLock(this); }
    @Override
    public String getName() { return name; }
    @Override
    public String getLocation() { return location; }
    @Override
    public boolean isOn() { return isLocked; }
    @Override
    public double getPowerConsumption() { return 1; }

    public int getBatteryPercent() { return batteryPercent; }
    public int getFailedAttempts() { return failedAttempts; }
}

/**
 * Concrete Visitor - Maintenance Check
 */
class MaintenanceVisitor implements DeviceVisitor {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceVisitor.class);
    private final List<String> issues = new ArrayList<>();
    private int devicesChecked = 0;

    @Override
    public void visitLight(VisitableLight light) {
        devicesChecked++;
        log.info("[Maintenance] Checking light: {}", light.getName());
        if (light.getRemainingLifePercent() < 20) {
            issues.add("Light '" + light.getName() + "' bulb needs replacement");
        }
    }

    @Override
    public void visitThermostat(VisitableThermostat thermostat) {
        devicesChecked++;
        log.info("[Maintenance] Checking thermostat: {}", thermostat.getName());
        if (thermostat.getFilterDaysRemaining() < 14) {
            issues.add("Thermostat '" + thermostat.getName() + "' filter needs replacement");
        }
    }

    @Override
    public void visitCamera(VisitableCamera camera) {
        devicesChecked++;
        log.info("[Maintenance] Checking camera: {}", camera.getName());
        if (!camera.isFirmwareUpToDate()) {
            issues.add("Camera '" + camera.getName() + "' needs firmware update");
        }
        if (camera.getStorageUsedPercent() > 80) {
            issues.add("Camera '" + camera.getName() + "' storage nearly full");
        }
    }

    @Override
    public void visitLock(VisitableLock lock) {
        devicesChecked++;
        log.info("[Maintenance] Checking lock: {}", lock.getName());
        if (lock.getBatteryPercent() < 20) {
            issues.add("Lock '" + lock.getName() + "' battery low");
        }
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MAINTENANCE REPORT ===\n");
        sb.append("Devices Checked: ").append(devicesChecked).append("\n");
        sb.append("Issues Found: ").append(issues.size()).append("\n");
        for (String issue : issues) {
            sb.append("  - ").append(issue).append("\n");
        }
        return sb.toString();
    }
}

/**
 * Concrete Visitor - Energy Audit
 */
class EnergyAuditVisitor implements DeviceVisitor {
    private static final Logger log = LoggerFactory.getLogger(EnergyAuditVisitor.class);
    private double totalPower = 0;
    private final List<String> recommendations = new ArrayList<>();

    @Override
    public void visitLight(VisitableLight light) {
        double power = light.getPowerConsumption();
        totalPower += power;
        log.info("[Energy] Light {} consuming {} W", light.getName(), power);
        if (light.getBrightness() > 80 && light.isOn()) {
            recommendations.add("Reduce brightness of '" + light.getName() + "'");
        }
    }

    @Override
    public void visitThermostat(VisitableThermostat thermostat) {
        double power = thermostat.getPowerConsumption();
        totalPower += power;
        log.info("[Energy] Thermostat {} consuming {} W", thermostat.getName(), power);
        if (thermostat.getTargetTemp() > 23) {
            recommendations.add("Lower target temp of '" + thermostat.getName() + "'");
        }
    }

    @Override
    public void visitCamera(VisitableCamera camera) {
        double power = camera.getPowerConsumption();
        totalPower += power;
        log.info("[Energy] Camera {} consuming {} W", camera.getName(), power);
    }

    @Override
    public void visitLock(VisitableLock lock) {
        double power = lock.getPowerConsumption();
        totalPower += power;
        log.info("[Energy] Lock {} consuming {} W", lock.getName(), power);
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ENERGY AUDIT REPORT ===\n");
        sb.append("Total Power Consumption: ").append(totalPower).append(" W\n");
        sb.append("Recommendations: ").append(recommendations.size()).append("\n");
        for (String rec : recommendations) {
            sb.append("  - ").append(rec).append("\n");
        }
        return sb.toString();
    }
}

/**
 * Concrete Visitor - Security Audit
 */
class SecurityAuditVisitor implements DeviceVisitor {
    private static final Logger log = LoggerFactory.getLogger(SecurityAuditVisitor.class);
    private final List<String> alerts = new ArrayList<>();
    private int securityScore = 100;

    @Override
    public void visitLight(VisitableLight light) {
        log.info("[Security] Checking light: {}", light.getName());
    }

    @Override
    public void visitThermostat(VisitableThermostat thermostat) {
        log.info("[Security] Checking thermostat: {}", thermostat.getName());
    }

    @Override
    public void visitCamera(VisitableCamera camera) {
        log.info("[Security] Checking camera: {}", camera.getName());
        if (!camera.isFirmwareUpToDate()) {
            alerts.add("Camera '" + camera.getName() + "' has outdated firmware");
            securityScore -= 20;
        }
    }

    @Override
    public void visitLock(VisitableLock lock) {
        log.info("[Security] Checking lock: {}", lock.getName());
        if (lock.getFailedAttempts() > 3) {
            alerts.add("Lock '" + lock.getName() + "' has multiple failed attempts");
            securityScore -= 30;
        }
        if (lock.getBatteryPercent() < 10) {
            alerts.add("Lock '" + lock.getName() + "' battery critical");
            securityScore -= 15;
        }
    }

    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SECURITY AUDIT REPORT ===\n");
        sb.append("Security Score: ").append(securityScore).append("/100\n");
        sb.append("Alerts: ").append(alerts.size()).append("\n");
        for (String alert : alerts) {
            sb.append("  - ").append(alert).append("\n");
        }
        return sb.toString();
    }
}
