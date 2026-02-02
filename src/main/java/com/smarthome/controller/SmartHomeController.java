package com.smarthome.controller;

import com.smarthome.domain.DeviceType;
import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.creational.builder.AutomationRule;
import com.smarthome.service.SmartHomeService;
import com.smarthome.web.viewmodel.AutomationRuleView;
import com.smarthome.web.viewmodel.DeviceView;
import com.smarthome.web.viewmodel.RoomView;
import com.smarthome.web.viewmodel.SceneView;
import com.smarthome.web.viewmodel.StatusView;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SmartHomeController {
    private final SmartHomeService smartHomeService;

    public SmartHomeController(SmartHomeService smartHomeService) {
        this.smartHomeService = smartHomeService;
    }

    @GetMapping("/status")
    public StatusView status() {
        return smartHomeService.getStatus();
    }

    @PostMapping("/mode/{mode}")
    public StatusView setMode(@PathVariable HomeMode mode) {
        smartHomeService.setHomeMode(mode);
        return smartHomeService.getStatus();
    }

    @GetMapping("/devices")
    public List<DeviceView> devices() {
        return smartHomeService.getDevices();
    }

    @GetMapping("/devices/{id}")
    public DeviceView device(@PathVariable String id) {
        return smartHomeService.getDevice(id);
    }

    @PostMapping("/devices/{id}/control")
    public DeviceView controlDevice(@PathVariable String id, @RequestParam String action) {
        boolean turnOn = "on".equalsIgnoreCase(action) || "true".equalsIgnoreCase(action);
        return smartHomeService.controlDevice(id, turnOn);
    }

    @GetMapping("/patterns")
    public Map<String, Object> patterns() {
        return smartHomeService.listPatterns();
    }

    @PostMapping("/patterns/factory/create")
    public DeviceView factoryCreate(
            @RequestParam DeviceType type,
            @RequestParam String name,
            @RequestParam String location
    ) {
        return smartHomeService.createDeviceViaFactory(type, name, location);
    }

    @PostMapping("/patterns/abstract-factory/create")
    public List<DeviceView> abstractFactoryCreate(
            @RequestParam String ecosystem,
            @RequestParam String location
    ) {
        return smartHomeService.createDevicesViaAbstractFactory(ecosystem, location);
    }

    @PostMapping("/patterns/builder/rule")
    public AutomationRule builderRule(
            @RequestParam String name,
            @RequestParam String trigger,
            @RequestParam String condition,
            @RequestParam String action
    ) {
        return smartHomeService.buildAutomationRule(name, trigger, condition, action);
    }

    @GetMapping("/patterns/prototype/templates")
    public List<Map<String, Object>> prototypeTemplates() {
        return smartHomeService.listPrototypeTemplates();
    }

    @PostMapping("/patterns/adapter/legacy")
    public Map<String, Object> adapterLegacy(
            @RequestParam String name,
            @RequestParam String location
    ) {
        return smartHomeService.adapterDemo(name, location);
    }

    @GetMapping("/patterns/bridge/demo")
    public Map<String, Object> bridgeDemo() {
        return smartHomeService.bridgeDemo();
    }

    @GetMapping("/patterns/composite/rooms")
    public Map<String, Object> compositeRooms() {
        return smartHomeService.roomsComposite();
    }

    @PostMapping("/patterns/composite/rooms/{room}/control")
    public List<DeviceView> compositeRoomControl(
            @PathVariable String room,
            @RequestParam String action
    ) {
        boolean turnOn = "on".equalsIgnoreCase(action) || "true".equalsIgnoreCase(action);
        return smartHomeService.controlRoom(room, turnOn);
    }

    @PostMapping("/patterns/decorator/wrap")
    public Map<String, Object> decoratorWrap(
            @RequestParam String deviceId,
            @RequestParam(required = false) String decorators
    ) {
        return smartHomeService.decoratorWrap(deviceId, decorators);
    }

    @PostMapping("/patterns/facade/scene/{name}")
    public Map<String, Object> facadeScene(@PathVariable String name) {
        return smartHomeService.activateScene(name);
    }

    @PostMapping("/patterns/proxy/remote")
    public Map<String, Object> proxyRemote(
            @RequestParam String name,
            @RequestParam String address
    ) {
        return smartHomeService.proxyRemote(name, address);
    }

    @PostMapping("/patterns/chain/alert")
    public Map<String, Object> chainAlert(
            @RequestParam String deviceId,
            @RequestParam String level,
            @RequestParam String message
    ) {
        return smartHomeService.chainAlert(deviceId, level, message);
    }

    @PostMapping("/patterns/command/execute")
    public Map<String, Object> commandExecute(
            @RequestParam String deviceId,
            @RequestParam String command
    ) {
        return smartHomeService.commandExecute(deviceId, command);
    }

    @PostMapping("/patterns/command/undo")
    public Map<String, Object> commandUndo() {
        return smartHomeService.commandUndo();
    }

    @PostMapping("/patterns/command/redo")
    public Map<String, Object> commandRedo() {
        return smartHomeService.commandRedo();
    }

    @PostMapping("/patterns/memento/save")
    public Map<String, Object> mementoSave(@RequestParam String sceneName) {
        return smartHomeService.saveMemento(sceneName);
    }

    @GetMapping("/patterns/memento/list")
    public Map<String, Object> mementoList() {
        return smartHomeService.listMementos();
    }

    @PostMapping("/patterns/memento/restore")
    public Map<String, Object> mementoRestore(@RequestParam String sceneName) {
        return smartHomeService.restoreMemento(sceneName);
    }

    @PostMapping("/patterns/observer/register")
    public Map<String, Object> observerRegister(
            @RequestParam String deviceId,
            @RequestParam String observerType
    ) {
        return smartHomeService.registerObserver(deviceId, observerType);
    }

    @PostMapping("/patterns/observer/subscribe")
    public Map<String, Object> observerSubscribe(
            @RequestParam String deviceId,
            @RequestParam String observerType
    ) {
        return smartHomeService.observerSubscribe(deviceId, observerType);
    }

    @PostMapping("/patterns/observer/trigger")
    public Map<String, Object> observerTrigger(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "MOTION") String eventType
    ) {
        return smartHomeService.observerTrigger(deviceId, eventType);
    }

    @GetMapping("/patterns/flyweight/demo")
    public Map<String, Object> flyweightDemo() {
        return smartHomeService.flyweightDemo();
    }

    @PostMapping("/patterns/interpreter/evaluate")
    public Map<String, Object> interpreterEvaluate(
            @RequestParam(required = false) String rule,
            @RequestParam Map<String, String> params
    ) {
        return smartHomeService.interpreterEvaluate(rule, parseVariables(params, Set.of("rule")));
    }

    @GetMapping("/patterns/iterator/demo")
    public Map<String, Object> iteratorDemo(
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterValue
    ) {
        return smartHomeService.iteratorDemo(filterType, filterValue);
    }

    @GetMapping("/patterns/mediator/demo")
    public Map<String, Object> mediatorDemo() {
        return smartHomeService.mediatorDemo();
    }

    @PostMapping("/patterns/mediator/notify")
    public Map<String, Object> mediatorNotify(
            @RequestParam(defaultValue = "sensor-1") String sourceDeviceId,
            @RequestParam(defaultValue = "MOTION_DETECTED") String event
    ) {
        return smartHomeService.mediatorNotify(sourceDeviceId, event);
    }

    @GetMapping("/patterns/template/demo")
    public Map<String, Object> templateDemo(@RequestParam(required = false) String deviceType) {
        return smartHomeService.templateDemo(deviceType);
    }

    @PostMapping("/patterns/template/init")
    public Map<String, Object> templateInit(
            @RequestParam(defaultValue = "LIGHT") String deviceType,
            @RequestParam(required = false) String deviceId
    ) {
        return smartHomeService.templateInit(deviceType, deviceId);
    }

    @GetMapping("/patterns/state/demo")
    public Map<String, Object> stateDemo() {
        return smartHomeService.stateDemo();
    }

    @PostMapping("/patterns/strategy/apply")
    public Map<String, Object> strategyApply(@RequestParam String strategy) {
        return smartHomeService.applyEnergyStrategy(strategy);
    }

    @GetMapping("/patterns/visitor/audit")
    public Map<String, Object> visitorAudit(@RequestParam(required = false) String type) {
        return smartHomeService.visitorAudit(type);
    }

    @GetMapping("/rooms")
    public List<RoomView> rooms() {
        return smartHomeService.getRoomViews();
    }

    @PostMapping("/rooms/create")
    public RoomView createRoom(
            @RequestParam String name,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String roomType
    ) {
        return smartHomeService.createRoom(name, floor, roomType);
    }

    @PostMapping("/rooms/{roomId}/assign")
    public RoomView assignDeviceToRoom(
            @PathVariable Long roomId,
            @RequestParam String deviceId
    ) {
        return smartHomeService.assignDeviceToRoom(roomId, deviceId);
    }

    @PostMapping("/rooms/{roomId}/unassign")
    public RoomView unassignDeviceFromRoom(
            @PathVariable Long roomId,
            @RequestParam String deviceId
    ) {
        return smartHomeService.unassignDeviceFromRoom(roomId, deviceId);
    }

    @GetMapping("/scenes")
    public List<SceneView> scenes() {
        return smartHomeService.getScenes();
    }

    @PostMapping("/scenes/create")
    public SceneView createScene(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean favorite
    ) {
        return smartHomeService.createSceneSnapshot(name, description, favorite);
    }

    @PostMapping("/scenes/{sceneId}/apply")
    public Map<String, Object> applyScene(@PathVariable Long sceneId) {
        return smartHomeService.applySceneSnapshot(sceneId);
    }

    @PostMapping("/scenes/{sceneId}/favorite")
    public SceneView toggleSceneFavorite(@PathVariable Long sceneId) {
        return smartHomeService.toggleSceneFavorite(sceneId);
    }

    @PostMapping("/scenes/{sceneId}/delete")
    public Map<String, Object> deleteScene(@PathVariable Long sceneId) {
        return smartHomeService.deleteScene(sceneId);
    }

    @GetMapping("/rules")
    public List<AutomationRuleView> rules() {
        return smartHomeService.getAutomationRules();
    }

    @PostMapping("/rules/create")
    public AutomationRuleView createRule(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String triggerCondition,
            @RequestParam String actionScript,
            @RequestParam(required = false) Integer priority
    ) {
        return smartHomeService.createAutomationRule(name, description, triggerCondition, actionScript, priority);
    }

    @PostMapping("/rules/{ruleId}/toggle")
    public AutomationRuleView toggleRule(@PathVariable Long ruleId) {
        return smartHomeService.toggleAutomationRule(ruleId);
    }

    @PostMapping("/rules/{ruleId}/delete")
    public Map<String, Object> deleteRule(@PathVariable Long ruleId) {
        return smartHomeService.deleteAutomationRule(ruleId);
    }

    @PostMapping("/rules/{ruleId}/run")
    public Map<String, Object> runRule(
            @PathVariable Long ruleId,
            @RequestParam(defaultValue = "false") boolean executeActions,
            @RequestParam(required = false) String vars,
            @RequestParam Map<String, String> params
    ) {
        Map<String, Object> variables = parseVariables(params, Set.of("executeActions", "vars"));
        variables.putAll(parseVariablesText(vars));

        return smartHomeService.runAutomationRule(
                ruleId,
                variables,
                executeActions
        );
    }

    private static Map<String, Object> parseVariablesText(String vars) {
        Map<String, Object> variables = new LinkedHashMap<>();
        if (vars == null || vars.isBlank()) {
            return variables;
        }

        String[] lines = vars.split("[\\n\\r;]+");
        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            int eq = line.indexOf('=');
            int colon = line.indexOf(':');
            int sep = eq >= 0 ? eq : colon;
            if (sep <= 0) {
                continue;
            }

            String key = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();
            if (key.isBlank() || value.isBlank()) {
                continue;
            }

            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                variables.put(key, Boolean.parseBoolean(value));
                continue;
            }
            try {
                variables.put(key, Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                variables.put(key, value);
            }
        }

        return variables;
    }

    private static Map<String, Object> parseVariables(Map<String, String> params, Set<String> ignoreKeys) {
        Map<String, Object> variables = new LinkedHashMap<>();
        if (params == null || params.isEmpty()) {
            return variables;
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (ignoreKeys != null && ignoreKeys.contains(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                continue;
            }

            String trimmed = value.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                variables.put(key, Boolean.parseBoolean(trimmed));
                continue;
            }
            try {
                variables.put(key, Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                variables.put(key, trimmed);
            }
        }
        return variables;
    }
}
