package com.smarthome.pattern.behavioral.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StateDemo {
    private StateDemo() {}

    public static Map<String, Object> demo() {
        StatefulDevice device = new StatefulDevice("Stateful Light");
        List<String> states = new ArrayList<>();

        states.add(device.getStateName());
        device.turnOn();
        states.add(device.getStateName());
        device.standby();
        states.add(device.getStateName());
        device.operate("TOGGLE");
        states.add(device.getStateName());
        device.reportError("Simulated failure");
        states.add(device.getStateName());
        device.turnOff(); // resets error to OFF
        states.add(device.getStateName());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pattern", "State");
        result.put("device", device.getName());
        result.put("states", states);
        result.put("lastError", device.getLastError()); // may be null after reset
        return result;
    }
}

