# 🏠 Smart Home Design Patterns Demo

> **A comprehensive Smart Home Automation System demonstrating all 23 Gang of Four (GoF) Design Patterns in a real-world, interactive Spring Boot application.**

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

## 🚀 Quick Start

Get the system running in minutes:

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/smarthome-patterns.git

# 2. Build and Run with Docker Compose
docker-compose up --build
```

Click here to access the dashboard: **[http://localhost:8080](http://localhost:8080)**

## 📖 Project Overview

This project isn't just a code repository; it's an **interactive learning platform**. It implements a fully functional Smart Home control system where every major feature is powered by a specific Design Pattern.

You can interact with the system via a **modern, glassmorphism-styled Web Dashboard** or a robust **REST API**.

### ✨ Key Features
- **Interactive UI Dashboard**: Manage devices, rooms, scenes, and rules with a beautiful dark-mode interface.
- **Pattern Playground**: A dedicated section in the UI to visualize and trigger each design pattern (e.g., drag-and-drop Command pattern, visual Observer network).
- **Automation Engine**: Create complex logic rules (Interpreter + Builder patterns).
- **Persistent Storage**: Data persistence using PostgreSQL (Production) or H2 (Dev).
- **Containerized**: Full Docker Compose setup for easy deployment.

---

## 📐 Design Patterns Implemented

The core architecture maps real-world smart home concepts to GoF patterns:

### 🔨 Creational Patterns (5)
| Pattern | Real-World Concept | Implementation Class |
|---------|-------------------|----------------------|
| **Singleton** | Central Home Controller | `HomeController` (One brain managing the house) |
| **Factory Method** | Device Assembly | `DeviceFactory` (Creating Lights, Cameras, Locks dynamically) |
| **Abstract Factory** | Ecosystem Support | `SmartDeviceAbstractFactory` (Supporting HomeKit vs SmartThings) |
| **Builder** | Complex Rule Creation | `AutomationRule.Builder` (Fluent API for "If X then Y") |
| **Prototype** | Cloning Configs | `DeviceConfiguration` (Quickly duplicate device settings) |

### 🏗️ Structural Patterns (7)
| Pattern | Real-World Concept | Implementation Class |
|---------|-------------------|----------------------|
| **Adapter** | Legacy Device Support | `LegacyThermostatAdapter` (Making old tech speak new APIs) |
| **Bridge** | Universal Remotes | `RemoteControl` (Decoupling remote hardware from logic) |
| **Composite** | Room/Group Control | `DeviceGroup` (Treating a whole room like one device) |
| **Decorator** | Feature Add-ons | `DeviceDecorator` (Wrapping logging/security around devices) |
| **Facade** | One-Click Scenes | `SmartHomeFacade` (Simplifying "Movie Mode" complexity) |
| **Flyweight** | Metadata Optimization | `DeviceType` (Sharing icons/descriptions across thousands of devices) |
| **Proxy** | Access Control | `DeviceProxy` (Lazy loading & security checks for cameras) |

### 🎭 Behavioral Patterns (11)
| Pattern | Real-World Concept | Implementation Class |
|---------|-------------------|----------------------|
| **Chain of Responsibility** | Alert System | `AlertHandler` (Smoke -> Phone -> Email -> 911) |
| **Command** | Remote Buttons | `Command` (Undo/Redo capabilities for switches) |
| **Interpreter** | Rule Engine | `RuleInterpreter` (Parsing "motion AND dark" logic) |
| **Iterator** | Device Browser | `DeviceIterator` (Looping through custom lists) |
| **Mediator** | Central Hub | `SmartHomeMediator` (Devices talk to Hub, not each other) |
| **Memento** | Scene Snapshots | `DeviceStateMemento` (Save state -> Restore later) |
| **Observer** | Event Subscribers | `DeviceObserver` (Mobile app updates on sensor trigger) |
| **State** | Device Modes | `DeviceState` (Handling On, Off, Error, FirmwareUpdate states) |
| **Strategy** | Energy Saving | `EnergyStrategy` (Switching algorithms: Eco vs Performance) |
| **Template Method** | Boot Sequence | `DeviceInitializer` (Standard startup with custom steps) |
| **Visitor** | System Diagnostics | `DeviceVisitor` (Running maintenance/security audits) |

---

## 🚀 Quick Start

### Option 1: Docker (Recommended)
The easiest way to run the full stack including the database.

```bash
# 1. Start services
docker-compose up --build -d

# 2. Access variables
# Web UI: http://localhost:8080
# DB Console: http://localhost:8080/h2-console
```

### Option 2: Local Development (Maven)
Requires JDK 17+ installed. Uses H2 in-memory database by default.

```bash
# Run the application
./mvnw spring-boot:run

# The application will start on port 8080.
```

---

## 🖥️ User Interface Guide

The application features a comprehensive web interface:

1.  **🏠 Dashboard**: Overview of system status, active home mode (Singleton), and quick scenes (Facade).
2.  **📱 Devices**: List of all connected devices with control toggles. Use the **Factory** tool here to spawn new devices.
3.  **🏢 Rooms**: Organize devices into rooms. Utilizes the **Composite** pattern to control entire rooms at once.
4.  **🎬 Scenes**: Save current device states as presets (**Memento**) and restore them later.
5.  **🧠 Rules**: Define automation logic (e.g., "IF motion THEN lights") using the **Interpreter** & **Builder** patterns.
6.  **📐 Pattern Lab**: A dedicated educational area where you can visualized specific patterns like the **Chain of Responsibility** alert flows or the **Mediator** network.

---

## 🌐 API Reference

You can also interact programmatically via REST endpoints:

### Core Controls
*   `GET /api/status` - System & Home Mode status
*   `GET /api/devices` - List all smart devices
*   `POST /api/devices/{id}/control?action=on` - Toggle a device

### Pattern Demos
*   `POST /api/patterns/factory/create` - Create dynamic devices
*   `POST /api/patterns/adapter/legacy` - Integrate "old" hardware
*   `POST /api/patterns/command/execute` - Execute reversible commands
*   `POST /api/patterns/chain/alert` - Trigger emergency alerts through the handler chain

_(See `SmartHomeController.java` for the full Swagger/OpenAPI definitions)_

---

## 🛠️ Technology Stack

*   **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA
*   **Frontend**: Thymeleaf, HTML5, CSS3 (Glassmorphism UI), Vanilla JS
*   **Database**: PostgreSQL (Docker), H2 (Local fallback)
*   **Build Tool**: Maven
*   **Containerization**: Docker, Docker Compose

## 📝 License

This project is open-source and intended for educational purposes. Feel free to fork and learn!
