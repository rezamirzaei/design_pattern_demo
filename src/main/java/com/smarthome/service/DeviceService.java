package com.smarthome.service;

import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.DeviceType;
import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.creational.abstractfactory.HomeKitFactory;
import com.smarthome.pattern.creational.abstractfactory.SmartDeviceAbstractFactory;
import com.smarthome.pattern.creational.abstractfactory.SmartThingsFactory;
import com.smarthome.pattern.creational.abstractfactory.SmartThingsSensor;
import com.smarthome.pattern.creational.factory.*;
import com.smarthome.pattern.creational.singleton.HomeController;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.web.viewmodel.DeviceView;
import com.smarthome.web.viewmodel.StatusView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages device CRUD, runtime state, and home mode.
 * The database is the single source of truth for device on/off state.
 */
@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final HomeController homeController = HomeController.INSTANCE;
    private final AtomicReference<HomeMode> homeMode = new AtomicReference<>(HomeMode.NORMAL);

    public DeviceService(DeviceRepository deviceRepository,
                         SimpMessagingTemplate messagingTemplate) {
        this.deviceRepository = deviceRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ── Queries ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DeviceView> getDevices() {
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getLocation)
                        .thenComparing(DeviceEntity::getName))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceView getDevice(String id) {
        return toView(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public StatusView getStatus() {
        int active = (int) deviceRepository.findAll().stream()
                .filter(DeviceEntity::isOn).count();
        return new StatusView("ONLINE", homeMode.get(), active);
    }

    @Transactional(readOnly = true)
    public List<String> getLocations() {
        return deviceRepository.findAll().stream()
                .map(DeviceEntity::getLocation)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    // ── Commands ──────────────────────────────────────────────

    @Transactional
    public DeviceView controlDevice(String id, boolean turnOn) {
        DeviceEntity device = findOrThrow(id);
        device.setOn(turnOn);
        deviceRepository.save(device);

        Device runtime = ensureRuntimeDevice(device);
        if (turnOn) runtime.turnOn(); else runtime.turnOff();

        DeviceView view = toView(device);
        broadcast("device", view);
        return view;
    }

    @Transactional
    public List<DeviceView> controlRoom(String room, boolean turnOn) {
        List<DeviceEntity> devices = deviceRepository.findByLocationIgnoreCase(room);
        for (DeviceEntity d : devices) {
            d.setOn(turnOn);
            Device rt = ensureRuntimeDevice(d);
            if (turnOn) rt.turnOn(); else rt.turnOff();
        }
        deviceRepository.saveAll(devices);
        List<DeviceView> views = devices.stream().map(this::toView).toList();
        views.forEach(v -> broadcast("device", v));
        return views;
    }

    public HomeMode setHomeMode(HomeMode mode) {
        homeMode.set(mode);
        homeController.setHomeMode(mode);
        return mode;
    }

    public HomeMode getHomeMode() {
        return homeMode.get();
    }

    // ── Factory pattern integration ───────────────────────────

    @Transactional
    public DeviceView createDeviceViaFactory(DeviceType type, String name, String location) {
        DeviceFactory factory = selectFactory(type);
        Device created = factory.createDevice(name, location);
        String id = generateId(type);

        DeviceEntity entity = new DeviceEntity(id, name, type, location,
                created.isOn(), type.getDefaultRatedPowerWatts(), "LOCAL");
        deviceRepository.save(entity);
        homeController.registerDevice(id, created);
        return toView(entity);
    }

    @Transactional
    public List<DeviceView> createDevicesViaAbstractFactory(String ecosystem, String location) {
        SmartDeviceAbstractFactory factory = selectAbstractFactory(ecosystem);
        String eco = factory.getEcosystemName();

        record Seed(DeviceType type, String displayName, Device device) {}
        List<Seed> seeds = List.of(
                new Seed(DeviceType.LIGHT, eco + " Light", factory.createLight(eco + " Light", location)),
                new Seed(DeviceType.THERMOSTAT, eco + " Thermostat", factory.createThermostat(eco + " Thermostat", location)),
                new Seed(DeviceType.LOCK, eco + " Lock", factory.createLock(eco + " Lock", location)),
                new Seed(DeviceType.SENSOR, eco + " Sensor", factory.createSensor(eco + " Sensor", location))
        );

        List<DeviceEntity> entities = new ArrayList<>();
        for (Seed s : seeds) {
            String id = generateId(s.type);
            DeviceEntity entity = new DeviceEntity(id, s.displayName, s.type, location,
                    s.device.isOn(), s.type.getDefaultRatedPowerWatts(),
                    ecosystem.toUpperCase(Locale.ROOT));
            entities.add(entity);
            homeController.registerDevice(id, s.device);
        }
        deviceRepository.saveAll(entities);
        return entities.stream().map(this::toView).toList();
    }

    // ── Internal helpers ──────────────────────────────────────

    public DeviceEntity findOrThrow(String id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + id));
    }

    public Device ensureRuntimeDevice(DeviceEntity entity) {
        Device existing = homeController.getDevice(entity.getId());
        if (existing != null) return existing;

        Device created = switch (entity.getType()) {
            case LIGHT -> new SmartLight(entity.getName(), entity.getLocation());
            case THERMOSTAT -> new SmartThermostat(entity.getName(), entity.getLocation());
            case CAMERA -> new SmartCamera(entity.getName(), entity.getLocation());
            case LOCK -> new SmartLock(entity.getName(), entity.getLocation());
            case SENSOR -> new SmartThingsSensor(entity.getName(), entity.getLocation());
        };
        if (entity.isOn()) created.turnOn(); else created.turnOff();
        homeController.registerDevice(entity.getId(), created);
        return created;
    }

    public Map<String, Boolean> syncDatabaseFromRuntime() {
        Map<String, Boolean> changes = new LinkedHashMap<>();
        List<DeviceEntity> all = deviceRepository.findAll();
        for (DeviceEntity e : all) {
            Device d = homeController.getDevice(e.getId());
            if (d != null && e.isOn() != d.isOn()) {
                e.setOn(d.isOn());
                changes.put(e.getId(), d.isOn());
            }
        }
        deviceRepository.saveAll(all);
        return changes;
    }

    public DeviceView toView(DeviceEntity d) {
        return new DeviceView(d.getId(), d.getInfo(), d.getType(), d.getLocation(),
                d.isOn(), d.isOn() ? d.getRatedPowerWatts() : 0);
    }

    public void broadcast(String topic, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/" + topic, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for '{}': {}", topic, e.getMessage());
        }
    }

    private DeviceFactory selectFactory(DeviceType type) {
        return switch (type) {
            case LIGHT -> new LightFactory();
            case THERMOSTAT -> new ThermostatFactory();
            case CAMERA -> new CameraFactory();
            case LOCK -> new LockFactory();
            case SENSOR -> throw new IllegalArgumentException("Factory Method does not support: " + type);
        };
    }

    private SmartDeviceAbstractFactory selectAbstractFactory(String ecosystem) {
        return switch (ecosystem.toUpperCase(Locale.ROOT)) {
            case "HOMEKIT" -> new HomeKitFactory();
            default -> new SmartThingsFactory();
        };
    }

    private String generateId(DeviceType type) {
        return type.name().toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

