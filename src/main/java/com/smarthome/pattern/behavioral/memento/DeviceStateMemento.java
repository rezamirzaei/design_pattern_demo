package com.smarthome.pattern.behavioral.memento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MEMENTO PATTERN
 *
 * Intent: Without violating encapsulation, capture and externalize an object's
 * internal state so that the object can be restored to this state later.
 *
 * Smart Home Application: Save device states as "scenes" that can be restored
 * later. For example, save a "Movie Night" scene and restore it whenever needed.
 */

/**
 * Memento - Stores device state snapshot
 */
public class DeviceStateMemento {
    private final String sceneName;
    private final Map<String, DeviceSnapshot> deviceStates;
    private final LocalDateTime timestamp;

    public DeviceStateMemento(String sceneName, Map<String, DeviceSnapshot> deviceStates) {
        this.sceneName = sceneName;
        this.deviceStates = new HashMap<>(deviceStates);
        this.timestamp = LocalDateTime.now();
    }

    String getSceneName() {
        return sceneName;
    }

    Map<String, DeviceSnapshot> getDeviceStates() {
        return new HashMap<>(deviceStates);
    }

    LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("Scene[%s] saved at %s with %d devices",
                sceneName, timestamp.format(formatter), deviceStates.size());
    }
}

/**
 * Device state snapshot
 */
class DeviceSnapshot {
    final boolean isOn;
    final Map<String, Object> properties;

    DeviceSnapshot(boolean isOn, Map<String, Object> properties) {
        this.isOn = isOn;
        this.properties = new HashMap<>(properties);
    }
}

/**
 * Originator - Smart device that can save/restore state
 */
class SceneDevice {
    private static final Logger log = LoggerFactory.getLogger(SceneDevice.class);
    private final String deviceId;
    private final String name;
    private boolean isOn;
    private final Map<String, Object> properties;

    public SceneDevice(String deviceId, String name) {
        this.deviceId = deviceId;
        this.name = name;
        this.isOn = false;
        this.properties = new HashMap<>();
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
        log.debug("Device '{}' property {} = {}", name, key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setOn(boolean on) {
        this.isOn = on;
        log.info("Device '{}' turned {}", name, on ? "ON" : "OFF");
    }

    public boolean isOn() {
        return isOn;
    }

    /**
     * Save current state to memento
     */
    DeviceSnapshot saveState() {
        return new DeviceSnapshot(isOn, properties);
    }

    /**
     * Restore state from memento
     */
    void restoreState(DeviceSnapshot snapshot) {
        this.isOn = snapshot.isOn;
        this.properties.clear();
        this.properties.putAll(snapshot.properties);
        log.info("Device '{}' state restored - ON: {}, properties: {}",
                name, isOn, properties);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return String.format("%s: %s, %s", name, isOn ? "ON" : "OFF", properties);
    }
}

/**
 * Caretaker - Manages scene mementos
 */
class SceneManager {
    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);
    private final Map<String, SceneDevice> devices = new HashMap<>();
    private final Map<String, DeviceStateMemento> savedScenes = new HashMap<>();
    private final List<DeviceStateMemento> history = new ArrayList<>();
    private static final int MAX_HISTORY = 10;

    public void registerDevice(SceneDevice device) {
        devices.put(device.getDeviceId(), device);
        log.info("Device registered for scene management: {}", device.getName());
    }

    /**
     * Save current state of all devices as a scene
     */
    public DeviceStateMemento saveScene(String sceneName) {
        Map<String, DeviceSnapshot> states = new HashMap<>();
        for (Map.Entry<String, SceneDevice> entry : devices.entrySet()) {
            states.put(entry.getKey(), entry.getValue().saveState());
        }

        DeviceStateMemento memento = new DeviceStateMemento(sceneName, states);
        savedScenes.put(sceneName, memento);
        addToHistory(memento);

        log.info("Scene saved: {}", memento);
        return memento;
    }

    /**
     * Restore a saved scene
     */
    public void restoreScene(String sceneName) {
        DeviceStateMemento memento = savedScenes.get(sceneName);
        if (memento == null) {
            log.warn("Scene not found: {}", sceneName);
            return;
        }

        log.info("Restoring scene: {}", sceneName);
        Map<String, DeviceSnapshot> states = memento.getDeviceStates();

        for (Map.Entry<String, DeviceSnapshot> entry : states.entrySet()) {
            SceneDevice device = devices.get(entry.getKey());
            if (device != null) {
                device.restoreState(entry.getValue());
            }
        }

        log.info("Scene '{}' restored successfully", sceneName);
    }

    /**
     * Delete a saved scene
     */
    public void deleteScene(String sceneName) {
        if (savedScenes.remove(sceneName) != null) {
            log.info("Scene deleted: {}", sceneName);
        }
    }

    /**
     * Get list of saved scenes
     */
    public Set<String> getSavedScenes() {
        return savedScenes.keySet();
    }

    /**
     * Undo last scene save (restore previous state from history)
     */
    public void undoLastSave() {
        if (history.size() < 2) {
            log.warn("Not enough history to undo");
            return;
        }

        // Remove current state
        history.remove(history.size() - 1);

        // Restore previous state
        DeviceStateMemento previous = history.get(history.size() - 1);
        log.info("Undoing to previous state: {}", previous.getSceneName());

        Map<String, DeviceSnapshot> states = previous.getDeviceStates();
        for (Map.Entry<String, DeviceSnapshot> entry : states.entrySet()) {
            SceneDevice device = devices.get(entry.getKey());
            if (device != null) {
                device.restoreState(entry.getValue());
            }
        }
    }

    private void addToHistory(DeviceStateMemento memento) {
        history.add(memento);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public int getHistorySize() {
        return history.size();
    }

    public List<String> getHistory() {
        List<String> descriptions = new ArrayList<>();
        for (DeviceStateMemento memento : history) {
            descriptions.add(memento.toString());
        }
        return descriptions;
    }
}
