package com.smarthome.pattern.structural.facade;

import com.smarthome.pattern.creational.factory.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * FACADE PATTERN
 *
 * Intent: Provide a unified interface to a set of interfaces in a subsystem.
 * Facade defines a higher-level interface that makes the subsystem easier to use.
 *
 * Smart Home Application: The SmartHomeFacade provides a simple interface to
 * complex subsystems (lighting, security, climate, entertainment). Users can
 * execute common scenarios without understanding the complexity underneath.
 */

// Subsystem classes
class LightingSystem {
    private static final Logger log = LoggerFactory.getLogger(LightingSystem.class);
    private final List<SmartLight> lights = new ArrayList<>();

    public void addLight(SmartLight light) {
        lights.add(light);
    }

    public void turnAllOn() {
        log.info("[Lighting] Turning all lights ON");
        lights.forEach(SmartLight::turnOn);
    }

    public void turnAllOff() {
        log.info("[Lighting] Turning all lights OFF");
        lights.forEach(SmartLight::turnOff);
    }

    public void setAllBrightness(int brightness) {
        log.info("[Lighting] Setting all lights to {}% brightness", brightness);
        lights.forEach(l -> l.setBrightness(brightness));
    }

    public void setAmbientMode() {
        log.info("[Lighting] Setting ambient mode");
        lights.forEach(l -> {
            l.turnOn();
            l.setBrightness(30);
            l.setColor("#FFE4B5");
        });
    }

    public int getLightCount() {
        return lights.size();
    }
}

class SecuritySystem {
    private static final Logger log = LoggerFactory.getLogger(SecuritySystem.class);
    private final List<SmartCamera> cameras = new ArrayList<>();
    private final List<SmartLock> locks = new ArrayList<>();
    private boolean alarmArmed = false;

    public void addCamera(SmartCamera camera) {
        cameras.add(camera);
    }

    public void addLock(SmartLock lock) {
        locks.add(lock);
    }

    public void armSystem() {
        log.info("[Security] Arming security system");
        alarmArmed = true;
        cameras.forEach(c -> {
            c.turnOn();
            c.startRecording();
        });
        locks.forEach(SmartLock::lock);
    }

    public void disarmSystem() {
        log.info("[Security] Disarming security system");
        alarmArmed = false;
        cameras.forEach(SmartCamera::stopRecording);
    }

    public void lockAllDoors() {
        log.info("[Security] Locking all doors");
        locks.forEach(SmartLock::lock);
    }

    public void unlockAllDoors() {
        log.info("[Security] Unlocking all doors");
        locks.forEach(SmartLock::unlock);
    }

    public boolean isArmed() {
        return alarmArmed;
    }
}

class ClimateSystem {
    private static final Logger log = LoggerFactory.getLogger(ClimateSystem.class);
    private final List<SmartThermostat> thermostats = new ArrayList<>();

    public void addThermostat(SmartThermostat thermostat) {
        thermostats.add(thermostat);
    }

    public void setComfortMode() {
        log.info("[Climate] Setting comfort mode (22°C)");
        thermostats.forEach(t -> {
            t.turnOn();
            t.setTargetTemperature(22.0);
            t.setMode("AUTO");
        });
    }

    public void setEcoMode() {
        log.info("[Climate] Setting eco mode (20°C)");
        thermostats.forEach(t -> {
            t.turnOn();
            t.setTargetTemperature(20.0);
            t.setMode("ECO");
        });
    }

    public void turnOff() {
        log.info("[Climate] Turning off climate control");
        thermostats.forEach(SmartThermostat::turnOff);
    }

    public void setTemperature(double temperature) {
        log.info("[Climate] Setting temperature to {}°C", temperature);
        thermostats.forEach(t -> t.setTargetTemperature(temperature));
    }
}

class EntertainmentSystem {
    private static final Logger log = LoggerFactory.getLogger(EntertainmentSystem.class);
    private boolean tvOn = false;
    private boolean musicOn = false;
    private int volume = 30;

    public void turnOnTV() {
        tvOn = true;
        log.info("[Entertainment] TV turned ON");
    }

    public void turnOffTV() {
        tvOn = false;
        log.info("[Entertainment] TV turned OFF");
    }

    public void playMusic() {
        musicOn = true;
        log.info("[Entertainment] Music playing");
    }

    public void stopMusic() {
        musicOn = false;
        log.info("[Entertainment] Music stopped");
    }

    public void setVolume(int volume) {
        this.volume = volume;
        log.info("[Entertainment] Volume set to {}", volume);
    }

    public void turnOffAll() {
        turnOffTV();
        stopMusic();
    }

    public boolean isTvOn() {
        return tvOn;
    }

    public boolean isMusicOn() {
        return musicOn;
    }
}

/**
 * Facade - Provides simplified interface to all subsystems
 */
public class SmartHomeFacade {
    private static final Logger log = LoggerFactory.getLogger(SmartHomeFacade.class);
    private final LightingSystem lighting;
    private final SecuritySystem security;
    private final ClimateSystem climate;
    private final EntertainmentSystem entertainment;

    public SmartHomeFacade() {
        this.lighting = new LightingSystem();
        this.security = new SecuritySystem();
        this.climate = new ClimateSystem();
        this.entertainment = new EntertainmentSystem();
    }

    // Convenient methods to add devices
    public void addLight(SmartLight light) {
        lighting.addLight(light);
    }

    public void addCamera(SmartCamera camera) {
        security.addCamera(camera);
    }

    public void addLock(SmartLock lock) {
        security.addLock(lock);
    }

    public void addThermostat(SmartThermostat thermostat) {
        climate.addThermostat(thermostat);
    }

    // ========== SCENE METHODS (High-level operations) ==========

    /**
     * "Good Morning" scene - wake up routine
     */
    public void goodMorning() {
        log.info("=== Activating 'Good Morning' Scene ===");
        lighting.turnAllOn();
        lighting.setAllBrightness(70);
        climate.setComfortMode();
        security.disarmSystem();
        entertainment.playMusic();
        entertainment.setVolume(20);
    }

    /**
     * "Good Night" scene - bedtime routine
     */
    public void goodNight() {
        log.info("=== Activating 'Good Night' Scene ===");
        lighting.turnAllOff();
        climate.setEcoMode();
        security.armSystem();
        entertainment.turnOffAll();
    }

    /**
     * "Leave Home" scene - away routine
     */
    public void leaveHome() {
        log.info("=== Activating 'Leave Home' Scene ===");
        lighting.turnAllOff();
        climate.setEcoMode();
        security.armSystem();
        security.lockAllDoors();
        entertainment.turnOffAll();
    }

    /**
     * "Arrive Home" scene - welcome routine
     */
    public void arriveHome() {
        log.info("=== Activating 'Arrive Home' Scene ===");
        security.disarmSystem();
        security.unlockAllDoors();
        lighting.turnAllOn();
        climate.setComfortMode();
    }

    /**
     * "Movie Night" scene
     */
    public void movieNight() {
        log.info("=== Activating 'Movie Night' Scene ===");
        lighting.setAmbientMode();
        entertainment.turnOnTV();
        entertainment.setVolume(40);
        climate.setComfortMode();
    }

    /**
     * "Party Mode" scene
     */
    public void partyMode() {
        log.info("=== Activating 'Party Mode' Scene ===");
        lighting.turnAllOn();
        lighting.setAllBrightness(100);
        entertainment.playMusic();
        entertainment.setVolume(70);
        climate.setComfortMode();
    }

    /**
     * Emergency - panic button
     */
    public void panicButton() {
        log.warn("!!! PANIC BUTTON ACTIVATED !!!");
        lighting.turnAllOn();
        lighting.setAllBrightness(100);
        security.armSystem();
        security.lockAllDoors();
    }

    // ========== INDIVIDUAL SUBSYSTEM ACCESS ==========

    public LightingSystem getLighting() {
        return lighting;
    }

    public SecuritySystem getSecurity() {
        return security;
    }

    public ClimateSystem getClimate() {
        return climate;
    }

    public EntertainmentSystem getEntertainment() {
        return entertainment;
    }

    /**
     * Get overall home status
     */
    public String getHomeStatus() {
        return String.format(
                "Smart Home Status:\n" +
                "  Lighting: %d lights configured\n" +
                "  Security: %s\n" +
                "  Entertainment: TV %s, Music %s",
                lighting.getLightCount(),
                security.isArmed() ? "ARMED" : "DISARMED",
                entertainment.isTvOn() ? "ON" : "OFF",
                entertainment.isMusicOn() ? "ON" : "OFF"
        );
    }
}
