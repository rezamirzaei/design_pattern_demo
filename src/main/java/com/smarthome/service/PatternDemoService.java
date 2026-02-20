package com.smarthome.service;

import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.DeviceType;
import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.behavioral.chain.ChainDemo;
import com.smarthome.pattern.behavioral.command.CommandDemo;
import com.smarthome.pattern.behavioral.interpreter.InterpreterDemo;
import com.smarthome.pattern.behavioral.iterator.IteratorDemo;
import com.smarthome.pattern.behavioral.mediator.MediatorDemo;
import com.smarthome.pattern.behavioral.memento.MementoDemo;
import com.smarthome.pattern.creational.factory.*;
import com.smarthome.pattern.creational.prototype.ConfigurationPrototypeRegistry;
import com.smarthome.pattern.creational.prototype.DeviceConfiguration;
import com.smarthome.pattern.creational.singleton.HomeController;
import com.smarthome.pattern.structural.adapter.LegacyThermostat;
import com.smarthome.pattern.structural.adapter.LegacyThermostatAdapter;
import com.smarthome.pattern.structural.bridge.*;
import com.smarthome.pattern.structural.composite.DeviceGroup;
import com.smarthome.pattern.structural.composite.SingleDevice;
import com.smarthome.pattern.structural.decorator.CachingDecorator;
import com.smarthome.pattern.structural.decorator.LoggingDecorator;
import com.smarthome.pattern.structural.decorator.SecurityDecorator;
import com.smarthome.pattern.structural.facade.SmartHomeFacade;
import com.smarthome.pattern.structural.flyweight.FlyweightDemo;
import com.smarthome.pattern.structural.proxy.DeviceProxy;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.RoomRepository;
import com.smarthome.web.viewmodel.DeviceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Encapsulates all 23 GoF pattern demonstrations.
 * Stateless where possible; uses thread-safe state for Command/Observer/State demos.
 */
@Service
public class PatternDemoService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final DeviceService deviceService;
    private final HomeController homeController = HomeController.INSTANCE;

    // ── Thread-safe demo state ────────────────────────────────
    private final Map<String, String> lastCommandByDevice = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> observersByDevice = new ConcurrentHashMap<>();
    private final AtomicReference<String> playerState = new AtomicReference<>("STOPPED");

    public PatternDemoService(DeviceRepository deviceRepository,
                              RoomRepository roomRepository,
                              DeviceService deviceService) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.deviceService = deviceService;
    }

    // ═══════════════════════════════════════════════════════════
    //  CREATIONAL
    // ═══════════════════════════════════════════════════════════

    public Map<String, Object> listPatterns() {
        return Map.of(
                "creational", List.of("singleton", "factory", "abstract-factory", "builder", "prototype"),
                "structural", List.of("adapter", "bridge", "composite", "decorator", "facade", "flyweight", "proxy"),
                "behavioral", List.of("chain", "command", "interpreter", "iterator", "mediator", "memento", "observer", "state", "strategy", "template", "visitor"));
    }

    public List<Map<String, Object>> listPrototypeTemplates() {
        List<String> keys = new ArrayList<>();
        ConfigurationPrototypeRegistry.getAvailablePrototypes().forEach(keys::add);
        keys.sort(String::compareToIgnoreCase);
        List<Map<String, Object>> out = new ArrayList<>();
        for (String key : keys) {
            DeviceConfiguration c = ConfigurationPrototypeRegistry.getClone(key);
            out.add(Map.of("prototypeKey", key, "presetName", c.getPresetName(),
                    "deviceType", c.getDeviceType(), "settings", c.getSettings()));
        }
        return out;
    }

    public Map<String, Object> prototypeClone(String template) {
        var registry = new ConfigurationPrototypeRegistry();
        var a = new DeviceConfiguration("ambient-light"); a.setProperty("brightness", 70); a.setProperty("colorTemp", 2700); a.setProperty("schedule", "sunset");
        registry.registerPrototype("ambient-light", a);
        var s = new DeviceConfiguration("security-camera"); s.setProperty("resolution", "4K"); s.setProperty("nightVision", true); s.setProperty("motionAlerts", true);
        registry.registerPrototype("security-camera", s);
        var e = new DeviceConfiguration("eco-thermostat"); e.setProperty("mode", "eco"); e.setProperty("dayTemp", 21); e.setProperty("nightTemp", 18);
        registry.registerPrototype("eco-thermostat", e);

        String key = template != null ? template.toLowerCase(Locale.ROOT) : "ambient-light";
        DeviceConfiguration cloned = registry.getClone(key);
        if (cloned == null) return Map.of("pattern", "Prototype", "error", "Template not found: " + key,
                "availableTemplates", List.of("ambient-light", "security-camera", "eco-thermostat"));

        String cloneId = key + "-clone-" + UUID.randomUUID().toString().substring(0, 8);
        cloned.setProperty("cloneId", cloneId); cloned.setProperty("clonedAt", Instant.now().toString());
        return Map.of("pattern", "Prototype", "action", "clone", "sourceTemplate", key,
                "cloneId", cloneId, "properties", cloned.getAllProperties(), "timestamp", Instant.now().toString());
    }

    // ═══════════════════════════════════════════════════════════
    //  STRUCTURAL
    // ═══════════════════════════════════════════════════════════

    public Map<String, Object> adapterDemo(String name, String location) {
        LegacyThermostat legacy = new LegacyThermostat();
        legacy.setPower(true); legacy.setTemperatureFahrenheit(72); legacy.setMode("heat");
        LegacyThermostatAdapter adapter = new LegacyThermostatAdapter(legacy, name, location);
        adapter.turnOn(); adapter.operate("TEMPERATURE:21"); adapter.operate("MODE:auto");
        return Map.of("pattern", "Adapter", "legacyStatus", legacy.getLegacyStatus(),
                "adaptedStatus", adapter.getStatus(), "adaptedInfo", adapter.getDeviceInfo());
    }

    public Map<String, Object> bridgeDemo() {
        TVDevice tv = new TVDevice(); RadioDevice radio = new RadioDevice();
        BasicRemote tvR = new BasicRemote(tv); AdvancedRemote radioR = new AdvancedRemote(radio);
        tvR.togglePower(); tvR.volumeUp(); tvR.channelUp();
        radioR.togglePower(); radioR.setVolume(15); radioR.voiceCommand("mute"); radioR.voiceCommand("channel 101"); radioR.voiceCommand("mute");
        return Map.of("pattern", "Bridge", "tv", tvR.getStatus(), "radio", radioR.getStatus());
    }

    public Map<String, Object> bridgeControl(String remote, String device, String action) {
        String dt = device == null ? "TV" : device.trim().toUpperCase(Locale.ROOT);
        String rt = remote == null ? "BASIC" : remote.trim().toUpperCase(Locale.ROOT);
        String act = action == null ? "ON" : action.trim().toUpperCase(Locale.ROOT);
        DeviceImplementor bd = switch (dt) { case "RADIO" -> new RadioDevice(); default -> new TVDevice(); };
        RemoteControl rc = switch (rt) { case "ADVANCED", "PRO" -> new AdvancedRemote(bd); default -> new BasicRemote(bd); };
        switch (act) { case "OFF" -> bd.disable(); case "ON" -> bd.enable(); default -> rc.togglePower(); }
        return Map.of("pattern", "Bridge", "remote", rt, "device", dt, "action", act,
                "status", rc.getStatus(), "deviceEnabled", bd.isEnabled(), "timestamp", Instant.now().toString());
    }

    public Map<String, Object> bridgeControl(String device, String platform) {
        String dt = device.toUpperCase(Locale.ROOT);
        String pt = platform == null ? "BASIC" : platform.toUpperCase(Locale.ROOT);
        DeviceImplementor bd = switch (dt) { case "RADIO" -> new RadioDevice(); default -> new TVDevice(); };
        RemoteControl rc = switch (pt) { case "ADVANCED", "IOS" -> new AdvancedRemote(bd); default -> new BasicRemote(bd); };
        rc.togglePower(); rc.volumeUp();
        return Map.of("pattern", "Bridge", "device", dt, "platform", pt,
                "status", rc.getStatus(), "timestamp", Instant.now().toString());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> roomsComposite() {
        DeviceGroup home = new DeviceGroup("Home");
        Map<String, DeviceGroup> rooms = new LinkedHashMap<>();
        for (DeviceEntity entity : deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getLocation).thenComparing(DeviceEntity::getName)).toList()) {
            String room = entity.getLocation() == null ? "Unknown" : entity.getLocation();
            DeviceGroup group = rooms.computeIfAbsent(room, DeviceGroup::new);
            group.add(new SingleDevice(entity.getName(), deviceService.ensureRuntimeDevice(entity)));
        }
        rooms.values().forEach(home::add);
        return Map.of("pattern", "Composite", "deviceCount", home.getDeviceCount(),
                "estimatedPower", home.getPowerConsumption(), "status", home.getStatus());
    }

    @Transactional
    public Map<String, Object> compositeControl(String target, String action) {
        String t = ServiceUtils.requireText(target, "target is required");
        String act = action == null ? "toggle" : action.trim().toLowerCase(Locale.ROOT);
        List<DeviceEntity> devices;
        if ("house".equalsIgnoreCase(t)) { devices = deviceRepository.findAll(); }
        else {
            String roomName = ServiceUtils.toTitleCase(t.replace('-', ' '));
            devices = deviceRepository.findByLocationIgnoreCase(roomName);
            if (devices.isEmpty()) {
                String prefix = t.toLowerCase(Locale.ROOT);
                devices = deviceRepository.findAll().stream().filter(d -> d.getId() != null && d.getId().toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
            }
        }
        if (devices.isEmpty()) return Map.of("pattern", "Composite", "target", t, "affectedDevices", 0, "message", "No matching devices");
        boolean turnOn = switch (act) { case "on", "true" -> true; case "off", "false" -> false; default -> true; };
        for (DeviceEntity d : devices) {
            boolean next = act.equals("toggle") ? !d.isOn() : turnOn;
            d.setOn(next);
            Device rt = deviceService.ensureRuntimeDevice(d);
            if (next) rt.turnOn(); else rt.turnOff();
        }
        deviceRepository.saveAll(devices);
        List<DeviceView> views = devices.stream().map(deviceService::toView).toList();
        views.forEach(v -> deviceService.broadcast("device", v));
        return Map.of("pattern", "Composite", "target", t, "action", act,
                "affectedDevices", views.size(), "devices", views, "timestamp", Instant.now().toString());
    }

    public Map<String, Object> compositeAction(String group, String action) {
        boolean turnOn = action == null || action.equalsIgnoreCase("on");
        List<DeviceView> affected = deviceService.controlRoom(group, turnOn);
        return Map.of("pattern", "Composite", "group", group, "action", turnOn ? "ON" : "OFF",
                "affectedDevices", affected.size(), "devices", affected, "timestamp", Instant.now().toString());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> decoratorWrap(String deviceId, String decorators) {
        DeviceEntity entity = deviceService.findOrThrow(deviceId);
        Device runtime = deviceService.ensureRuntimeDevice(entity);
        List<String> decs = List.of(Optional.ofNullable(decorators).orElse("").split(","));
        Device decorated = runtime; SecurityDecorator sec = null; CachingDecorator cache = null;
        for (String d : decs) { switch (d) {
            case "LOGGING" -> decorated = new LoggingDecorator(decorated);
            case "SECURITY" -> { sec = new SecurityDecorator(decorated); sec.authenticate("demo-user", "pass1234"); decorated = sec; }
            case "CACHING" -> { cache = new CachingDecorator(decorated, Duration.ofSeconds(15)); decorated = cache; }
            default -> {} } }
        String s1 = decorated.getStatus(); String s2 = decorated.getStatus();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pattern", "Decorator"); out.put("decorators", decs); out.put("deviceInfo", decorated.getDeviceInfo());
        out.put("status1", s1); out.put("status2", s2);
        out.put("securityAuthenticated", sec != null && sec.isAuthenticated());
        out.put("cacheStats", cache != null ? cache.getCacheStats() : null);
        return out;
    }

    @Transactional
    public Map<String, Object> activateScene(String sceneName) {
        SmartHomeFacade facade = new SmartHomeFacade();
        for (Device d : homeController.getDevicesSnapshot().values()) {
            if (d instanceof SmartLight l) facade.addLight(l);
            else if (d instanceof SmartCamera c) facade.addCamera(c);
            else if (d instanceof SmartLock l) facade.addLock(l);
            else if (d instanceof SmartThermostat t) facade.addThermostat(t);
        }
        String n = Optional.ofNullable(sceneName).orElse("morning").toLowerCase(Locale.ROOT);
        switch (n) {
            case "morning" -> { facade.goodMorning(); deviceService.setHomeMode(HomeMode.NORMAL); }
            case "night" -> { facade.goodNight(); deviceService.setHomeMode(HomeMode.NIGHT); }
            case "leave" -> { facade.leaveHome(); deviceService.setHomeMode(HomeMode.AWAY); }
            case "arrive" -> { facade.arriveHome(); deviceService.setHomeMode(HomeMode.NORMAL); }
            case "movie" -> { facade.movieNight(); deviceService.setHomeMode(HomeMode.NIGHT); }
            case "party" -> { facade.partyMode(); deviceService.setHomeMode(HomeMode.NORMAL); }
            default -> { facade.goodMorning(); deviceService.setHomeMode(HomeMode.NORMAL); }
        }
        Map<String, Boolean> changes = deviceService.syncDatabaseFromRuntime();
        return Map.of("pattern", "Facade", "scene", n, "homeStatus", facade.getHomeStatus(),
                "mode", deviceService.getHomeMode(), "changedDevices", changes, "timestamp", Instant.now().toString());
    }

    public Map<String, Object> flyweightDemo(Integer count) {
        return FlyweightDemo.demo(count != null && count > 0 ? count : 100);
    }

    public Map<String, Object> flyweightStats(Integer instances) {
        return FlyweightDemo.stats(instances != null && instances > 0 ? instances : 10_000);
    }

    public Map<String, Object> proxyRemote(String name, String address) {
        String id = "proxy-" + UUID.randomUUID().toString().substring(0, 8);
        DeviceProxy proxy = new DeviceProxy(id, name, address);
        boolean b1 = proxy.isInitialized(); String s1 = proxy.getStatus(); boolean b2 = proxy.isInitialized();
        proxy.setAccess("demo-admin", DeviceProxy.AccessLevel.ADMIN); proxy.turnOn();
        String s2 = proxy.getStatus(); String s3 = proxy.getStatus();
        return Map.of("pattern", "Proxy", "deviceId", id, "address", address,
                "initializedBefore", b1, "initializedAfter", b2,
                "status1", s1, "status2", s2, "status3", s3, "connected", proxy.isConnected());
    }

    // ═══════════════════════════════════════════════════════════
    //  BEHAVIORAL
    // ═══════════════════════════════════════════════════════════

    public Map<String, Object> chainAlert(String deviceId, String level, String message) {
        return ChainDemo.process(deviceId, level, message);
    }

    @Transactional
    public Map<String, Object> commandExecute(String deviceId, String command) {
        DeviceEntity entity = deviceService.findOrThrow(deviceId);
        Device runtime = deviceService.ensureRuntimeDevice(entity);
        String effective = normalizeCommand(runtime, command);
        Map<String, Object> result = new LinkedHashMap<>(CommandDemo.execute(runtime, effective));
        result.put("requestedCommand", command); result.put("effectiveCommand", effective);
        entity.setOn(runtime.isOn()); deviceRepository.save(entity);
        lastCommandByDevice.put(deviceId, effective);
        return result;
    }

    public Map<String, Object> commandUndo() {
        if (lastCommandByDevice.isEmpty()) return Map.of("pattern", "Command", "message", "Nothing to undo");
        var entry = lastCommandByDevice.entrySet().iterator().next();
        String undo = reverseCommand(entry.getValue());
        var exec = commandExecute(entry.getKey(), undo);
        return Map.of("pattern", "Command", "action", "undo", "deviceId", entry.getKey(), "undoCommand", undo, "result", exec);
    }

    public Map<String, Object> commandRedo() {
        if (lastCommandByDevice.isEmpty()) return Map.of("pattern", "Command", "message", "Nothing to redo");
        var entry = lastCommandByDevice.entrySet().iterator().next();
        var exec = commandExecute(entry.getKey(), entry.getValue());
        return Map.of("pattern", "Command", "action", "redo", "deviceId", entry.getKey(), "command", entry.getValue(), "result", exec);
    }

    public Map<String, Object> interpreterEvaluate(String rule, Map<String, Object> variables) {
        return InterpreterDemo.evaluate(rule, variables);
    }

    public Map<String, Object> interpreterEvaluate(String rule) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("motion_detected", true); vars.put("is_dark", true);
        vars.put("door_open", false); vars.put("temp_high", false);
        vars.put("hour", java.time.LocalTime.now().getHour());
        return InterpreterDemo.evaluate(rule, vars);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> iteratorIterate(String type, String filter) {
        List<IteratorDemo.DeviceSeed> seeds = deviceRepository.findAll().stream()
                .map(e -> new IteratorDemo.DeviceSeed(deviceService.ensureRuntimeDevice(e),
                        e.getLocation() == null ? "Unknown" : e.getLocation(),
                        e.getType() == null ? "UNKNOWN" : e.getType().name()))
                .toList();
        return IteratorDemo.iterate(seeds, type, filter);
    }

    public Map<String, Object> mediatorDemo() { return MediatorDemo.motionScenario(); }

    public Map<String, Object> mediatorNotify(String sourceDeviceId, String event) {
        return Map.of("pattern", "Mediator", "sourceDeviceId", sourceDeviceId, "event", event, "demo", mediatorDemo());
    }

    public Map<String, Object> mementoSave(String sceneName) { return MementoDemo.saveAndRestore(sceneName); }

    public Map<String, Object> observerSubscribe(String deviceId, String observerType) {
        String id = ServiceUtils.requireText(deviceId, "deviceId is required");
        String type = (observerType == null ? "MOBILE" : observerType.trim().toUpperCase(Locale.ROOT));
        observersByDevice.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(type);
        var response = Map.of("pattern", "Observer", "action", "subscribe",
                "deviceId", id, "observerType", type, "subscribers", observersByDevice.getOrDefault(id, Set.of()));
        deviceService.broadcast("observer", response);
        return response;
    }

    public Map<String, Object> observerTrigger(String deviceId, String eventType) {
        String id = ServiceUtils.requireText(deviceId, "deviceId is required");
        String event = (eventType == null ? "MOTION" : eventType.trim().toUpperCase(Locale.ROOT));
        Set<String> subs = observersByDevice.getOrDefault(id, Set.of());
        List<String> notified = subs.stream().sorted().map(s -> s + " notified of " + event + " on " + id).toList();
        var response = Map.of("pattern", "Observer", "action", "trigger",
                "deviceId", id, "eventType", event, "subscriberCount", subs.size(), "notifications", notified);
        deviceService.broadcast("observer", response);
        return response;
    }

    public Map<String, Object> stateDemo() {
        return com.smarthome.pattern.behavioral.state.StateDemo.demo();
    }

    public Map<String, Object> stateTransition(String action) {
        String oldState = playerState.get();
        String newState = switch (action.toUpperCase(Locale.ROOT)) {
            case "PLAYPAUSE" -> oldState.equals("PLAYING") ? "PAUSED" : "PLAYING";
            case "STOP" -> "STOPPED"; case "PLAY" -> "PLAYING"; case "PAUSE" -> "PAUSED";
            default -> oldState;
        };
        playerState.set(newState);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pattern", "State"); response.put("action", action);
        response.put("oldState", oldState); response.put("newState", newState);
        response.put("timestamp", Instant.now().toString());
        deviceService.broadcast("state", response);
        return response;
    }

    public Map<String, Object> applyEnergyStrategy(String strategy) {
        List<Device> devices = new ArrayList<>(homeController.getDevicesSnapshot().values());
        List<String> before = devices.stream().map(Device::getStatus).toList();
        Map<String, Object> result = com.smarthome.pattern.behavioral.strategy.StrategyDemo.apply(strategy, devices);
        List<String> after = devices.stream().map(Device::getStatus).toList();
        deviceService.syncDatabaseFromRuntime();
        return Map.of("pattern", "Strategy", "before", before, "after", after, "result", result);
    }

    public Map<String, Object> templateDemo(String deviceType) {
        return com.smarthome.pattern.behavioral.templatemethod.TemplateMethodDemo.run(deviceType);
    }

    public Map<String, Object> templateInit(String deviceType) {
        return Map.of("pattern", "Template Method", "deviceType", deviceType, "demo", templateDemo(deviceType));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> visitorAudit(String type) {
        return com.smarthome.pattern.behavioral.visitor.VisitorDemo.audit(type);
    }

    // ── helpers ──────────────────────────────────────────────

    private static String normalizeCommand(Device device, String command) {
        String n = command == null ? "ON" : command.trim().toUpperCase(Locale.ROOT);
        if (n.equals("DIM") && device instanceof SmartLight) return "BRIGHTNESS:50";
        if (n.equals("TEMP_UP") && device instanceof SmartThermostat t) return "TEMPERATURE:" + (t.getTargetTemperature() + 1.0);
        if (n.equals("TEMP_DOWN") && device instanceof SmartThermostat t) return "TEMPERATURE:" + (t.getTargetTemperature() - 1.0);
        return n;
    }

    private static String reverseCommand(String command) {
        String c = command == null ? "" : command.trim().toUpperCase(Locale.ROOT);
        if (c.equals("ON")) return "OFF"; if (c.equals("OFF")) return "ON";
        return "OFF";
    }
}



