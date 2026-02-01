package com.smarthome.pattern.behavioral.state;

import java.util.ArrayList;
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

        return Map.of(
                "pattern", "State",
                "device", device.getName(),
                "states", states,
                "lastError", device.getLastError()
        );
    }
}

