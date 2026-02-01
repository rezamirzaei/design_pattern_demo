package com.smarthome.pattern.behavioral.command;

import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * COMMAND PATTERN
 *
 * Intent: Encapsulate a request as an object, thereby letting you parameterize
 * clients with different requests, queue or log requests, and support undoable operations.
 */

/**
 * Command interface
 */
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

/**
 * Concrete Command - Turn On
 */
class TurnOnCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(TurnOnCommand.class);
    private final Device device;
    private boolean wasOn;

    public TurnOnCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        wasOn = device.isOn();
        device.turnOn();
        log.info("Executed: Turn ON {}", device.getDeviceInfo());
    }

    @Override
    public void undo() {
        if (!wasOn) {
            device.turnOff();
            log.info("Undone: Turn ON {} (restored to OFF)", device.getDeviceInfo());
        }
    }

    @Override
    public String getDescription() {
        return "Turn ON " + device.getDeviceInfo();
    }
}

/**
 * Concrete Command - Turn Off
 */
class TurnOffCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(TurnOffCommand.class);
    private final Device device;
    private boolean wasOn;

    public TurnOffCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        wasOn = device.isOn();
        device.turnOff();
        log.info("Executed: Turn OFF {}", device.getDeviceInfo());
    }

    @Override
    public void undo() {
        if (wasOn) {
            device.turnOn();
            log.info("Undone: Turn OFF {} (restored to ON)", device.getDeviceInfo());
        }
    }

    @Override
    public String getDescription() {
        return "Turn OFF " + device.getDeviceInfo();
    }
}

/**
 * Concrete Command - Set Brightness
 */
class SetBrightnessCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(SetBrightnessCommand.class);
    private final SmartLight light;
    private final int newBrightness;
    private int previousBrightness;

    public SetBrightnessCommand(SmartLight light, int brightness) {
        this.light = light;
        this.newBrightness = brightness;
    }

    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.setBrightness(newBrightness);
        log.info("Executed: Set brightness to {}%", newBrightness);
    }

    @Override
    public void undo() {
        light.setBrightness(previousBrightness);
        log.info("Undone: Brightness restored to {}%", previousBrightness);
    }

    @Override
    public String getDescription() {
        return "Set brightness to " + newBrightness + "%";
    }
}

/**
 * Concrete Command - Set Temperature
 */
class SetTemperatureCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(SetTemperatureCommand.class);
    private final SmartThermostat thermostat;
    private final double newTemperature;
    private double previousTemperature;

    public SetTemperatureCommand(SmartThermostat thermostat, double temperature) {
        this.thermostat = thermostat;
        this.newTemperature = temperature;
    }

    @Override
    public void execute() {
        previousTemperature = thermostat.getTargetTemperature();
        thermostat.setTargetTemperature(newTemperature);
        log.info("Executed: Set temperature to {}°C", newTemperature);
    }

    @Override
    public void undo() {
        thermostat.setTargetTemperature(previousTemperature);
        log.info("Undone: Temperature restored to {}°C", previousTemperature);
    }

    @Override
    public String getDescription() {
        return "Set temperature to " + newTemperature + "°C";
    }
}

/**
 * Macro Command - Executes multiple commands
 */
class MacroCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(MacroCommand.class);
    private final String name;
    private final List<Command> commands;

    public MacroCommand(String name) {
        this.name = name;
        this.commands = new ArrayList<>();
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    @Override
    public void execute() {
        log.info("Executing macro: {}", name);
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        log.info("Undoing macro: {}", name);
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String getDescription() {
        return "Macro: " + name + " (" + commands.size() + " commands)";
    }
}

/**
 * Invoker - Command executor with history for undo/redo
 */
class CommandInvoker {
    private static final Logger log = LoggerFactory.getLogger(CommandInvoker.class);
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private final List<Command> commandQueue = new ArrayList<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear();
        log.debug("Command added to history. History size: {}", history.size());
    }

    public void queueCommand(Command command) {
        commandQueue.add(command);
        log.info("Command queued: {}", command.getDescription());
    }

    public void executeQueue() {
        log.info("Executing {} queued commands", commandQueue.size());
        for (Command command : commandQueue) {
            executeCommand(command);
        }
        commandQueue.clear();
    }

    public void undo() {
        if (history.isEmpty()) {
            log.warn("Nothing to undo");
            return;
        }
        Command command = history.pop();
        command.undo();
        redoStack.push(command);
        log.info("Undone: {}", command.getDescription());
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            log.warn("Nothing to redo");
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        history.push(command);
        log.info("Redone: {}", command.getDescription());
    }

    public int getHistorySize() {
        return history.size();
    }

    public int getQueueSize() {
        return commandQueue.size();
    }

    public List<String> getHistory() {
        List<String> descriptions = new ArrayList<>();
        for (Command command : history) {
            descriptions.add(command.getDescription());
        }
        return descriptions;
    }
}
