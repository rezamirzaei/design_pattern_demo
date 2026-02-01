package com.smarthome.pattern.behavioral.memento;

import java.util.List;
import java.util.Map;

public final class MementoDemo {
    private MementoDemo() {}

    public static Map<String, Object> saveAndRestore(String sceneName) {
        SceneManager sceneManager = new SceneManager();
        SceneDevice light = new SceneDevice("m-light-1", "Scene Light");
        SceneDevice thermostat = new SceneDevice("m-thermo-1", "Scene Thermostat");

        sceneManager.registerDevice(light);
        sceneManager.registerDevice(thermostat);

        light.setOn(true);
        light.setProperty("brightness", 80);
        thermostat.setOn(true);
        thermostat.setProperty("targetTempC", 22);

        DeviceStateMemento saved = sceneManager.saveScene(sceneName == null || sceneName.isBlank() ? "Demo Scene" : sceneName);

        light.setOn(false);
        thermostat.setProperty("targetTempC", 18);

        sceneManager.restoreScene(saved.getSceneName());

        return Map.of(
                "pattern", "Memento",
                "saved", saved.toString(),
                "deviceStatuses", List.of(light.getStatus(), thermostat.getStatus()),
                "history", sceneManager.getHistory()
        );
    }
}

