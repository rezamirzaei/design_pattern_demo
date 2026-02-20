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
import java.util.Objects;
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

    @PostMapping("/patterns/facade/activate")
    public Map<String, Object> facadeActivate(@RequestParam String scene) {
        return smartHomeService.activateScene(scene);
    }

    @PostMapping("/patterns/state/transition")
    public Map<String, Object> stateTransition(@RequestParam String action) {
        return smartHomeService.stateTransition(action);
    }

    @PostMapping("/patterns/strategy/apply")
    public Map<String, Object> strategyApply(@RequestParam String strategy) {
        return smartHomeService.applyEnergyStrategy(strategy);
    }

    @PostMapping("/patterns/template/init")
    public Map<String, Object> templateInit(@RequestParam String deviceType) {
        return smartHomeService.templateInit(deviceType);
    }

    @PostMapping("/patterns/iterator/iterate")
    public Map<String, Object> iteratorIterate(@RequestParam String type, @RequestParam(required = false) String filter) {
        return smartHomeService.iteratorIterate(type, filter);
    }

    @PostMapping("/patterns/mediator/notify")
    public Map<String, Object> mediatorNotify(@RequestParam String sourceDeviceId, @RequestParam String event) {
        return smartHomeService.mediatorNotify(sourceDeviceId, event);
    }

    @PostMapping("/patterns/interpreter/evaluate")
    public Map<String, Object> interpreterEvaluate(
            @RequestParam String rule,
            @RequestParam Map<String, String> params
    ) {
        Map<String, Object> variables = parseVariables(params, Set.of("rule"));
        if (variables.isEmpty()) {
            return smartHomeService.interpreterEvaluate(rule);
        }
        return smartHomeService.interpreterEvaluate(rule, variables);
    }

    @PostMapping("/patterns/observer/subscribe")
    public Map<String, Object> observerSubscribe(@RequestParam String deviceId, @RequestParam(required = false) String observerType) {
        return smartHomeService.observerSubscribe(deviceId, observerType);
    }

    @PostMapping("/patterns/observer/trigger")
    public Map<String, Object> observerTrigger(@RequestParam String deviceId, @RequestParam(required = false) String event) {
        return smartHomeService.observerTrigger(deviceId, event);
    }

    @GetMapping("/patterns/visitor/audit")
    public Map<String, Object> visitorAuditGet(@RequestParam String type) {
        return smartHomeService.visitorAudit(type);
    }

    @PostMapping("/patterns/visitor/audit")
    public Map<String, Object> visitorAudit(@RequestParam String type) {
        return smartHomeService.visitorAudit(type);
    }

    @GetMapping("/patterns/iterator/iterate")
    public Map<String, Object> iteratorIterateGet(@RequestParam String type, @RequestParam(required = false) String filter) {
        return smartHomeService.iteratorIterate(type, filter);
    }

    // --- GET demo endpoints used by app.js runPatternDemo() ---

    @GetMapping("/patterns/flyweight/demo")
    public Map<String, Object> flyweightDemoGet(@RequestParam(required = false) Integer count) {
        return smartHomeService.flyweightDemo(count);
    }

    @GetMapping("/patterns/iterator/demo")
    public Map<String, Object> iteratorDemoGet(
            @RequestParam(required = false, defaultValue = "ALL") String filterType,
            @RequestParam(required = false) String filterValue
    ) {
        return smartHomeService.iteratorIterate(filterType, filterValue);
    }

    @GetMapping("/patterns/mediator/demo")
    public Map<String, Object> mediatorDemoGet() {
        return smartHomeService.mediatorDemo();
    }

    @GetMapping("/patterns/state/demo")
    public Map<String, Object> stateDemoGet() {
        return smartHomeService.stateDemo();
    }

    @GetMapping("/patterns/template/demo")
    public Map<String, Object> templateDemoGet(@RequestParam(required = false, defaultValue = "LIGHT") String deviceType) {
        return smartHomeService.templateDemo(deviceType);
    }

    @PostMapping("/patterns/observer/register")
    public Map<String, Object> observerRegister(
            @RequestParam String deviceId,
            @RequestParam(required = false) String observerType
    ) {
        return smartHomeService.observerSubscribe(deviceId, observerType);
    }

    @PostMapping("/patterns/flyweight/demo")
    public Map<String, Object> flyweightDemo(@RequestParam(required = false) Integer count) {
        return smartHomeService.flyweightDemo(count);
    }

    @GetMapping("/patterns/flyweight/stats")
    public Map<String, Object> flyweightStats(@RequestParam(required = false) Integer instances) {
        return smartHomeService.flyweightStats(instances);
    }

    @PostMapping("/patterns/proxy/connect")
    public Map<String, Object> proxyConnect(@RequestParam String target, @RequestParam(required = false) String address) {
        return smartHomeService.proxyRemote(target, address != null ? address : "192.168.1.100:8080");
    }

    @PostMapping("/patterns/chain/process")
    public Map<String, Object> chainProcess(@RequestParam String message, @RequestParam String level) {
        return smartHomeService.chainAlert("system", level, message);
    }

    @PostMapping("/patterns/memento/save")
    public Map<String, Object> mementoSave(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sceneName
    ) {
        String effectiveName = (name != null && !name.isBlank()) ? name : sceneName;
        return smartHomeService.mementoSave(effectiveName);
    }

    @GetMapping("/patterns/memento/list")
    public Map<String, Object> mementoList() {
        return smartHomeService.listMementos();
    }

    @PostMapping("/patterns/memento/restore")
    public Map<String, Object> mementoRestore(
            @RequestParam(required = false) String snapshotId,
            @RequestParam(required = false) String sceneName
    ) {
        String effectiveId = (snapshotId != null && !snapshotId.isBlank()) ? snapshotId : sceneName;
        return smartHomeService.mementoRestore(effectiveId);
    }

    @PostMapping("/patterns/bridge/control")
    public Map<String, Object> bridgeControl(
            @RequestParam String device,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String remote,
            @RequestParam(required = false) String action
    ) {
        String effectiveRemote = (remote != null && !remote.isBlank()) ? remote : platform;
        if (action != null && !action.isBlank()) {
            return smartHomeService.bridgeControl(effectiveRemote, device, action);
        }
        return smartHomeService.bridgeControl(device, effectiveRemote);
    }

    @PostMapping("/patterns/composite/control")
    public Map<String, Object> compositeControl(@RequestParam String target, @RequestParam(required = false) String action) {
        return smartHomeService.compositeControl(target, action);
    }

    @PostMapping("/patterns/composite/action")
    public Map<String, Object> compositeAction(@RequestParam String group, @RequestParam(required = false) String action) {
        return smartHomeService.compositeAction(group, action);
    }

    @PostMapping("/patterns/prototype/clone")
    public Map<String, Object> prototypeClone(
            @RequestParam(required = false) String template,
            @RequestParam(required = false) String templateId
    ) {
        String effectiveTemplate = (template != null && !template.isBlank()) ? template : templateId;
        return smartHomeService.prototypeClone(effectiveTemplate);
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
