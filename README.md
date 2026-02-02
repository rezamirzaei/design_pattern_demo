# Smart Home Design Patterns Demo

A comprehensive Smart Home Automation System that demonstrates **all 23 Gang of Four (GoF) Design Patterns** in a real-world application.

## 🏠 Project Overview

This project implements a Smart Home control system with:
- **REST API** for device control
- **Web UI** dashboard
- **PostgreSQL** database
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

## 🚀 Quick Start

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

## 🌐 API Endpoints

### System Status
- `GET /api/status` - Get system status
- `POST /api/mode/{mode}` - Set home mode (NORMAL, AWAY, NIGHT, VACATION)

### Device Control
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device details
- `POST /api/devices/{id}/control?action=on|off` - Control device

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
- `POST /api/patterns/proxy/remote` - Proxy demo
- `POST /api/patterns/chain/alert` - Chain of Responsibility demo
- `POST /api/patterns/command/execute` - Command demo
- `POST /api/patterns/observer/register` - Observer demo
- `POST /api/patterns/strategy/apply` - Strategy demo
- `GET /api/patterns/state/demo` - State demo
- `POST /api/patterns/memento/save` - Memento demo
- `GET /api/patterns/visitor/audit` - Visitor demo

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

- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA**
- **PostgreSQL** (production) / **H2** (development)
- **Thymeleaf** (templates)
- **Docker & Docker Compose**
- **Lombok**

## 📝 License

This project is for educational purposes, demonstrating design patterns in a practical application.
