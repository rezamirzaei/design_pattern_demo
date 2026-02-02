package com.smarthome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.domain.AutomationRuleEntity;
import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.DeviceType;
import com.smarthome.domain.HomeMode;
import com.smarthome.domain.RoomEntity;
import com.smarthome.domain.SceneEntity;
import com.smarthome.pattern.behavioral.chain.ChainDemo;
import com.smarthome.pattern.behavioral.command.CommandDemo;
import com.smarthome.pattern.behavioral.interpreter.InterpreterDemo;
import com.smarthome.pattern.behavioral.iterator.IteratorDemo;
import com.smarthome.pattern.behavioral.mediator.MediatorDemo;
import com.smarthome.pattern.behavioral.memento.MementoDemo;
import com.smarthome.pattern.creational.abstractfactory.SmartDeviceAbstractFactory;
import com.smarthome.pattern.creational.abstractfactory.HomeKitFactory;
import com.smarthome.pattern.creational.abstractfactory.SmartThingsSensor;
import com.smarthome.pattern.creational.abstractfactory.SmartThingsFactory;
import com.smarthome.pattern.creational.builder.AutomationRule;
import com.smarthome.pattern.creational.factory.SmartCamera;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartLock;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import com.smarthome.pattern.creational.factory.CameraFactory;
import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.DeviceFactory;
import com.smarthome.pattern.creational.factory.LightFactory;
import com.smarthome.pattern.creational.factory.LockFactory;
import com.smarthome.pattern.creational.factory.ThermostatFactory;
import com.smarthome.pattern.creational.prototype.ConfigurationPrototypeRegistry;
import com.smarthome.pattern.creational.prototype.DeviceConfiguration;
import com.smarthome.pattern.creational.singleton.HomeController;
import com.smarthome.pattern.structural.adapter.LegacyThermostat;
import com.smarthome.pattern.structural.adapter.LegacyThermostatAdapter;
import com.smarthome.pattern.structural.bridge.AdvancedRemote;
import com.smarthome.pattern.structural.bridge.BasicRemote;
import com.smarthome.pattern.structural.bridge.RadioDevice;
import com.smarthome.pattern.structural.bridge.TVDevice;
import com.smarthome.pattern.structural.composite.DeviceGroup;
import com.smarthome.pattern.structural.composite.SingleDevice;
import com.smarthome.pattern.structural.decorator.CachingDecorator;
import com.smarthome.pattern.structural.decorator.LoggingDecorator;
import com.smarthome.pattern.structural.decorator.SecurityDecorator;
import com.smarthome.pattern.structural.facade.SmartHomeFacade;
import com.smarthome.pattern.structural.flyweight.FlyweightDemo;
import com.smarthome.pattern.structural.proxy.DeviceProxy;
import com.smarthome.repository.AutomationRuleRepository;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.RoomRepository;
import com.smarthome.repository.SceneRepository;
import com.smarthome.web.viewmodel.AutomationRuleView;
import com.smarthome.web.viewmodel.DeviceView;
import com.smarthome.web.viewmodel.RoomView;
import com.smarthome.web.viewmodel.SceneView;
import com.smarthome.web.viewmodel.StatusView;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class SmartHomeService {
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final SceneRepository sceneRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final HomeController homeController = HomeController.INSTANCE;

    private final Map<String, Set<String>> observersByDevice = new ConcurrentHashMap<>();
    // Fixed: Added list to store temporary rules for the Builder Demo
    private final List<AutomationRule> rules = new ArrayList<>();

    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(\\s*(.*?)\\s*\\)\\s*$");

    public SmartHomeService(
            DeviceRepository deviceRepository,
            RoomRepository roomRepository,
            SceneRepository sceneRepository,
            AutomationRuleRepository automationRuleRepository,
            ObjectMapper objectMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.sceneRepository = sceneRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    private void broadcastUpdate(String type, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/" + type, payload);
        } catch (Exception e) {
            // Log but don't break transaction if websocket fails
            System.err.println("Failed to broadcast update: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<DeviceView> getDevices() {
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getLocation).thenComparing(DeviceEntity::getName))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceView getDevice(String id) {
        return toView(getDeviceEntityOrThrow(id));
    }

    @Transactional
    public DeviceView controlDevice(String id, boolean turnOn) {
        DeviceEntity device = getDeviceEntityOrThrow(id);
        device.setOn(turnOn);
        deviceRepository.save(device);

        Device runtimeDevice = ensureRuntimeDevice(device);
        if (turnOn) {
            runtimeDevice.turnOn();
        } else {
            runtimeDevice.turnOff();
        }

        DeviceView updatedView = toView(device);
        broadcastUpdate("device", updatedView);
        return updatedView;
    }

    @Transactional
    public List<DeviceView> controlRoom(String room, boolean turnOn) {
        List<DeviceEntity> devices = deviceRepository.findByLocationIgnoreCase(room);
        for (DeviceEntity device : devices) {
            device.setOn(turnOn);
            Device runtimeDevice = ensureRuntimeDevice(device);
            if (turnOn) {
                runtimeDevice.turnOn();
            } else {
                runtimeDevice.turnOff();
            }
        }
        deviceRepository.saveAll(devices);
        List<DeviceView> views = devices.stream().map(this::toView).toList();
        views.forEach(v -> broadcastUpdate("device", v));
        return views;
    }

    @Transactional(readOnly = true)
    public List<String> getRooms() {
        List<String> rooms = roomRepository.findAll().stream()
                .map(RoomEntity::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (!rooms.isEmpty()) {
            return rooms;
        }

        return deviceRepository.findAll().stream()
                .map(DeviceEntity::getLocation)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomView> getRoomViews() {
        return roomRepository.findAll().stream()
                .sorted(Comparator.comparing(RoomEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toRoomView)
                .toList();
    }

    @Transactional
    public RoomView createRoom(String name, String floor, String roomType) {
        String normalizedName = requireText(name, "Room name is required");
        roomRepository.findByName(normalizedName).ifPresent(existing -> {
            throw new IllegalArgumentException("Room already exists: " + normalizedName);
        });

        RoomEntity room = new RoomEntity();
        room.setName(normalizedName);
        room.setFloor(blankToNull(floor));
        room.setRoomType(blankToNull(roomType));
        roomRepository.save(room);
        return toRoomView(room);
    }

    @Transactional
    public RoomView assignDeviceToRoom(Long roomId, String deviceId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room id is required");
        }
        String normalizedDeviceId = requireText(deviceId, "Device id is required");

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        DeviceEntity device = getDeviceEntityOrThrow(normalizedDeviceId);
        device.setLocation(room.getName());
        deviceRepository.save(device);

        boolean alreadyAssigned = room.getDevices().stream()
                .anyMatch(existing -> normalizedDeviceId.equals(existing.getId()));
        if (!alreadyAssigned) {
            room.getDevices().add(device);
        }
        roomRepository.save(room);

        return toRoomView(room);
    }

    @Transactional
    public RoomView unassignDeviceFromRoom(Long roomId, String deviceId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room id is required");
        }
        String normalizedDeviceId = requireText(deviceId, "Device id is required");

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        boolean removed = room.getDevices().removeIf(device -> normalizedDeviceId.equals(device.getId()));
        if (removed) {
            DeviceEntity device = getDeviceEntityOrThrow(normalizedDeviceId);
            device.setLocation("Unassigned");
            deviceRepository.save(device);
            roomRepository.save(room);
        }
        return toRoomView(room);
    }

    @Transactional(readOnly = true)
    public List<SceneView> getScenes() {
        return sceneRepository.findAll().stream()
                .sorted(Comparator.comparing(SceneEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toSceneView)
                .toList();
    }

    @Transactional
    public SceneView createSceneSnapshot(String name, String description, boolean favorite) {
        String normalizedName = requireText(name, "Scene name is required");
        Map<String, Boolean> snapshot = captureCurrentDeviceStates();

        String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize device states: " + e.getMessage(), e);
        }

        SceneEntity scene = sceneRepository.findByName(normalizedName).orElseGet(SceneEntity::new);
        scene.setName(normalizedName);
        scene.setDescription(blankToNull(description));
        scene.setDeviceStates(json);
        scene.setIsFavorite(favorite);
        sceneRepository.save(scene);
        return toSceneView(scene);
    }

    @Transactional
    public SceneView toggleSceneFavorite(Long sceneId) {
        if (sceneId == null) {
            throw new IllegalArgumentException("Scene id is required");
        }
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));
        boolean newValue = !Boolean.TRUE.equals(scene.getIsFavorite());
        scene.setIsFavorite(newValue);
        sceneRepository.save(scene);
        return toSceneView(scene);
    }

    @Transactional
    public Map<String, Object> applySceneSnapshot(Long sceneId) {
        if (sceneId == null) {
            throw new IllegalArgumentException("Scene id is required");
        }
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));

        Map<String, Boolean> targetStates = parseDeviceStates(scene.getDeviceStates());
        if (targetStates.isEmpty()) {
            return Map.of(
                    "sceneId", sceneId,
                    "sceneName", scene.getName(),
                    "message", "Scene has no device states to apply",
                    "timestamp", Instant.now().toString()
            );
        }

        Map<String, Boolean> before = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn, (a, b) -> a, LinkedHashMap::new));

        List<String> missingDevices = new ArrayList<>();
        List<DeviceEntity> updated = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : targetStates.entrySet()) {
            String deviceId = entry.getKey();
            boolean desiredOn = Boolean.TRUE.equals(entry.getValue());

            Optional<DeviceEntity> maybe = deviceRepository.findById(deviceId);
            if (maybe.isEmpty()) {
                missingDevices.add(deviceId);
                continue;
            }
            DeviceEntity entity = maybe.get();
            entity.setOn(desiredOn);
            Device runtime = ensureRuntimeDevice(entity);
            if (desiredOn) {
                runtime.turnOn();
            } else {
                runtime.turnOff();
            }
            updated.add(entity);
        }
        deviceRepository.saveAll(updated);

        Map<String, Boolean> after = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn, (a, b) -> a, LinkedHashMap::new));

        Map<String, Map<String, Boolean>> diffs = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : after.entrySet()) {
            String deviceId = entry.getKey();
            boolean afterState = entry.getValue();
            boolean beforeState = before.getOrDefault(deviceId, false);
            if (beforeState != afterState) {
                diffs.put(deviceId, Map.of("before", beforeState, "after", afterState));
            }
        }

        return Map.of(
                "sceneId", sceneId,
                "sceneName", scene.getName(),
                "missingDevices", missingDevices,
                "changedDevices", diffs,
                "timestamp", Instant.now().toString()
        );
    }

    @Transactional
    public Map<String, Object> deleteScene(Long sceneId) {
        if (sceneId == null) {
            throw new IllegalArgumentException("Scene id is required");
        }
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));
        sceneRepository.delete(scene);
        return Map.of(
                "sceneId", sceneId,
                "sceneName", scene.getName(),
                "deleted", true
        );
    }

    @Transactional(readOnly = true)
    public List<AutomationRuleView> getAutomationRules() {
        return automationRuleRepository.findAll().stream()
                .sorted(Comparator.comparing(AutomationRuleEntity::getPriority, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(AutomationRuleEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toRuleView)
                .toList();
    }

    @Transactional
    public AutomationRuleView createAutomationRule(
            String name,
            String description,
            String triggerCondition,
            String actionScript,
            Integer priority
    ) {
        String normalizedName = requireText(name, "Rule name is required");
        String normalizedCondition = requireText(triggerCondition, "Trigger condition is required");
        String normalizedAction = requireText(actionScript, "Action script is required");

        // Use the Builder pattern as a first-class way to create rules, then persist the rule entity.
        new AutomationRule.Builder()
                .name(normalizedName)
                .trigger("expression")
                .condition(normalizedCondition)
                .action(normalizedAction)
                .build();

        AutomationRuleEntity entity = new AutomationRuleEntity();
        entity.setName(normalizedName);
        entity.setDescription(blankToNull(description));
        entity.setTriggerCondition(normalizedCondition);
        entity.setActionScript(normalizedAction);
        entity.setIsEnabled(true);
        entity.setPriority(priority == null ? 5 : priority);
        automationRuleRepository.save(entity);
        return toRuleView(entity);
    }

    @Transactional
    public AutomationRuleView toggleAutomationRule(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule id is required");
        }
        AutomationRuleEntity entity = automationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        boolean newValue = !Boolean.TRUE.equals(entity.getIsEnabled());
        entity.setIsEnabled(newValue);
        automationRuleRepository.save(entity);
        return toRuleView(entity);
    }

    @Transactional
    public Map<String, Object> deleteAutomationRule(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule id is required");
        }
        AutomationRuleEntity entity = automationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        automationRuleRepository.delete(entity);
        return Map.of(
                "ruleId", ruleId,
                "ruleName", entity.getName(),
                "deleted", true
        );
    }

    @Transactional
    public Map<String, Object> runAutomationRule(Long ruleId, Map<String, Object> variables, boolean executeActions) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule id is required");
        }
        AutomationRuleEntity entity = automationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        Map<String, Object> interpreter = InterpreterDemo.evaluate(entity.getTriggerCondition(), variables);
        boolean matched = Boolean.TRUE.equals(interpreter.get("result"));

        List<Map<String, Object>> actions = new ArrayList<>();
        boolean executed = false;
        if (matched && executeActions && Boolean.TRUE.equals(entity.getIsEnabled())) {
            actions = executeActionScript(entity.getActionScript());
            executed = true;
            entity.setLastTriggered(LocalDateTime.now());
            automationRuleRepository.save(entity);
        }

        return Map.of(
                "rule", toRuleView(entity),
                "variables", variables == null ? Map.of() : variables,
                "matched", matched,
                "executeActions", executeActions,
                "executed", executed,
                "actions", actions,
                "interpreter", interpreter
        );
    }

    @Transactional(readOnly = true)
    public StatusView getStatus() {
        int active = (int) deviceRepository.findAll().stream().filter(DeviceEntity::isOn).count();
        return new StatusView("ONLINE", homeController.getHomeModeEnum(), active);
    }

    public HomeMode setHomeMode(HomeMode mode) {
        homeController.setHomeMode(mode);
        return homeController.getHomeModeEnum();
    }

    @Transactional
    public DeviceView createDeviceViaFactory(DeviceType type, String name, String location) {
        DeviceFactory factory = selectFactory(type);
        Device created = factory.createDevice(name, location);

        String id = generateDeviceId(type);
        DeviceEntity entity = new DeviceEntity(
                id,
                name,
                type,
                location,
                created.isOn(),
                type.getDefaultRatedPowerWatts(),
                "LOCAL"
        );
        deviceRepository.save(entity);
        homeController.registerDevice(id, created);
        return toView(entity);
    }

    @Transactional
    public List<DeviceView> createDevicesViaAbstractFactory(String ecosystem, String location) {
        SmartDeviceAbstractFactory factory = selectAbstractFactory(ecosystem);

        String lightName = factory.getEcosystemName() + " Light";
        String thermostatName = factory.getEcosystemName() + " Thermostat";
        String lockName = factory.getEcosystemName() + " Lock";
        String sensorName = factory.getEcosystemName() + " Sensor";

        List<DeviceSeed> seeds = List.of(
                new DeviceSeed(DeviceType.LIGHT, lightName, factory.createLight(lightName, location)),
                new DeviceSeed(DeviceType.THERMOSTAT, thermostatName, factory.createThermostat(thermostatName, location)),
                new DeviceSeed(DeviceType.LOCK, lockName, factory.createLock(lockName, location)),
                new DeviceSeed(DeviceType.SENSOR, sensorName, factory.createSensor(sensorName, location))
        );

        List<DeviceEntity> entities = new ArrayList<>();
        for (DeviceSeed seed : seeds) {
            String id = generateDeviceId(seed.type);
            DeviceEntity entity = new DeviceEntity(
                    id,
                    seed.displayName,
                    seed.type,
                    location,
                    seed.device.isOn(),
                    seed.type.getDefaultRatedPowerWatts(),
                    ecosystemLabel(ecosystem)
            );
            entities.add(entity);
            homeController.registerDevice(id, seed.device);
        }

        deviceRepository.saveAll(entities);
        return entities.stream().map(this::toView).toList();
    }

    public AutomationRule buildAutomationRule(String name, String trigger, String condition, String action) {
        AutomationRule rule = new AutomationRule.Builder()
                .name(name)
                .trigger(trigger)
                .condition(condition)
                .action(action)
                .build();
        rules.add(rule);
        return rule;
    }

    public List<Map<String, Object>> listPrototypeTemplates() {
        List<String> keys = new ArrayList<>();
        for (String key : ConfigurationPrototypeRegistry.getAvailablePrototypes()) {
            keys.add(key);
        }
        keys.sort(String::compareToIgnoreCase);

        List<Map<String, Object>> out = new ArrayList<>();
        for (String key : keys) {
            DeviceConfiguration cloned = ConfigurationPrototypeRegistry.getClone(key);
            out.add(Map.of(
                    "prototypeKey", key,
                    "presetName", cloned.getPresetName(),
                    "deviceType", cloned.getDeviceType(),
                    "settings", cloned.getSettings()
            ));
        }
        return out;
    }

    // =====================
    // Pattern UI support APIs
    // =====================

    // ----- COMMAND (Undo/Redo) -----
    // We store last command per device so we can "undo" by executing the reverse command.
    private final Map<String, String> lastCommandByDevice = new ConcurrentHashMap<>();

    public Map<String, Object> commandUndo() {
        if (lastCommandByDevice.isEmpty()) {
            return Map.of("pattern", "Command", "message", "Nothing to undo" );
        }
        // Pick most recently touched device by iterating insertion order isn't guaranteed;
        // we'll just pick any entry (good enough for demo UI).
        Map.Entry<String, String> entry = lastCommandByDevice.entrySet().iterator().next();
        String deviceId = entry.getKey();
        String last = entry.getValue();
        String undo = reverseCommand(last);
        Map<String, Object> exec = commandExecute(deviceId, undo);
        return Map.of(
                "pattern", "Command",
                "action", "undo",
                "deviceId", deviceId,
                "undoCommand", undo,
                "result", exec
        );
    }

    public Map<String, Object> commandRedo() {
        // For demo: redo just re-executes the stored last command.
        if (lastCommandByDevice.isEmpty()) {
            return Map.of("pattern", "Command", "message", "Nothing to redo" );
        }
        Map.Entry<String, String> entry = lastCommandByDevice.entrySet().iterator().next();
        String deviceId = entry.getKey();
        String cmd = entry.getValue();
        Map<String, Object> exec = commandExecute(deviceId, cmd);
        return Map.of(
                "pattern", "Command",
                "action", "redo",
                "deviceId", deviceId,
                "command", cmd,
                "result", exec
        );
    }

    private static String reverseCommand(String command) {
        String c = command == null ? "" : command.trim().toUpperCase(Locale.ROOT);
        if (c.equals("ON")) return "OFF";
        if (c.equals("OFF")) return "ON";
        if (c.startsWith("BRIGHTNESS:")) return "OFF";
        return "OFF";
    }

    // ----- MEMENTO (List/Restore) -----
    // We use the existing DB Scene snapshots as the "memento store" for UI list/restore.
    // This is a pragmatic real-world adaptation: DB row is the memento.

    public Map<String, Object> listMementos() {
        List<SceneView> scenes = getScenes();
        // Explicitly create list of generic maps to satisfy return type inference
        List<Map<String, Object>> items = scenes.stream()
                .<Map<String, Object>>map(s -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", s.id());
                    map.put("name", s.name());
                    map.put("favorite", s.favorite());
                    map.put("deviceCount", s.deviceCount());
                    map.put("createdAt", s.createdAt() == null ? null : s.createdAt().toString());
                    return map;
                })
                .toList();
        return Map.of(
                "pattern", "Memento",
                "count", items.size(),
                "scenes", items
        );
    }

    public Map<String, Object> restoreMemento(String sceneName) {
        // Restore by name (or create then apply if missing)
        String normalized = requireText(sceneName, "sceneName is required");
        SceneEntity scene = sceneRepository.findByName(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + normalized));
        Map<String, Object> apply = applySceneSnapshot(scene.getId());
        return Map.of(
                "pattern", "Memento",
                "action", "restore",
                "sceneName", normalized,
                "sceneId", scene.getId(),
                "result", apply
        );
    }

    // ----- OBSERVER (Subscribe/Trigger) -----
    // We keep subscriptions in-memory (observerType list per device) and return the notification fan-out.

    public Map<String, Object> observerSubscribe(String deviceId, String observerType) {
        String id = requireText(deviceId, "deviceId is required");
        String type = (observerType == null ? "MOBILE" : observerType.trim().toUpperCase(Locale.ROOT));
        observersByDevice.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(type);

        Map<String, Object> response = Map.of(
                "pattern", "Observer",
                "action", "subscribe",
                "deviceId", id,
                "observerType", type,
                "subscribers", observersByDevice.getOrDefault(id, Set.of())
        );
        // Fixed: Broadcast update so the HTML UI sees the new subscription
        broadcastUpdate("observer", response);
        return response;
    }

    public Map<String, Object> observerTrigger(String deviceId, String eventType) {
        String id = requireText(deviceId, "deviceId is required");
        String event = (eventType == null ? "MOTION" : eventType.trim().toUpperCase(Locale.ROOT));
        Set<String> subs = observersByDevice.getOrDefault(id, Set.of());
        List<String> notified = subs.stream().sorted().map(s -> s + " notified of " + event + " on " + id).toList();

        Map<String, Object> response = Map.of(
                "pattern", "Observer",
                "action", "trigger",
                "deviceId", id,
                "eventType", event,
                "subscriberCount", subs.size(),
                "notifications", notified
        );
        // Fixed: Broadcast update so the HTML UI sees the event trigger
        broadcastUpdate("observer", response);
        return response;
    }

    // ----- MEDIATOR (Notify) -----
    // Keep using the existing demo + echo the requested parameters.

    public Map<String, Object> mediatorNotify(String sourceDeviceId, String event) {
        return Map.of(
                "pattern", "Mediator",
                "sourceDeviceId", sourceDeviceId,
                "event", event,
                "demo", mediatorDemo()
        );
    }

    // ----- TEMPLATE METHOD (Init) -----
    public Map<String, Object> templateInit(String deviceType) {
        return Map.of(
                "pattern", "Template Method",
                "deviceType", deviceType,
                "demo", templateDemo(deviceType)
        );
    }

    private static String requireText(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2
                && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private RoomView toRoomView(RoomEntity room) {
        List<DeviceView> devices = Optional.ofNullable(room.getDevices()).orElse(List.of()).stream()
                .sorted(Comparator.comparing(DeviceEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toView)
                .toList();

        return new RoomView(
                room.getId(),
                room.getName(),
                room.getFloor(),
                room.getRoomType(),
                devices.size(),
                devices
        );
    }

    private SceneView toSceneView(SceneEntity scene) {
        String states = Optional.ofNullable(scene.getDeviceStates()).orElse("");
        int deviceCount = parseDeviceStates(states).size();

        return new SceneView(
                scene.getId(),
                scene.getName(),
                scene.getDescription(),
                Boolean.TRUE.equals(scene.getIsFavorite()),
                deviceCount,
                scene.getCreatedAt(),
                states
        );
    }

    private AutomationRuleView toRuleView(AutomationRuleEntity rule) {
        Integer priority = rule.getPriority();
        return new AutomationRuleView(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getTriggerCondition(),
                rule.getActionScript(),
                Boolean.TRUE.equals(rule.getIsEnabled()),
                priority == null ? 0 : priority,
                rule.getCreatedAt(),
                rule.getLastTriggered()
        );
    }

    private Map<String, Boolean> captureCurrentDeviceStates() {
        syncDatabaseFromRuntimeDevices();
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getId, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, Boolean> parseDeviceStates(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Boolean> parsed = objectMapper.readValue(
                    json,
                    new TypeReference<LinkedHashMap<String, Boolean>>() {}
            );
            return parsed == null ? Map.of() : new LinkedHashMap<>(parsed);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> executeActionScript(String actionScript) {
        String script = blankToNull(actionScript);
        if (script == null) {
            return List.of(Map.of(
                    "status", "noop",
                    "message", "No action script provided"
            ));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        String[] statements = script.split("[;\\n\\r]+");
        for (String raw : statements) {
            String stmt = raw == null ? "" : raw.trim();
            if (stmt.isBlank()) {
                continue;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("statement", stmt);

            Matcher matcher = ACTION_PATTERN.matcher(stmt);
            if (!matcher.matches()) {
                result.put("status", "ignored");
                result.put("message", "Unsupported action format. Use fn(arg), e.g. turn_on(deviceId)");
                results.add(result);
                continue;
            }

            String fn = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            String arg = stripQuotes(matcher.group(2).trim());

            try {
                switch (fn) {
                    case "turn_on", "on" -> {
                        result.put("status", "ok");
                        result.put("action", "turn_on");
                        result.put("device", controlDevice(arg, true));
                    }
                    case "turn_off", "off" -> {
                        result.put("status", "ok");
                        result.put("action", "turn_off");
                        result.put("device", controlDevice(arg, false));
                    }
                    case "toggle" -> {
                        DeviceEntity entity = getDeviceEntityOrThrow(arg);
                        result.put("status", "ok");
                        result.put("action", "toggle");
                        result.put("device", controlDevice(arg, !entity.isOn()));
                    }
                    case "room_on" -> {
                        result.put("status", "ok");
                        result.put("action", "room_on");
                        result.put("room", arg);
                        result.put("devices", controlRoom(arg, true));
                    }
                    case "room_off" -> {
                        result.put("status", "ok");
                        result.put("action", "room_off");
                        result.put("room", arg);
                        result.put("devices", controlRoom(arg, false));
                    }
                    case "mode", "set_mode" -> {
                        HomeMode mode = HomeMode.valueOf(arg.trim().toUpperCase(Locale.ROOT));
                        result.put("status", "ok");
                        result.put("action", "mode");
                        result.put("mode", setHomeMode(mode));
                    }
                    case "scene", "activate_scene" -> {
                        result.put("status", "ok");
                        result.put("action", "scene");
                        result.put("scene", arg);
                        result.put("result", activateScene(arg));
                    }
                    case "apply_scene" -> {
                        SceneEntity scene = sceneRepository.findByName(arg)
                                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + arg));
                        result.put("status", "ok");
                        result.put("action", "apply_scene");
                        result.put("scene", arg);
                        result.put("result", applySceneSnapshot(scene.getId()));
                    }
                    default -> {
                        result.put("status", "ignored");
                        result.put("message", "Unknown action: " + fn);
                    }
                }
            } catch (Exception e) {
                result.put("status", "error");
                result.put("message", e.getMessage());
            }

            results.add(result);
        }

        return results;
    }

    private DeviceEntity getDeviceEntityOrThrow(String id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + id));
    }

    private Device ensureRuntimeDevice(DeviceEntity entity) {
        Device existing = homeController.getDevice(entity.getId());
        if (existing != null) {
            return existing;
        }

        Device created = switch (entity.getType()) {
            case LIGHT -> new SmartLight(entity.getName(), entity.getLocation());
            case THERMOSTAT -> new SmartThermostat(entity.getName(), entity.getLocation());
            case CAMERA -> new SmartCamera(entity.getName(), entity.getLocation());
            case LOCK -> new SmartLock(entity.getName(), entity.getLocation());
            case SENSOR -> new SmartThingsSensor(entity.getName(), entity.getLocation());
        };

        if (entity.isOn()) {
            created.turnOn();
        } else {
            created.turnOff();
        }
        homeController.registerDevice(entity.getId(), created);
        return created;
    }

    private Map<String, Boolean> syncDatabaseFromRuntimeDevices() {
        Map<String, Boolean> changes = new LinkedHashMap<>();
        List<DeviceEntity> entities = deviceRepository.findAll();
        for (DeviceEntity entity : entities) {
            Device device = homeController.getDevice(entity.getId());
            if (device == null) {
                continue;
            }
            boolean newState = device.isOn();
            if (entity.isOn() != newState) {
                entity.setOn(newState);
                changes.put(entity.getId(), newState);
            }
        }
        deviceRepository.saveAll(entities);
        return changes;
    }

    private DeviceView toView(DeviceEntity device) {
        int power = device.isOn() ? device.getRatedPowerWatts() : 0;
        return new DeviceView(
                device.getId(),
                device.getInfo(),
                device.getType(),
                device.getLocation(),
                device.isOn(),
                power
        );
    }

    private SmartDeviceAbstractFactory selectAbstractFactory(String ecosystem) {
        String normalized = ecosystemLabel(ecosystem);
        return switch (normalized) {
            case "HOMEKIT" -> new HomeKitFactory();
            case "SMARTTHINGS" -> new SmartThingsFactory();
            default -> new SmartThingsFactory();
        };
    }

    private DeviceFactory selectFactory(DeviceType type) {
        return switch (type) {
            case LIGHT -> new LightFactory();
            case THERMOSTAT -> new ThermostatFactory();
            case CAMERA -> new CameraFactory();
            case LOCK -> new LockFactory();
            case SENSOR -> throw new IllegalArgumentException("Factory Method demo does not support type: " + type);
        };
    }

    private String ecosystemLabel(String ecosystem) {
        return Optional.ofNullable(ecosystem).orElse("SMARTTHINGS").toUpperCase(Locale.ROOT);
    }

    private String generateDeviceId(DeviceType type) {
        return type.name().toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private record DeviceSeed(DeviceType type, String displayName, Device device) {}

    // =====================
    // Existing pattern demo APIs (used by SmartHomeController)
    // =====================

    public Map<String, Object> listPatterns() {
        return Map.of(
                "creational", List.of("singleton", "factory", "abstract-factory", "builder", "prototype"),
                "structural", List.of("adapter", "bridge", "composite", "decorator", "facade", "flyweight", "proxy"),
                "behavioral", List.of("chain", "command", "interpreter", "iterator", "mediator", "memento", "observer", "state", "strategy", "template", "visitor")
        );
    }

    public Map<String, Object> flyweightDemo() {
        return FlyweightDemo.demo();
    }

    public Map<String, Object> mediatorDemo() {
        return MediatorDemo.motionScenario();
    }

    public Map<String, Object> templateDemo(String deviceType) {
        return com.smarthome.pattern.behavioral.templatemethod.TemplateMethodDemo.run(deviceType);
    }

    public Map<String, Object> interpreterEvaluate(String rule, Map<String, Object> variables) {
        return InterpreterDemo.evaluate(rule, variables);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> iteratorDemo(String filterType, String filterValue) {
        List<IteratorDemo.DeviceSeed> seeds = deviceRepository.findAll().stream()
                .map(entity -> new IteratorDemo.DeviceSeed(
                        ensureRuntimeDevice(entity),
                        entity.getLocation(),
                        entity.getType().name()
                ))
                .toList();
        return IteratorDemo.iterate(seeds, filterType, filterValue);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> roomsComposite() {
        DeviceGroup home = new DeviceGroup("Home");
        Map<String, DeviceGroup> rooms = new LinkedHashMap<>();

        for (DeviceEntity entity : deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getLocation).thenComparing(DeviceEntity::getName))
                .toList()) {
            String room = entity.getLocation() == null ? "Unknown" : entity.getLocation();
            DeviceGroup group = rooms.computeIfAbsent(room, DeviceGroup::new);
            Device runtime = ensureRuntimeDevice(entity);
            group.add(new SingleDevice(entity.getName(), runtime));
        }
        rooms.values().forEach(home::add);

        return Map.of(
                "pattern", "Composite",
                "deviceCount", home.getDeviceCount(),
                "estimatedPower", home.getPowerConsumption(),
                "status", home.getStatus()
        );
    }

    public Map<String, Object> bridgeDemo() {
        TVDevice tv = new TVDevice();
        RadioDevice radio = new RadioDevice();

        BasicRemote tvRemote = new BasicRemote(tv);
        AdvancedRemote radioRemote = new AdvancedRemote(radio);

        tvRemote.togglePower();
        tvRemote.volumeUp();
        tvRemote.channelUp();

        radioRemote.togglePower();
        radioRemote.setVolume(15);
        radioRemote.voiceCommand("mute");
        radioRemote.voiceCommand("channel 101");
        radioRemote.voiceCommand("mute");

        return Map.of(
                "pattern", "Bridge",
                "tv", tvRemote.getStatus(),
                "radio", radioRemote.getStatus()
        );
    }

    public Map<String, Object> adapterDemo(String name, String location) {
        LegacyThermostat legacy = new LegacyThermostat();
        legacy.setPower(true);
        legacy.setTemperatureFahrenheit(72);
        legacy.setMode("heat");

        LegacyThermostatAdapter adapter = new LegacyThermostatAdapter(legacy, name, location);
        adapter.turnOn();
        adapter.operate("TEMPERATURE:21");
        adapter.operate("MODE:auto");

        return Map.of(
                "pattern", "Adapter",
                "legacyStatus", legacy.getLegacyStatus(),
                "adaptedStatus", adapter.getStatus(),
                "adaptedInfo", adapter.getDeviceInfo()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> decoratorWrap(String deviceId, String decorators) {
        // Reuse the existing decorator feature already implemented earlier in repo:
        // For safety here, just return a simple demo response if device not found.
        DeviceEntity entity = getDeviceEntityOrThrow(deviceId);
        Device runtime = ensureRuntimeDevice(entity);

        List<String> normalized = Optional.ofNullable(decorators).orElse("").split(",") == null ? List.of() : List.of(Optional.ofNullable(decorators).orElse("").split(","));

        Device decorated = runtime;
        SecurityDecorator security = null;
        CachingDecorator caching = null;
        for (String decorator : normalized) {
            switch (decorator) {
                case "LOGGING" -> decorated = new LoggingDecorator(decorated);
                case "SECURITY" -> {
                    security = new SecurityDecorator(decorated);
                    security.authenticate("demo-user", "pass1234");
                    decorated = security;
                }
                case "CACHING" -> {
                    caching = new CachingDecorator(decorated, Duration.ofSeconds(15));
                    decorated = caching;
                }
                default -> {
                    // ignore
                }
            }
        }

        String status1 = decorated.getStatus();
        String status2 = decorated.getStatus();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pattern", "Decorator");
        out.put("decorators", normalized);
        out.put("deviceInfo", decorated.getDeviceInfo());
        out.put("status1", status1);
        out.put("status2", status2);
        out.put("securityAuthenticated", security != null && security.isAuthenticated());
        out.put("cacheStats", caching != null ? caching.getCacheStats() : null);
        return out;
    }

    @Transactional
    public Map<String, Object> activateScene(String sceneName) {
        SmartHomeFacade facade = new SmartHomeFacade();
        for (Device device : homeController.getDevicesSnapshot().values()) {
            if (device instanceof SmartLight light) {
                facade.addLight(light);
            } else if (device instanceof SmartCamera camera) {
                facade.addCamera(camera);
            } else if (device instanceof SmartLock lock) {
                facade.addLock(lock);
            } else if (device instanceof SmartThermostat thermostat) {
                facade.addThermostat(thermostat);
            }
        }

        String normalized = Optional.ofNullable(sceneName).orElse("morning").toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "morning" -> {
                facade.goodMorning();
                homeController.setHomeMode(HomeMode.NORMAL);
            }
            case "night" -> {
                facade.goodNight();
                homeController.setHomeMode(HomeMode.NIGHT);
            }
            case "leave" -> {
                facade.leaveHome();
                homeController.setHomeMode(HomeMode.AWAY);
            }
            case "arrive" -> {
                facade.arriveHome();
                homeController.setHomeMode(HomeMode.NORMAL);
            }
            case "movie" -> {
                facade.movieNight();
                homeController.setHomeMode(HomeMode.NIGHT);
            }
            case "party" -> {
                facade.partyMode();
                homeController.setHomeMode(HomeMode.NORMAL);
            }
            default -> {
                facade.goodMorning();
                homeController.setHomeMode(HomeMode.NORMAL);
            }
        }

        Map<String, Boolean> changes = syncDatabaseFromRuntimeDevices();
        return Map.of(
                "pattern", "Facade",
                "scene", normalized,
                "homeStatus", facade.getHomeStatus(),
                "mode", homeController.getHomeModeEnum(),
                "changedDevices", changes,
                "timestamp", Instant.now().toString()
        );
    }

    public Map<String, Object> proxyRemote(String name, String address) {
        String deviceId = "proxy-" + UUID.randomUUID().toString().substring(0, 8);
        DeviceProxy proxy = new DeviceProxy(deviceId, name, address);

        boolean beforeInit = proxy.isInitialized();
        String status1 = proxy.getStatus();
        boolean afterInit = proxy.isInitialized();

        proxy.setAccess("demo-admin", DeviceProxy.AccessLevel.ADMIN);
        proxy.turnOn();
        String status2 = proxy.getStatus();
        String status3 = proxy.getStatus();

        return Map.of(
                "pattern", "Proxy",
                "deviceId", deviceId,
                "address", address,
                "initializedBefore", beforeInit,
                "initializedAfter", afterInit,
                "status1", status1,
                "status2", status2,
                "status3", status3,
                "connected", proxy.isConnected()
        );
    }

    public Map<String, Object> chainAlert(String deviceId, String level, String message) {
        return ChainDemo.process(deviceId, level, message);
    }

    @Transactional
    public Map<String, Object> commandExecute(String deviceId, String command) {
        DeviceEntity entity = getDeviceEntityOrThrow(deviceId);
        Device runtime = ensureRuntimeDevice(entity);
        String effectiveCommand = normalizeCommand(runtime, command);
        Map<String, Object> result = new LinkedHashMap<>(CommandDemo.execute(runtime, effectiveCommand));
        result.put("requestedCommand", command);
        result.put("effectiveCommand", effectiveCommand);
        entity.setOn(runtime.isOn());
        deviceRepository.save(entity);
        lastCommandByDevice.put(deviceId, effectiveCommand);
        return result;
    }

    private static String normalizeCommand(Device device, String command) {
        String normalized = command == null ? "ON" : command.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("DIM") && device instanceof SmartLight) {
            return "BRIGHTNESS:50";
        }
        if (normalized.equals("TEMP_UP") && device instanceof SmartThermostat thermostat) {
            return "TEMPERATURE:" + (thermostat.getTargetTemperature() + 1.0);
        }
        if (normalized.equals("TEMP_DOWN") && device instanceof SmartThermostat thermostat) {
            return "TEMPERATURE:" + (thermostat.getTargetTemperature() - 1.0);
        }
        return normalized;
    }

    public Map<String, Object> saveMemento(String sceneName) {
        // Keep the original pure Memento demo around.
        return MementoDemo.saveAndRestore(sceneName);
    }

    public Map<String, Object> registerObserver(String deviceId, String observerType) {
        Map<String, Object> demo = com.smarthome.pattern.behavioral.observer.ObserverDemo.demo();
        return Map.of(
                "pattern", "Observer",
                "requestedDeviceId", deviceId,
                "requestedObserverType", observerType,
                "demo", demo
        );
    }

    public Map<String, Object> stateDemo() {
        return com.smarthome.pattern.behavioral.state.StateDemo.demo();
    }

    public Map<String, Object> applyEnergyStrategy(String strategy) {
        List<Device> devices = new ArrayList<>(homeController.getDevicesSnapshot().values());
        List<String> before = devices.stream().map(Device::getStatus).toList();

        Map<String, Object> result = com.smarthome.pattern.behavioral.strategy.StrategyDemo.apply(strategy, devices);

        List<String> after = devices.stream().map(Device::getStatus).toList();
        syncDatabaseFromRuntimeDevices();

        return Map.of(
                "pattern", "Strategy",
                "before", before,
                "after", after,
                "result", result
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> visitorAudit(String type) {
        return com.smarthome.pattern.behavioral.visitor.VisitorDemo.audit(type);
    }

    // ===== STATE PATTERN =====
    private String currentPlayerState = "STOPPED";

    public Map<String, Object> stateTransition(String action) {
        String oldState = currentPlayerState;
        String newState = switch (action.toUpperCase(Locale.ROOT)) {
            case "PLAYPAUSE" -> currentPlayerState.equals("PLAYING") ? "PAUSED" : "PLAYING";
            case "STOP" -> "STOPPED";
            case "PLAY" -> "PLAYING";
            case "PAUSE" -> "PAUSED";
            default -> currentPlayerState;
        };
        currentPlayerState = newState;

        Map<String, Object> response = Map.of(
                "pattern", "State",
                "action", action,
                "oldState", oldState,
                "newState", newState,
                "timestamp", Instant.now().toString()
        );
        broadcastUpdate("state", response);
        return response;
    }

    // ===== ITERATOR PATTERN =====
    public Map<String, Object> iteratorIterate(String type, String filter) {
        List<IteratorDemo.DeviceSeed> seeds = deviceRepository.findAll().stream()
                .map(entity -> new IteratorDemo.DeviceSeed(
                        ensureRuntimeDevice(entity),
                        entity.getLocation() == null ? "Unknown" : entity.getLocation(),
                        entity.getType() == null ? "UNKNOWN" : entity.getType().name()
                ))
                .toList();
        return IteratorDemo.iterate(seeds, type, filter);
    }

    // ===== INTERPRETER PATTERN =====
    public Map<String, Object> interpreterEvaluate(String rule) {
        // Parse the rule and create a context with default values
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("motion_detected", true);
        variables.put("is_dark", true);
        variables.put("door_open", false);
        variables.put("temp_high", false);
        variables.put("hour", java.time.LocalTime.now().getHour());

        return InterpreterDemo.evaluate(rule, variables);
    }

    // ===== FLYWEIGHT PATTERN =====
    public Map<String, Object> flyweightDemo(Integer count) {
        int actualCount = count != null && count > 0 ? count : 100;
        return FlyweightDemo.demo(actualCount);
    }

    public Map<String, Object> flyweightStats(Integer instances) {
        int count = instances != null && instances > 0 ? instances : 10_000;
        return FlyweightDemo.stats(count);
    }

    // ===== MEMENTO PATTERN =====
    public Map<String, Object> mementoSave(String sceneName) {
        // Create a scene snapshot using existing functionality
        SceneView scene = createSceneSnapshot(sceneName, "Memento snapshot", false);
        return Map.of(
                "pattern", "Memento",
                "action", "save",
                "sceneName", sceneName,
                "sceneId", scene.id(),
                "deviceCount", scene.deviceCount(),
                "timestamp", Instant.now().toString()
        );
    }

    public Map<String, Object> mementoRestore(String snapshotId) {
        try {
            Long sceneId = Long.parseLong(snapshotId);
            Map<String, Object> result = applySceneSnapshot(sceneId);
            return Map.of(
                    "pattern", "Memento",
                    "action", "restore",
                    "snapshotId", snapshotId,
                    "result", result,
                    "timestamp", Instant.now().toString()
            );
        } catch (NumberFormatException e) {
            // Try by name
            return restoreMemento(snapshotId);
        }
    }

    // ===== BRIDGE PATTERN =====
    public Map<String, Object> bridgeControl(String remote, String device, String action) {
        String deviceType = device == null ? "TV" : device.trim().toUpperCase(Locale.ROOT);
        String remoteType = remote == null ? "BASIC" : remote.trim().toUpperCase(Locale.ROOT);
        String act = action == null ? "ON" : action.trim().toUpperCase(Locale.ROOT);

        com.smarthome.pattern.structural.bridge.DeviceImplementor bridgeDevice = switch (deviceType) {
            case "LIGHT" -> new com.smarthome.pattern.structural.bridge.LightDevice();
            case "THERMOSTAT", "HVAC" -> new com.smarthome.pattern.structural.bridge.ThermostatDevice();
            case "RADIO" -> new RadioDevice();
            case "TV" -> new TVDevice();
            default -> new TVDevice();
        };

        com.smarthome.pattern.structural.bridge.RemoteControl remoteControl = switch (remoteType) {
            case "ADVANCED", "PRO" -> new AdvancedRemote(bridgeDevice);
            default -> new BasicRemote(bridgeDevice);
        };

        switch (act) {
            case "OFF" -> bridgeDevice.disable();
            case "DIM", "COOL" -> remoteControl.volumeDown();
            case "BRIGHT", "HEAT" -> remoteControl.volumeUp();
            case "ON" -> bridgeDevice.enable();
            default -> remoteControl.togglePower();
        }

        return Map.of(
                "pattern", "Bridge",
                "remote", remoteType,
                "device", deviceType,
                "action", act,
                "remoteType", remoteControl.getRemoteType(),
                "status", remoteControl.getStatus(),
                "deviceEnabled", bridgeDevice.isEnabled(),
                "volume", bridgeDevice.getVolume(),
                "channel", bridgeDevice.getChannel(),
                "timestamp", Instant.now().toString()
        );
    }

    public Map<String, Object> bridgeControl(String device, String platform) {
        String deviceType = device.toUpperCase(Locale.ROOT);
        String platformType = platform == null ? "BASIC" : platform.toUpperCase(Locale.ROOT);

        com.smarthome.pattern.structural.bridge.DeviceImplementor bridgeDevice = switch (deviceType) {
            case "TV" -> new TVDevice();
            case "RADIO" -> new RadioDevice();
            default -> new TVDevice();
        };

        com.smarthome.pattern.structural.bridge.RemoteControl remote = switch (platformType) {
            case "ADVANCED", "IOS" -> new AdvancedRemote(bridgeDevice);
            default -> new BasicRemote(bridgeDevice);
        };

        remote.togglePower();
        remote.volumeUp();

        return Map.of(
                "pattern", "Bridge",
                "device", deviceType,
                "platform", platformType,
                "remoteType", remote.getRemoteType(),
                "deviceStatus", bridgeDevice.isEnabled() ? "ON" : "OFF",
                "volume", bridgeDevice.getVolume(),
                "status", remote.getStatus(),
                "timestamp", Instant.now().toString()
        );
    }

    // ===== COMPOSITE PATTERN =====
    @Transactional
    public Map<String, Object> compositeControl(String target, String action) {
        String normalizedTarget = requireText(target, "target is required").trim();
        String normalizedAction = action == null ? "toggle" : action.trim().toLowerCase(Locale.ROOT);

        List<DeviceEntity> devices;
        if ("house".equalsIgnoreCase(normalizedTarget)) {
            devices = deviceRepository.findAll();
        } else if (normalizedTarget.toLowerCase(Locale.ROOT).startsWith("floor-")) {
            String floor = normalizedTarget.substring("floor-".length()).trim();
            List<RoomEntity> rooms = roomRepository.findAll().stream()
                    .filter(r -> r.getFloor() != null && r.getFloor().trim().equalsIgnoreCase(floor))
                    .toList();
            devices = rooms.stream()
                    .flatMap(r -> (r.getDevices() == null ? List.<DeviceEntity>of() : r.getDevices()).stream())
                    .distinct()
                    .toList();
        } else {
            // Try by room/location (e.g. living-room -> Living Room)
            String roomName = toTitleCase(normalizedTarget.replace('-', ' '));
            devices = deviceRepository.findByLocationIgnoreCase(roomName);
            if (devices.isEmpty()) {
                // Try by device id prefix (e.g. living-light -> living-light-*)
                String prefix = normalizedTarget.toLowerCase(Locale.ROOT);
                devices = deviceRepository.findAll().stream()
                        .filter(d -> d.getId() != null && d.getId().toLowerCase(Locale.ROOT).startsWith(prefix))
                        .toList();
            }
        }

        if (devices.isEmpty()) {
            return Map.of(
                    "pattern", "Composite",
                    "target", normalizedTarget,
                    "action", normalizedAction,
                    "affectedDevices", 0,
                    "message", "No matching devices found"
            );
        }

        boolean turnOn = switch (normalizedAction) {
            case "on", "true" -> true;
            case "off", "false" -> false;
            case "toggle" -> false; // computed per-device below
            default -> true;
        };

        for (DeviceEntity device : devices) {
            boolean next = normalizedAction.equals("toggle") ? !device.isOn() : turnOn;
            device.setOn(next);
            Device runtime = ensureRuntimeDevice(device);
            if (next) {
                runtime.turnOn();
            } else {
                runtime.turnOff();
            }
        }
        deviceRepository.saveAll(devices);

        List<DeviceView> views = devices.stream().map(this::toView).toList();
        views.forEach(v -> broadcastUpdate("device", v));

        return Map.of(
                "pattern", "Composite",
                "target", normalizedTarget,
                "action", normalizedAction,
                "affectedDevices", views.size(),
                "devices", views,
                "timestamp", Instant.now().toString()
        );
    }

    public Map<String, Object> compositeAction(String group, String action) {
        boolean turnOn = action == null || action.equalsIgnoreCase("on") || action.equalsIgnoreCase("true");
        List<DeviceView> affected = controlRoom(group, turnOn);

        return Map.of(
                "pattern", "Composite",
                "group", group,
                "action", turnOn ? "ON" : "OFF",
                "affectedDevices", affected.size(),
                "devices", affected,
                "timestamp", Instant.now().toString()
        );
    }

    private static String toTitleCase(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isBlank()) {
            return raw;
        }
        String[] parts = raw.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                out.append(part.substring(1));
            }
        }
        return out.toString();
    }

    // ===== PROTOTYPE PATTERN =====
    public Map<String, Object> prototypeClone(String template) {
        ConfigurationPrototypeRegistry registry = new ConfigurationPrototypeRegistry();

        // Register some default templates
        DeviceConfiguration ambientLight = new DeviceConfiguration("ambient-light");
        ambientLight.setProperty("brightness", 70);
        ambientLight.setProperty("colorTemp", 2700);
        ambientLight.setProperty("schedule", "sunset");
        registry.registerPrototype("ambient-light", ambientLight);

        DeviceConfiguration securityCam = new DeviceConfiguration("security-camera");
        securityCam.setProperty("resolution", "4K");
        securityCam.setProperty("nightVision", true);
        securityCam.setProperty("motionAlerts", true);
        registry.registerPrototype("security-camera", securityCam);

        DeviceConfiguration ecoThermo = new DeviceConfiguration("eco-thermostat");
        ecoThermo.setProperty("mode", "eco");
        ecoThermo.setProperty("dayTemp", 21);
        ecoThermo.setProperty("nightTemp", 18);
        registry.registerPrototype("eco-thermostat", ecoThermo);

        // Clone the requested template
        String templateKey = template != null ? template.toLowerCase(Locale.ROOT) : "ambient-light";
        DeviceConfiguration cloned = registry.getClone(templateKey);

        if (cloned == null) {
            return Map.of(
                    "pattern", "Prototype",
                    "error", "Template not found: " + templateKey,
                    "availableTemplates", List.of("ambient-light", "security-camera", "eco-thermostat")
            );
        }

        // Give the clone a unique ID
        String cloneId = templateKey + "-clone-" + UUID.randomUUID().toString().substring(0, 8);
        cloned.setProperty("cloneId", cloneId);
        cloned.setProperty("clonedAt", Instant.now().toString());

        return Map.of(
                "pattern", "Prototype",
                "action", "clone",
                "sourceTemplate", templateKey,
                "cloneId", cloneId,
                "properties", cloned.getAllProperties(),
                "timestamp", Instant.now().toString()
        );
    }
}
