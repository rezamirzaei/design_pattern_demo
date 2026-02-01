package com.smarthome.web.catalog;

import com.smarthome.web.viewmodel.PatternParameter;
import com.smarthome.web.viewmodel.PatternView;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PatternCatalog {

    public List<PatternView> all() {
        return List.of(
                new PatternView(
                        "singleton",
                        "Singleton",
                        "Creational",
                        "A single HomeController instance manages home mode and registered devices.",
                        "GET",
                        "/status",
                        List.of("com.smarthome.pattern.creational.singleton.HomeController"),
                        Collections.emptyList()
                ),
                new PatternView(
                        "factory",
                        "Factory Method",
                        "Creational",
                        "Concrete factories create SmartLight/SmartThermostat/SmartCamera/SmartLock without the caller depending on concrete classes.",
                        "POST",
                        "/patterns/factory/create",
                        List.of(
                                "com.smarthome.pattern.creational.factory.DeviceFactory",
                                "com.smarthome.pattern.creational.factory.LightFactory",
                                "com.smarthome.pattern.creational.factory.ThermostatFactory",
                                "com.smarthome.pattern.creational.factory.CameraFactory",
                                "com.smarthome.pattern.creational.factory.LockFactory"
                        ),
                        List.of(
                                new PatternParameter("type", "Device Type", "select", List.of("LIGHT", "THERMOSTAT", "CAMERA", "LOCK"), "LIGHT"),
                                new PatternParameter("name", "Device Name", "text", null, "New Device"),
                                new PatternParameter("location", "Location", "text", null, "Living Room")
                        )
                ),
                new PatternView(
                        "abstract-factory",
                        "Abstract Factory",
                        "Creational",
                        "Ecosystem factories (HomeKit / SmartThings) create families of compatible devices.",
                        "POST",
                        "/patterns/abstract-factory/create",
                        List.of(
                                "com.smarthome.pattern.creational.abstractfactory.SmartDeviceAbstractFactory",
                                "com.smarthome.pattern.creational.abstractfactory.HomeKitFactory",
                                "com.smarthome.pattern.creational.abstractfactory.SmartThingsFactory"
                        ),
                        List.of(
                                new PatternParameter("ecosystem", "Ecosystem", "select", List.of("SMARTTHINGS", "HOMEKIT"), "SMARTTHINGS"),
                                new PatternParameter("location", "Location", "text", null, "Bedroom")
                        )
                ),
                new PatternView(
                        "builder",
                        "Builder",
                        "Creational",
                        "Fluent builder constructs an AutomationRule object from multiple parts.",
                        "POST",
                        "/patterns/builder/rule",
                        List.of(
                                "com.smarthome.pattern.creational.builder.AutomationRule",
                                "com.smarthome.pattern.creational.builder.RuleContext"
                        ),
                        List.of(
                                new PatternParameter("name", "Rule Name", "text", null, "Evening Lights"),
                                new PatternParameter("trigger", "Trigger", "text", null, "motion detected"),
                                new PatternParameter("condition", "Condition", "text", null, "time > 18:00"),
                                new PatternParameter("action", "Action", "text", null, "turn lights on")
                        )
                ),
                new PatternView(
                        "prototype",
                        "Prototype",
                        "Creational",
                        "Clone configuration presets via DeviceConfiguration prototypes.",
                        "POST",
                        "/patterns/prototype/clone",
                        List.of(
                                "com.smarthome.pattern.creational.prototype.DeviceConfiguration",
                                "com.smarthome.pattern.creational.prototype.ConfigurationPrototypeRegistry"
                        ),
                        List.of(
                                new PatternParameter("template", "Template to Clone", "select", List.of("bright-light", "dim-light", "comfort-thermostat", "security-camera"), "bright-light"),
                                new PatternParameter("newName", "New Configuration Name", "text", null, "My Custom Config")
                        )
                ),
                new PatternView(
                        "adapter",
                        "Adapter",
                        "Structural",
                        "Wrap a LegacyThermostat and expose it through the smart Device interface.",
                        "POST",
                        "/patterns/adapter/legacy",
                        List.of(
                                "com.smarthome.pattern.structural.adapter.LegacyThermostat",
                                "com.smarthome.pattern.structural.adapter.LegacyThermostatAdapter"
                        ),
                        List.of(
                                new PatternParameter("name", "Device Name", "text", null, "Old Thermostat"),
                                new PatternParameter("location", "Location", "text", null, "Basement")
                        )
                ),
                new PatternView(
                        "bridge",
                        "Bridge",
                        "Structural",
                        "Remotes (abstraction) control devices (implementor) without tight coupling.",
                        "GET",
                        "/patterns/bridge/demo",
                        List.of(
                                "com.smarthome.pattern.structural.bridge.RemoteControl",
                                "com.smarthome.pattern.structural.bridge.BasicRemote",
                                "com.smarthome.pattern.structural.bridge.AdvancedRemote",
                                "com.smarthome.pattern.structural.bridge.TVDevice",
                                "com.smarthome.pattern.structural.bridge.RadioDevice"
                        ),
                        Collections.emptyList()
                ),
                new PatternView(
                        "composite",
                        "Composite",
                        "Structural",
                        "Treat rooms and individual devices uniformly (DeviceGroup / SingleDevice).",
                        "GET",
                        "/patterns/composite/rooms",
                        List.of(
                                "com.smarthome.pattern.structural.composite.DeviceComponent",
                                "com.smarthome.pattern.structural.composite.DeviceGroup",
                                "com.smarthome.pattern.structural.composite.SingleDevice"
                        ),
                        Collections.emptyList()
                ),
                new PatternView(
                        "decorator",
                        "Decorator",
                        "Structural",
                        "Add behavior (logging/security/caching) around a device without changing the device class.",
                        "POST",
                        "/patterns/decorator/wrap",
                        List.of(
                                "com.smarthome.pattern.structural.decorator.DeviceDecorator",
                                "com.smarthome.pattern.structural.decorator.LoggingDecorator",
                                "com.smarthome.pattern.structural.decorator.SecurityDecorator",
                                "com.smarthome.pattern.structural.decorator.CachingDecorator"
                        ),
                        List.of(
                                new PatternParameter("deviceId", "Target Device ID", "text", null, "living-light-1"),
                                new PatternParameter("decorators", "Decorators (comma-separated)", "text", null, "LOGGING,SECURITY")
                        )
                ),
                new PatternView(
                        "facade",
                        "Facade",
                        "Structural",
                        "SmartHomeFacade provides simple scene methods that coordinate subsystems.",
                        "POST",
                        "/patterns/facade/scene/{sceneName}",
                        List.of(
                                "com.smarthome.pattern.structural.facade.SmartHomeFacade"
                        ),
                        List.of(
                                new PatternParameter("sceneName", "Scene Name", "select", List.of("morning", "night", "leave", "arrive", "movie", "party", "panic"), "movie")
                        )
                ),
                new PatternView(
                        "flyweight",
                        "Flyweight",
                        "Structural",
                        "Share immutable DeviceType flyweights across many DeviceInstance objects.",
                        "GET",
                        "/patterns/flyweight/demo",
                        List.of(
                                "com.smarthome.pattern.structural.flyweight.DeviceType",
                                "com.smarthome.pattern.structural.flyweight.DeviceTypeFactory"
                        ),
                        Collections.emptyList()
                ),
                new PatternView(
                        "proxy",
                        "Proxy",
                        "Structural",
                        "DeviceProxy controls access to a remote device with lazy init + caching + access control.",
                        "POST",
                        "/patterns/proxy/remote",
                        List.of(
                                "com.smarthome.pattern.structural.proxy.DeviceProxy",
                                "com.smarthome.pattern.structural.proxy.RemoteDevice"
                        ),
                        List.of(
                                new PatternParameter("name", "Device Name", "text", null, "Remote Camera"),
                                new PatternParameter("address", "IP Address", "text", null, "192.168.1.50")
                        )
                ),
                new PatternView(
                        "chain",
                        "Chain of Responsibility",
                        "Behavioral",
                        "Alert handlers process alerts based on severity.",
                        "POST",
                        "/patterns/chain/alert",
                        List.of(
                                "com.smarthome.pattern.behavioral.chain.AlertHandler",
                                "com.smarthome.pattern.behavioral.chain.LoggingAlertHandler",
                                "com.smarthome.pattern.behavioral.chain.NotificationAlertHandler",
                                "com.smarthome.pattern.behavioral.chain.AlarmAlertHandler",
                                "com.smarthome.pattern.behavioral.chain.EmergencyAlertHandler"
                        ),
                        List.of(
                                new PatternParameter("deviceId", "Source Device ID", "text", null, "sensor-1"),
                                new PatternParameter("level", "Alert Level", "select", List.of("INFO", "WARNING", "CRITICAL", "EMERGENCY"), "WARNING"),
                                new PatternParameter("message", "Alert Message", "text", null, "Motion detected in restricted area")
                        )
                ),
                new PatternView(
                        "command",
                        "Command",
                        "Behavioral",
                        "Encapsulate device operations with undo/redo support.",
                        "POST",
                        "/patterns/command/execute",
                        List.of(
                                "com.smarthome.pattern.behavioral.command.Command",
                                "com.smarthome.pattern.behavioral.command.TurnOnCommand",
                                "com.smarthome.pattern.behavioral.command.CommandInvoker"
                        ),
                        List.of(
                                new PatternParameter("deviceId", "Target Device ID", "text", null, "living-light-1"),
                                new PatternParameter("command", "Command", "select", List.of("ON", "OFF"), "ON")
                        )
                ),
                new PatternView(
                        "interpreter",
                        "Interpreter",
                        "Behavioral",
                        "Parse and evaluate simple automation expressions against a context.",
                        "POST",
                        "/patterns/interpreter/evaluate",
                        List.of(
                                "com.smarthome.pattern.behavioral.interpreter.InterpreterContext",
                                "com.smarthome.pattern.behavioral.interpreter.RuleInterpreter"
                        ),
                        List.of(
                                new PatternParameter("rule", "Rule Expression", "text", null, "motion AND hour >= 18"),
                                new PatternParameter("motion", "Context: motion (boolean)", "select", List.of("true", "false"), "true"),
                                new PatternParameter("hour", "Context: hour (int)", "text", null, "20")
                        )
                ),
                new PatternView(
                        "iterator",
                        "Iterator",
                        "Behavioral",
                        "Traverse devices without exposing collection internals (all / by room / type / status).",
                        "GET",
                        "/patterns/iterator/demo",
                        List.of(
                                "com.smarthome.pattern.behavioral.iterator.DeviceIterator",
                                "com.smarthome.pattern.behavioral.iterator.SmartHomeDeviceCollection"
                        ),
                        List.of(
                                new PatternParameter("filterType", "Filter Type", "select", List.of("ALL", "ROOM", "TYPE", "STATUS"), "ROOM"),
                                new PatternParameter("filterValue", "Filter Value", "text", null, "Living Room")
                        )
                ),
                new PatternView(
                        "mediator",
                        "Mediator",
                        "Behavioral",
                        "Central hub coordinates device interactions without devices referencing each other directly.",
                        "GET",
                        "/patterns/mediator/demo",
                        List.of(
                                "com.smarthome.pattern.behavioral.mediator.SmartHomeMediator",
                                "com.smarthome.pattern.behavioral.mediator.CentralHubMediator"
                        ),
                        Collections.emptyList()
                ),
                new PatternView(
                        "memento",
                        "Memento",
                        "Behavioral",
                        "Save and restore scene snapshots without violating encapsulation.",
                        "POST",
                        "/patterns/memento/save",
                        List.of(
                                "com.smarthome.pattern.behavioral.memento.DeviceStateMemento",
                                "com.smarthome.pattern.behavioral.memento.SceneManager"
                        ),
                        List.of(
                                new PatternParameter("sceneName", "Snapshot Name", "text", null, "My Snapshot")
                        )
                ),
                new PatternView(
                        "observer",
                        "Observer",
                        "Behavioral",
                        "ObservableDevice notifies observers of device events (mobile/dashboard/analytics/email).",
                        "POST",
                        "/patterns/observer/register",
                        List.of(
                                "com.smarthome.pattern.behavioral.observer.DeviceObserver",
                                "com.smarthome.pattern.behavioral.observer.ObservableDevice"
                        ),
                        List.of(
                                new PatternParameter("deviceId", "Device ID to Observe", "text", null, "living-thermostat"),
                                new PatternParameter("observerType", "Observer Type", "text", null, "MobileApp")
                        )
                ),
                new PatternView(
                        "state",
                        "State",
                        "Behavioral",
                        "StatefulDevice transitions between OFF/ON/STANDBY/ERROR with different behavior per state.",
                        "GET",
                        "/patterns/state/demo",
                        List.of(
                                "com.smarthome.pattern.behavioral.state.DeviceState",
                                "com.smarthome.pattern.behavioral.state.StatefulDevice"
                        ),
                        Collections.emptyList()
                ),
                new PatternView(
                        "strategy",
                        "Strategy",
                        "Behavioral",
                        "EnergyManager applies different strategies (Eco/Comfort/Away/Party/Night).",
                        "POST",
                        "/patterns/strategy/apply",
                        List.of(
                                "com.smarthome.pattern.behavioral.strategy.EnergyStrategy",
                                "com.smarthome.pattern.behavioral.strategy.EnergyManager"
                        ),
                        List.of(
                                new PatternParameter("strategy", "Strategy", "select", List.of("ECO", "COMFORT", "AWAY", "PARTY", "NIGHT"), "ECO")
                        )
                ),
                new PatternView(
                        "template",
                        "Template Method",
                        "Behavioral",
                        "DeviceInitializer defines the init algorithm; subclasses customize steps.",
                        "GET",
                        "/patterns/template/demo",
                        List.of(
                                "com.smarthome.pattern.behavioral.templatemethod.DeviceInitializer",
                                "com.smarthome.pattern.behavioral.templatemethod.LightInitializer"
                        ),
                        List.of(
                                new PatternParameter("deviceType", "Device Type", "select", List.of("LIGHT", "THERMOSTAT", "CAMERA", "LOCK"), "LIGHT")
                        )
                ),
                new PatternView(
                        "visitor",
                        "Visitor",
                        "Behavioral",
                        "Run audits (maintenance/energy/security) without changing device element classes.",
                        "GET",
                        "/patterns/visitor/audit",
                        List.of(
                                "com.smarthome.pattern.behavioral.visitor.DeviceVisitor",
                                "com.smarthome.pattern.behavioral.visitor.MaintenanceVisitor"
                        ),
                        List.of(
                                new PatternParameter("type", "Audit Type", "select", List.of("MAINTENANCE", "ENERGY", "SECURITY"), "MAINTENANCE")
                        )
                )
        );
    }
}

