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
import com.smarthome.pattern.behavioral.observer.ObserverDemo;
import com.smarthome.pattern.behavioral.state.StateDemo;
import com.smarthome.pattern.behavioral.strategy.StrategyDemo;
import com.smarthome.pattern.behavioral.templatemethod.TemplateMethodDemo;
import com.smarthome.pattern.behavioral.visitor.VisitorDemo;
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

@Service
public class SmartHomeService {
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final SceneRepository sceneRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final ObjectMapper objectMapper;
    private final HomeController homeController = HomeController.INSTANCE;

    private final Map<String, Map<String, Boolean>> savedScenes = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> observersByDevice = new ConcurrentHashMap<>();
    private final List<AutomationRule> rules = new ArrayList<>();

    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(\\s*(.*?)\\s*\\)\\s*$");

    public SmartHomeService(
            DeviceRepository deviceRepository,
            RoomRepository roomRepository,
            SceneRepository sceneRepository,
            AutomationRuleRepository automationRuleRepository,
            ObjectMapper objectMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.sceneRepository = sceneRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.objectMapper = objectMapper;
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

        return toView(device);
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
        return devices.stream().map(this::toView).toList();
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

    public Map<String, Object> saveMemento(String sceneName) {
        return MementoDemo.saveAndRestore(sceneName);
    }

    public Map<String, Object> registerObserver(String deviceId, String observerType) {
        Map<String, Object> demo = ObserverDemo.demo();
        return Map.of(
                "pattern", "Observer",
                "requestedDeviceId", deviceId,
                "requestedObserverType", observerType,
                "demo", demo
        );
    }

    public Map<String, Object> applyEnergyStrategy(String strategy) {
        List<Device> devices = new ArrayList<>(homeController.getDevicesSnapshot().values());
        List<String> before = devices.stream().map(Device::getStatus).toList();

        Map<String, Object> result = StrategyDemo.apply(strategy, devices);

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

    public Map<String, Object> chainAlert(String deviceId, String level, String message) {
        return ChainDemo.process(deviceId, level, message);
    }

    @Transactional
    public Map<String, Object> commandExecute(String deviceId, String command) {
        DeviceEntity entity = getDeviceEntityOrThrow(deviceId);
        Device runtime = ensureRuntimeDevice(entity);
        Map<String, Object> result = CommandDemo.execute(runtime, command);
        entity.setOn(runtime.isOn());
        deviceRepository.save(entity);
        return result;
    }

    public Map<String, Object> stateDemo() {
        return StateDemo.demo();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> visitorAudit(String type) {
        return VisitorDemo.audit(type);
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
        List<String> normalized = List.of(Optional.ofNullable(decorators).orElse("").split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        DeviceEntity entity = getDeviceEntityOrThrow(deviceId);
        Device runtime = ensureRuntimeDevice(entity);

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

        return Map.of(
                "pattern", "Decorator",
                "decorators", normalized,
                "deviceInfo", decorated.getDeviceInfo(),
                "status1", status1,
                "status2", status2,
                "securityAuthenticated", security != null && security.isAuthenticated(),
                "cacheStats", caching != null ? caching.getCacheStats() : null
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
        String status3 = proxy.getStatus(); // cached

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
        return TemplateMethodDemo.run(deviceType);
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
}
