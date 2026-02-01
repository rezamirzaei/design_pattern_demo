# Smart Home Design Patterns Demo

A comprehensive Smart Home Automation System that demonstrates **all 23 Gang of Four (GoF) Design Patterns** in a real-world application.

## 🏠 Project Overview

This project implements a Smart Home control system with:
- **REST API** for device control
- **Spring MVC Web UI** (Thymeleaf) for devices/rooms/scenes/rules + patterns lab
- **PostgreSQL** database (Docker/production profile) / **H2** (development)
- **Docker** containerization

## 📐 Design Patterns Implemented

### Creational Patterns (5)
| Pattern | Implementation | Description |
|---------|---------------|-------------|
| **Singleton** | `HomeController` | Central home controller - single instance managing all devices |
| **Factory Method** | `DeviceFactory` | Creates different device types (Light, Thermostat, Camera, Lock) |
| **Abstract Factory** | `SmartDeviceAbstractFactory` | Creates device families for different ecosystems (SmartThings, HomeKit) |
| **Builder** | `AutomationRule.Builder` | Builds complex automation rules with fluent API |
| **Prototype** | `DeviceConfiguration` | Clones device configuration templates |

### Structural Patterns (7)
| Pattern | Implementation | Description |
|---------|---------------|-------------|
| **Adapter** | `LegacyThermostatAdapter` | Adapts legacy devices to modern interface |
| **Bridge** | `RemoteControl` | Separates remote control abstraction from device implementation |
| **Composite** | `DeviceGroup` | Groups devices into rooms/zones for uniform control |
| **Decorator** | `DeviceDecorator` | Adds logging, security, caching to devices |
| **Facade** | `SmartHomeFacade` | Simplified interface for complex scene operations |
| **Flyweight** | `DeviceType` | Shares common device metadata across instances |
| **Proxy** | `DeviceProxy` | Controls access to remote devices with lazy init and caching |

### Behavioral Patterns (11)
| Pattern | Implementation | Description |
|---------|---------------|-------------|
| **Chain of Responsibility** | `AlertHandler` | Processes alerts through handler chain |
| **Command** | `Command` | Encapsulates device operations with undo/redo |
| **Interpreter** | `RuleInterpreter` | Parses automation rule language |
| **Iterator** | `DeviceIterator` | Traverses devices by room, type, or status |
| **Mediator** | `SmartHomeMediator` | Coordinates device communication |
| **Memento** | `DeviceStateMemento` | Saves/restores device states as scenes |
| **Observer** | `DeviceObserver` | Notifies subscribers of device events |
| **State** | `DeviceState` | Manages device state machine (ON, OFF, STANDBY, ERROR) |
| **Strategy** | `EnergyStrategy` | Pluggable energy optimization algorithms |
| **Template Method** | `DeviceInitializer` | Common device initialization with customizable steps |
| **Visitor** | `DeviceVisitor` | Performs audits (maintenance, energy, security) on devices |

## 🧩 How Each Pattern Is Used (and Why It Helps)

This project exposes pattern demos through:
- **MVC UI pages** (Thymeleaf): `/ui/*`
- **REST APIs** (Spring MVC): `/api/*`

Below is a short “how/why” for every GoF pattern used in this codebase.

### Creational Patterns (5)

**1) Singleton**
- **How:** `HomeController.INSTANCE` is the single runtime registry for devices + home mode, used by `SmartHomeService`.
- **Why it helps:** One consistent source of truth for shared state across scenes, device control, and pattern demos.
- **Try it:** Dashboard home-mode buttons (`/`) or `POST /api/mode/{mode}` + `GET /api/status`.

**2) Factory Method**
- **How:** `DeviceFactory` + concrete factories create concrete `Device` implementations without callers depending on concrete types.
- **Why it helps:** Adding a new device type becomes “add a factory + implementation” instead of changing callers everywhere.
- **Try it:** `/ui/devices` (Create Device) or `POST /api/patterns/factory/create`.

**3) Abstract Factory**
- **How:** `SmartDeviceAbstractFactory` creates a compatible family of devices per ecosystem (`HomeKitFactory`, `SmartThingsFactory`).
- **Why it helps:** Centralizes “ecosystem compatibility” decisions and keeps device-family creation consistent.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/abstract-factory/create`.

**4) Builder**
- **How:** `AutomationRule.Builder` assembles a rule from parts (name/trigger/condition/action) in a fluent way.
- **Why it helps:** Makes complex object creation readable and safe (especially when optional fields grow).
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/builder/rule` (and `/ui/rules` for persisted rules).

**5) Prototype**
- **How:** `DeviceConfiguration` templates are cloned via `ConfigurationPrototypeRegistry` to produce new preset instances.
- **Why it helps:** Fast, consistent creation of preset configurations without re-building maps/settings each time.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/prototype/templates`.

### Structural Patterns (7)

**6) Adapter**
- **How:** `LegacyThermostatAdapter` wraps `LegacyThermostat` and exposes it like a modern smart-home device.
- **Why it helps:** Integrates “old” APIs without changing legacy code or polluting the rest of the system with legacy details.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/adapter/legacy`.

**7) Bridge**
- **How:** `RemoteControl` (and `BasicRemote` / `AdvancedRemote`) works with any `DeviceImplementor` (`TVDevice`, `RadioDevice`).
- **Why it helps:** You can extend remotes and devices independently (avoid a combinatorial explosion of subclasses).
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/bridge/demo`.

**8) Composite**
- **How:** `DeviceGroup` + `SingleDevice` implement a uniform component so rooms/groups and individual devices can be treated the same.
- **Why it helps:** Lets you execute “turn all devices in a room on/off” using the same interface as “control one device”.
- **Try it:** Dashboard room controls (`/`) or `GET /api/patterns/composite/rooms` + `POST /api/patterns/composite/rooms/{room}/control`.

**9) Decorator**
- **How:** `LoggingDecorator`, `SecurityDecorator`, and `CachingDecorator` wrap a device at runtime without changing the base device.
- **Why it helps:** Adds cross-cutting concerns dynamically (observability/security/perf) without subclassing each device type.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/decorator/wrap`.

**10) Facade**
- **How:** `SmartHomeFacade` exposes simple scene methods (`goodNight`, `leaveHome`, etc.) over multiple subsystems/devices.
- **Why it helps:** Turns a multi-step orchestration into a single call, keeping controllers/UI clean.
- **Try it:** Dashboard Quick Scenes (`/`) or `POST /api/patterns/facade/scene/{name}`.

**11) Flyweight**
- **How:** `com.smarthome.pattern.structural.flyweight.DeviceType` objects are shared (via a factory) across many device instances.
- **Why it helps:** Saves memory and improves consistency when many instances share the same immutable metadata.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/flyweight/demo`.

**12) Proxy**
- **How:** `DeviceProxy` controls access to a `RemoteDevice`, lazily initializing and optionally caching status calls.
- **Why it helps:** Adds access control/lazy init/caching for expensive remote interactions without changing client code.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/proxy/remote`.

### Behavioral Patterns (11)

**13) Chain of Responsibility**
- **How:** An alert flows through a chain of handlers (logging → notification → alarm → emergency).
- **Why it helps:** Adds/reorders/removes alert processing steps without changing the sender.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/chain/alert`.

**14) Command**
- **How:** Device actions are encapsulated into command objects (`TurnOnCommand`, `SetBrightnessCommand`, etc.) executed by an invoker.
- **Why it helps:** Enables undo/redo and macro commands; decouples “request” from “execution”.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/command/execute`.

**15) Interpreter**
- **How:** A small DSL (e.g. `motion AND hour >= 18`) is parsed/evaluated against an `InterpreterContext`.
- **Why it helps:** Makes automation logic configurable and testable as data, not hardcoded `if` blocks.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/interpreter/evaluate` (and `/ui/rules` for persisted rules).

**16) Iterator**
- **How:** Iterators traverse device collections using filters (by room/type/status) without exposing internals.
- **Why it helps:** Adds new traversal strategies without changing the collection structure.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/iterator/demo`.

**17) Mediator**
- **How:** `CentralHubMediator` coordinates device interactions without devices directly referencing each other.
- **Why it helps:** Reduces coupling between devices and keeps interaction logic in one place.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/mediator/demo`.

**18) Memento**
- **How:** Pattern classes create and restore snapshots of device state without exposing internals.
- **Why it helps:** Enables “save/restore scene” behavior while preserving encapsulation.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/memento/save` (and `/ui/scenes` for persisted snapshots).

**19) Observer**
- **How:** `ObservableDevice` notifies multiple observers (mobile/dashboard/analytics/email) of device events.
- **Why it helps:** Lets you add new event consumers without changing the device/event source.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/observer/register`.

**20) State**
- **How:** A `StatefulDevice` changes behavior by swapping state objects (OFF/ON/STANDBY/ERROR).
- **Why it helps:** Avoids giant `switch`/`if` trees and makes state-specific behavior explicit and extensible.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/state/demo`.

**21) Strategy**
- **How:** `EnergyManager` applies different `EnergyStrategy` implementations (ECO/COMFORT/AWAY/PARTY/NIGHT).
- **Why it helps:** Swaps optimization algorithms at runtime without changing the caller.
- **Try it:** Patterns Lab (`/ui/patterns`) or `POST /api/patterns/strategy/apply`.

**22) Template Method**
- **How:** `DeviceInitializer` defines the algorithm for initialization while subclasses customize steps.
- **Why it helps:** Shares a single “golden path” for init while still allowing specialization per device type.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/template/demo`.

**23) Visitor**
- **How:** Visitors run audits (maintenance/energy/security) over visitable device elements without modifying the elements.
- **Why it helps:** Adds new operations across many device types without changing device classes.
- **Try it:** Patterns Lab (`/ui/patterns`) or `GET /api/patterns/visitor/audit`.

## 🚀 Quick Start

### Requirements

- **Java 17+**
- (Optional) **Docker** + **Docker Compose** for PostgreSQL

### Using Docker (Recommended)

```bash
# Clone the repository
cd untitled3

# Start with Docker Compose
docker-compose up -d

# Access the application
open http://localhost:8080
```

### Using Maven (Development)

```bash
# Run with H2 in-memory database
./mvnw spring-boot:run

# Access the application
open http://localhost:8080
```

### Build a runnable JAR

```bash
./mvnw -DskipTests package
java -jar target/smart-home-design-patterns-*.jar
```

## 🖥️ Web UI (MVC)

- Dashboard: `GET /`
- Devices: `GET /ui/devices`
- Rooms: `GET /ui/rooms`
- Scenes: `GET /ui/scenes`
- Rules: `GET /ui/rules`
- Patterns Lab: `GET /ui/patterns`

### Dev tools

- H2 console (dev profile only): `GET /h2-console`

## 🌐 API Endpoints

### System Status
- `GET /api/status` - Get system status
- `POST /api/mode/{mode}` - Set home mode (NORMAL, AWAY, NIGHT, VACATION)

### Device Control
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device details
- `POST /api/devices/{id}/control?action=on|off` - Control device

### Rooms / Scenes / Rules (Domain Entities)
- `GET /api/rooms` - List rooms
- `POST /api/rooms/create` - Create room
- `POST /api/rooms/{roomId}/assign?deviceId=...` - Assign device to room
- `POST /api/rooms/{roomId}/unassign?deviceId=...` - Unassign device from room

- `GET /api/scenes` - List scenes
- `POST /api/scenes/create` - Save a snapshot of current device states
- `POST /api/scenes/{sceneId}/apply` - Apply a saved snapshot
- `POST /api/scenes/{sceneId}/favorite` - Toggle favorite
- `POST /api/scenes/{sceneId}/delete` - Delete scene

- `GET /api/rules` - List automation rules
- `POST /api/rules/create` - Create rule (Interpreter + Builder concepts)
- `POST /api/rules/{ruleId}/run` - Evaluate (and optionally execute) a rule
- `POST /api/rules/{ruleId}/toggle` - Enable/disable rule
- `POST /api/rules/{ruleId}/delete` - Delete rule

### Pattern Demonstrations
- `GET /api/patterns` - List all patterns with endpoints
- `POST /api/patterns/factory/create` - Factory Method demo
- `POST /api/patterns/abstract-factory/create` - Abstract Factory demo
- `POST /api/patterns/builder/rule` - Builder demo
- `GET /api/patterns/prototype/templates` - Prototype demo
- `POST /api/patterns/adapter/legacy` - Adapter demo
- `GET /api/patterns/bridge/demo` - Bridge demo
- `GET /api/patterns/composite/rooms` - Composite demo
- `POST /api/patterns/decorator/wrap` - Decorator demo
- `POST /api/patterns/facade/scene/{name}` - Facade demo
- `GET /api/patterns/flyweight/demo` - Flyweight demo
- `POST /api/patterns/proxy/remote` - Proxy demo
- `POST /api/patterns/chain/alert` - Chain of Responsibility demo
- `POST /api/patterns/command/execute` - Command demo
- `POST /api/patterns/interpreter/evaluate` - Interpreter demo
- `GET /api/patterns/iterator/demo` - Iterator demo
- `GET /api/patterns/mediator/demo` - Mediator demo
- `POST /api/patterns/observer/register` - Observer demo
- `POST /api/patterns/strategy/apply` - Strategy demo
- `GET /api/patterns/state/demo` - State demo
- `POST /api/patterns/memento/save` - Memento demo
- `GET /api/patterns/template/demo` - Template Method demo
- `GET /api/patterns/visitor/audit` - Visitor demo

## ✅ Quick Verify

```bash
curl -fsS http://localhost:8080/api/status
curl -fsS http://localhost:8080/ui/patterns >/dev/null
curl -fsS http://localhost:8080/ui/rooms >/dev/null
curl -fsS http://localhost:8080/ui/scenes >/dev/null
curl -fsS http://localhost:8080/ui/rules >/dev/null
```

## 🧯 Troubleshooting

- Port already in use: run with a different port, e.g. `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`
- Docker rebuild: `docker-compose up -d --build`
- Reset Docker DB: `docker-compose down -v`

## 📁 Project Structure

```
src/main/java/com/smarthome/
├── SmartHomeApplication.java          # Main application
├── config/                            # Spring configuration
├── controller/                        # REST controllers
│   ├── SmartHomeController.java       # API endpoints
│   └── WebController.java             # Web UI controller
├── domain/                            # JPA entities
├── repository/                        # Spring Data repositories
├── service/                           # Business logic
│   └── SmartHomeService.java          # Main service
└── pattern/
    ├── creational/
    │   ├── singleton/                 # HomeController
    │   ├── factory/                   # Device factories
    │   ├── abstractfactory/           # Ecosystem factories
    │   ├── builder/                   # AutomationRule builder
    │   └── prototype/                 # Configuration prototypes
    ├── structural/
    │   ├── adapter/                   # Legacy device adapter
    │   ├── bridge/                    # Remote control bridge
    │   ├── composite/                 # Device groups
    │   ├── decorator/                 # Device decorators
    │   ├── facade/                    # Smart home facade
    │   ├── flyweight/                 # Device type flyweights
    │   └── proxy/                     # Device proxy
    └── behavioral/
        ├── chain/                     # Alert handler chain
        ├── command/                   # Device commands
        ├── interpreter/               # Rule interpreter
        ├── iterator/                  # Device iterators
        ├── mediator/                  # Device mediator
        ├── memento/                   # Scene mementos
        ├── observer/                  # Event observers
        ├── state/                     # Device states
        ├── strategy/                  # Energy strategies
        ├── templatemethod/            # Device initializers
        └── visitor/                   # Device visitors
```

## 🛠️ Technology Stack

- **Java 17+**
- **Spring Boot 3.2**
- **Spring Data JPA**
- **PostgreSQL** (production) / **H2** (development)
- **Thymeleaf** (templates)
- **Docker & Docker Compose**
- **Lombok**

## 📝 License

This project is for educational purposes, demonstrating design patterns in a practical application.
