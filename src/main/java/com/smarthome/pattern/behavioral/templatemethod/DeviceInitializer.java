package com.smarthome.pattern.behavioral.templatemethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TEMPLATE METHOD PATTERN
 *
 * Intent: Define the skeleton of an algorithm in an operation, deferring some
 * steps to subclasses. Template Method lets subclasses redefine certain steps
 * of an algorithm without changing the algorithm's structure.
 *
 * Smart Home Application: Device initialization follows a common pattern but
 * with device-specific steps. The template method defines the overall init
 * process while subclasses implement device-specific details.
 */

/**
 * Abstract class with template method
 */
public abstract class DeviceInitializer {
    protected static final Logger log = LoggerFactory.getLogger(DeviceInitializer.class);

    /**
     * Template method - defines the algorithm structure
     */
    public final void initializeDevice() {
        log.info("=== Starting device initialization ===");

        // Step 1: Connect to device (common)
        connect();

        // Step 2: Authenticate (common)
        authenticate();

        // Step 3: Check firmware (device-specific)
        checkFirmware();

        // Step 4: Load configuration (device-specific)
        loadConfiguration();

        // Step 5: Perform self-test (device-specific)
        performSelfTest();

        // Step 5b: Calibrate device (if needed)
        if (needsCalibration()) {
            calibrate();
        }

        // Step 6: Hook for additional initialization (optional)
        additionalSetup();

        // Step 7: Register with home controller (common)
        registerDevice();

        // Step 8: Ready notification (common)
        notifyReady();

        log.info("=== Device initialization complete ===");
    }

    // Common steps (concrete implementations)
    protected void connect() {
        log.info("Step 1: Connecting to device...");
        // Common connection logic
    }

    protected void authenticate() {
        log.info("Step 2: Authenticating device...");
        // Common auth logic
    }

    protected void registerDevice() {
        log.info("Step 7: Registering with home controller...");
        // Common registration
    }

    protected void notifyReady() {
        log.info("Step 8: Device ready - sending notification");
    }

    // Abstract methods - must be implemented by subclasses
    protected abstract void checkFirmware();
    protected abstract void loadConfiguration();
    protected abstract void performSelfTest();

    // Hook methods - optional override
    protected boolean needsCalibration() {
        return false;
    }

    protected void calibrate() {
        log.info("Step X: Calibrating device...");
    }

    protected void additionalSetup() {
        // Default: do nothing
    }

    // Template method for getting device type
    public abstract String getDeviceType();
}

/**
 * Concrete class - Light initializer
 */
class LightInitializer extends DeviceInitializer {
    private final String lightId;

    public LightInitializer(String lightId) {
        this.lightId = lightId;
    }

    @Override
    protected void checkFirmware() {
        log.info("Step 3: Checking light firmware (v2.1.0)...");
    }

    @Override
    protected void loadConfiguration() {
        log.info("Step 4: Loading light configuration...");
        log.info("  - Default brightness: 80%");
        log.info("  - Default color: Warm White");
        log.info("  - Transition time: 500ms");
    }

    @Override
    protected void performSelfTest() {
        log.info("Step 5: Performing light self-test...");
        log.info("  - Testing ON/OFF cycle");
        log.info("  - Testing brightness range");
        log.info("  - Testing color modes");
        log.info("  - Self-test PASSED");
    }

    @Override
    protected void additionalSetup() {
        log.info("Step 6: Setting up light schedules...");
        log.info("  - Sunrise simulation: 6:30 AM");
        log.info("  - Auto-off: 11:00 PM");
    }

    @Override
    public String getDeviceType() {
        return "Smart Light";
    }
}

/**
 * Concrete class - Thermostat initializer
 */
class ThermostatInitializer extends DeviceInitializer {
    private final String thermostatId;

    public ThermostatInitializer(String thermostatId) {
        this.thermostatId = thermostatId;
    }

    @Override
    protected void checkFirmware() {
        log.info("Step 3: Checking thermostat firmware (v3.0.5)...");
        log.info("  - HVAC compatibility check...");
    }

    @Override
    protected void loadConfiguration() {
        log.info("Step 4: Loading thermostat configuration...");
        log.info("  - Temperature scale: Celsius");
        log.info("  - Default target: 22°C");
        log.info("  - Eco setback: 18°C");
    }

    @Override
    protected void performSelfTest() {
        log.info("Step 5: Performing thermostat self-test...");
        log.info("  - Testing temperature sensor");
        log.info("  - Testing humidity sensor");
        log.info("  - Testing HVAC communication");
        log.info("  - Self-test PASSED");
    }

    @Override
    protected boolean needsCalibration() {
        return true;
    }

    @Override
    protected void calibrate() {
        log.info("Step 5b: Calibrating temperature sensor...");
    }

    @Override
    protected void additionalSetup() {
        log.info("Step 6: Loading heating/cooling schedule...");
        log.info("  - Weekday schedule loaded");
        log.info("  - Weekend schedule loaded");
    }

    @Override
    public String getDeviceType() {
        return "Smart Thermostat";
    }
}

/**
 * Concrete class - Camera initializer
 */
class CameraInitializer extends DeviceInitializer {
    private final String cameraId;

    public CameraInitializer(String cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    protected void checkFirmware() {
        log.info("Step 3: Checking camera firmware (v4.2.1)...");
        log.info("  - Security patch level: Current");
    }

    @Override
    protected void loadConfiguration() {
        log.info("Step 4: Loading camera configuration...");
        log.info("  - Resolution: 1080p");
        log.info("  - Frame rate: 30fps");
        log.info("  - Night vision: Enabled");
        log.info("  - Motion sensitivity: Medium");
    }

    @Override
    protected void performSelfTest() {
        log.info("Step 5: Performing camera self-test...");
        log.info("  - Testing video capture");
        log.info("  - Testing microphone");
        log.info("  - Testing IR LEDs");
        log.info("  - Testing motion detection");
        log.info("  - Self-test PASSED");
    }

    @Override
    protected boolean needsCalibration() {
        return true;
    }

    @Override
    protected void calibrate() {
        log.info("Step 5b: Calibrating camera lens...");
    }

    @Override
    protected void additionalSetup() {
        log.info("Step 6: Configuring motion zones...");
        log.info("  - Zone 1: Entry area - High sensitivity");
        log.info("  - Zone 2: Window area - Medium sensitivity");
    }

    @Override
    public String getDeviceType() {
        return "Smart Camera";
    }
}

/**
 * Concrete class - Lock initializer
 */
class LockInitializer extends DeviceInitializer {
    private final String lockId;

    public LockInitializer(String lockId) {
        this.lockId = lockId;
    }

    @Override
    protected void checkFirmware() {
        log.info("Step 3: Checking lock firmware (v1.5.0)...");
        log.info("  - Encryption module: AES-256");
    }

    @Override
    protected void loadConfiguration() {
        log.info("Step 4: Loading lock configuration...");
        log.info("  - Auto-lock: Enabled (30 seconds)");
        log.info("  - Keypad enabled: Yes");
        log.info("  - Access codes: 3 configured");
    }

    @Override
    protected void performSelfTest() {
        log.info("Step 5: Performing lock self-test...");
        log.info("  - Testing motor mechanism");
        log.info("  - Testing door sensor");
        log.info("  - Testing tamper detection");
        log.info("  - Self-test PASSED");
    }

    @Override
    protected void additionalSetup() {
        log.info("Step 6: Setting up lock access codes...");
    }

    @Override
    public String getDeviceType() {
        return "Smart Lock";
    }
}