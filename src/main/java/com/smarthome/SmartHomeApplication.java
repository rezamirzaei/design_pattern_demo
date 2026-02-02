package com.smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Home Automation System
 * 
 * This application demonstrates all 23 Gang of Four (GoF) Design Patterns
 * in a real-world Smart Home context.
 * 
 * CREATIONAL PATTERNS:
 * 1. Singleton - HomeController (central coordinator)
 * 2. Factory Method - DeviceFactory (device creation)
 * 3. Abstract Factory - SmartDeviceAbstractFactory (device ecosystems)
 * 4. Builder - AutomationRuleBuilder (complex rule construction)
 * 5. Prototype - DeviceConfiguration (cloning device configs)
 * 
 * STRUCTURAL PATTERNS:
 * 6. Adapter - LegacyDeviceAdapter (legacy device integration)
 * 7. Bridge - RemoteControl (separates abstraction from implementation)
 * 8. Composite - DeviceGroup (device/room hierarchies)
 * 9. Decorator - DeviceDecorator (adds features to devices)
 * 10. Facade - SmartHomeFacade (simplified interface)
 * 11. Flyweight - DeviceType (shared device metadata)
 * 12. Proxy - DeviceProxy (remote access control)
 * 
 * BEHAVIORAL PATTERNS:
 * 13. Chain of Responsibility - AlertHandler (alert processing)
 * 14. Command - DeviceCommand (encapsulated operations)
 * 15. Interpreter - RuleInterpreter (automation rule language)
 * 16. Iterator - DeviceIterator (device traversal)
 * 17. Mediator - SmartHomeMediator (device communication)
 * 18. Memento - SceneMemento (save/restore states)
 * 19. Observer - DeviceObserver (event notifications)
 * 20. State - DeviceState (device state machine)
 * 21. Strategy - EnergyStrategy (energy optimization)
 * 22. Template Method - DeviceInitializer (initialization template)
 * 23. Visitor - DeviceVisitor (operations on devices)
 */
@SpringBootApplication
public class SmartHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHomeApplication.class, args);
    }
}
