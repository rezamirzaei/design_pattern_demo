package com.smarthome.pattern.behavioral.command;

import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import java.util.Locale;
import java.util.Map;

public final class CommandDemo {
    private CommandDemo() {}

    public static Map<String, Object> execute(Device device, String command) {
        CommandInvoker invoker = new CommandInvoker();

        Command cmd = toCommand(device, command);
        String before = device.getStatus();

        invoker.executeCommand(cmd);
        String afterExecute = device.getStatus();

        invoker.undo();
        String afterUndo = device.getStatus();

        invoker.redo();
        String afterRedo = device.getStatus();

        return Map.of(
                "pattern", "Command",
                "device", device.getDeviceInfo(),
                "command", cmd.getDescription(),
                "before", before,
                "afterExecute", afterExecute,
                "afterUndo", afterUndo,
                "afterRedo", afterRedo,
                "history", invoker.getHistory()
        );
    }

    private static Command toCommand(Device device, String command) {
        String normalized = command == null ? "ON" : command.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("BRIGHTNESS:") && device instanceof SmartLight light) {
            int brightness = Integer.parseInt(normalized.substring("BRIGHTNESS:".length()));
            return new SetBrightnessCommand(light, brightness);
        }
        if (normalized.startsWith("TEMPERATURE:") && device instanceof SmartThermostat thermostat) {
            double temp = Double.parseDouble(normalized.substring("TEMPERATURE:".length()));
            return new SetTemperatureCommand(thermostat, temp);
        }
        return switch (normalized) {
            case "OFF" -> new TurnOffCommand(device);
            case "ON" -> new TurnOnCommand(device);
            default -> new TurnOnCommand(device);
        };
    }
}

