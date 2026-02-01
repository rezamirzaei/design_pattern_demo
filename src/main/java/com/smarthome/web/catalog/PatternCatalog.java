package com.smarthome.web.catalog;

import com.smarthome.web.viewmodel.PatternView;
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
                        List.of(
                                "com.smarthome.pattern.creational.singleton.HomeController"
                        )
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
                                "com.smarthome.pattern.creational.factory.LockFactory",
                                "com.smarthome.pattern.creational.factory.Device",
                                "com.smarthome.pattern.creational.factory.SmartLight",
                                "com.smarthome.pattern.creational.factory.SmartThermostat",
                                "com.smarthome.pattern.creational.factory.SmartCamera",
                                "com.smarthome.pattern.creational.factory.SmartLock"
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
                                "com.smarthome.pattern.creational.abstractfactory.SmartThingsFactory",
                                "com.smarthome.pattern.creational.abstractfactory.HomeKitSensor",
                                "com.smarthome.pattern.creational.abstractfactory.SmartThingsSensor"
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
                        )
                ),
                new PatternView(
                        "prototype",
                        "Prototype",
                        "Creational",
                        "Clone configuration presets via DeviceConfiguration prototypes.",
                        "GET",
                        "/patterns/prototype/templates",
                        List.of(
                                "com.smarthome.pattern.creational.prototype.DeviceConfiguration",
                                "com.smarthome.pattern.creational.prototype.ConfigurationPrototypeRegistry"
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
                                "com.smarthome.pattern.structural.bridge.DeviceImplementor",
                                "com.smarthome.pattern.structural.bridge.TVDevice",
                                "com.smarthome.pattern.structural.bridge.RadioDevice"
                        )
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
                        )
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
                        )
                ),
                new PatternView(
                        "facade",
                        "Facade",
                        "Structural",
                        "SmartHomeFacade provides simple scene methods that coordinate subsystems.",
                        "POST",
                        "/patterns/facade/scene/{name}",
                        List.of(
                                "com.smarthome.pattern.structural.facade.SmartHomeFacade"
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
                                "com.smarthome.pattern.structural.flyweight.DeviceTypeFactory",
                                "com.smarthome.pattern.structural.flyweight.DeviceInstance"
                        )
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
                                "com.smarthome.pattern.structural.proxy.RemoteDevice",
                                "com.smarthome.pattern.structural.proxy.RealRemoteDevice"
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
                                "com.smarthome.pattern.behavioral.chain.EmergencyAlertHandler",
                                "com.smarthome.pattern.behavioral.chain.AlertHandlerChain"
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
                                "com.smarthome.pattern.behavioral.command.TurnOffCommand",
                                "com.smarthome.pattern.behavioral.command.SetBrightnessCommand",
                                "com.smarthome.pattern.behavioral.command.SetTemperatureCommand",
                                "com.smarthome.pattern.behavioral.command.MacroCommand",
                                "com.smarthome.pattern.behavioral.command.CommandInvoker"
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
                                "com.smarthome.pattern.behavioral.interpreter.RuleInterpreter",
                                "com.smarthome.pattern.behavioral.interpreter.AutomationRuleExpression"
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
                                "com.smarthome.pattern.behavioral.iterator.SmartHomeDeviceCollection",
                                "com.smarthome.pattern.behavioral.iterator.AllDevicesIterator",
                                "com.smarthome.pattern.behavioral.iterator.RoomFilterIterator",
                                "com.smarthome.pattern.behavioral.iterator.TypeFilterIterator",
                                "com.smarthome.pattern.behavioral.iterator.StatusFilterIterator"
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
                                "com.smarthome.pattern.behavioral.mediator.CentralHubMediator",
                                "com.smarthome.pattern.behavioral.mediator.SmartLightColleague",
                                "com.smarthome.pattern.behavioral.mediator.CameraColleague",
                                "com.smarthome.pattern.behavioral.mediator.MotionSensorColleague"
                        )
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
                                "com.smarthome.pattern.behavioral.memento.SceneManager",
                                "com.smarthome.pattern.behavioral.memento.SceneDevice",
                                "com.smarthome.pattern.behavioral.memento.DeviceSnapshot"
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
                                "com.smarthome.pattern.behavioral.observer.ObservableDevice",
                                "com.smarthome.pattern.behavioral.observer.MobileAppObserver",
                                "com.smarthome.pattern.behavioral.observer.DashboardObserver",
                                "com.smarthome.pattern.behavioral.observer.AnalyticsObserver",
                                "com.smarthome.pattern.behavioral.observer.EmailObserver"
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
                                "com.smarthome.pattern.behavioral.state.StatefulDevice",
                                "com.smarthome.pattern.behavioral.state.OffState",
                                "com.smarthome.pattern.behavioral.state.OnState",
                                "com.smarthome.pattern.behavioral.state.StandbyState",
                                "com.smarthome.pattern.behavioral.state.ErrorState"
                        )
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
                                "com.smarthome.pattern.behavioral.strategy.EnergyManager",
                                "com.smarthome.pattern.behavioral.strategy.EcoModeStrategy",
                                "com.smarthome.pattern.behavioral.strategy.ComfortModeStrategy",
                                "com.smarthome.pattern.behavioral.strategy.AwayModeStrategy",
                                "com.smarthome.pattern.behavioral.strategy.PartyModeStrategy",
                                "com.smarthome.pattern.behavioral.strategy.NightModeStrategy"
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
                                "com.smarthome.pattern.behavioral.templatemethod.LightInitializer",
                                "com.smarthome.pattern.behavioral.templatemethod.ThermostatInitializer",
                                "com.smarthome.pattern.behavioral.templatemethod.CameraInitializer"
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
                                "com.smarthome.pattern.behavioral.visitor.VisitableLight",
                                "com.smarthome.pattern.behavioral.visitor.VisitableThermostat",
                                "com.smarthome.pattern.behavioral.visitor.VisitableCamera",
                                "com.smarthome.pattern.behavioral.visitor.VisitableLock",
                                "com.smarthome.pattern.behavioral.visitor.MaintenanceVisitor",
                                "com.smarthome.pattern.behavioral.visitor.EnergyAuditVisitor",
                                "com.smarthome.pattern.behavioral.visitor.SecurityAuditVisitor"
                        )
                )
        );
    }
}

