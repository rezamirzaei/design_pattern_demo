package com.smarthome.service;

import com.smarthome.domain.DeviceType;
import com.smarthome.domain.HomeMode;
import com.smarthome.pattern.creational.builder.AutomationRule;
import com.smarthome.web.viewmodel.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Thin facade that delegates to focused services.
 * Preserves backward compatibility with existing controller and test code.
 *
 * @see DeviceService
 * @see RoomService
 * @see SceneService
 * @see RuleService
 * @see PatternDemoService
 */
@Service
public class SmartHomeService {

    private final DeviceService deviceService;
    private final RoomService roomService;
    private final SceneService sceneService;
    private final RuleService ruleService;
    private final PatternDemoService patternDemoService;

    public SmartHomeService(DeviceService deviceService,
                            RoomService roomService,
                            SceneService sceneService,
                            RuleService ruleService,
                            PatternDemoService patternDemoService) {
        this.deviceService = deviceService;
        this.roomService = roomService;
        this.sceneService = sceneService;
        this.ruleService = ruleService;
        this.patternDemoService = patternDemoService;
    }

    // ═══ Device ══════════════════════════════════════════════
    public List<DeviceView> getDevices() { return deviceService.getDevices(); }
    public DeviceView getDevice(String id) { return deviceService.getDevice(id); }
    public DeviceView controlDevice(String id, boolean turnOn) { return deviceService.controlDevice(id, turnOn); }
    public List<DeviceView> controlRoom(String room, boolean turnOn) { return deviceService.controlRoom(room, turnOn); }
    public StatusView getStatus() { return deviceService.getStatus(); }
    public HomeMode setHomeMode(HomeMode mode) { return deviceService.setHomeMode(mode); }

    // ═══ Factory / Abstract Factory ══════════════════════════
    public DeviceView createDeviceViaFactory(DeviceType type, String name, String location) { return deviceService.createDeviceViaFactory(type, name, location); }
    public List<DeviceView> createDevicesViaAbstractFactory(String eco, String loc) { return deviceService.createDevicesViaAbstractFactory(eco, loc); }

    // ═══ Room ════════════════════════════════════════════════
    public List<String> getRooms() { return roomService.getRoomNames(); }
    public List<RoomView> getRoomViews() { return roomService.getRoomViews(); }
    public RoomView createRoom(String name, String floor, String type) { return roomService.createRoom(name, floor, type); }
    public RoomView assignDeviceToRoom(Long roomId, String deviceId) { return roomService.assignDeviceToRoom(roomId, deviceId); }
    public RoomView unassignDeviceFromRoom(Long roomId, String deviceId) { return roomService.unassignDeviceFromRoom(roomId, deviceId); }

    // ═══ Scene ═══════════════════════════════════════════════
    public List<SceneView> getScenes() { return sceneService.getScenes(); }
    public SceneView createSceneSnapshot(String name, String desc, boolean fav) { return sceneService.createSceneSnapshot(name, desc, fav); }
    public SceneView toggleSceneFavorite(Long id) { return sceneService.toggleSceneFavorite(id); }
    public Map<String, Object> applySceneSnapshot(Long id) { return sceneService.applySceneSnapshot(id); }
    public Map<String, Object> deleteScene(Long id) { return sceneService.deleteScene(id); }

    // ═══ Rule ════════════════════════════════════════════════
    public List<AutomationRuleView> getAutomationRules() { return ruleService.getAutomationRules(); }
    public AutomationRuleView createAutomationRule(String n, String d, String tc, String as, Integer p) { return ruleService.createAutomationRule(n, d, tc, as, p); }
    public AutomationRuleView toggleAutomationRule(Long id) { return ruleService.toggleAutomationRule(id); }
    public Map<String, Object> deleteAutomationRule(Long id) { return ruleService.deleteAutomationRule(id); }
    public Map<String, Object> runAutomationRule(Long id, Map<String, Object> vars, boolean exec) { return ruleService.runAutomationRule(id, vars, exec); }
    public AutomationRule buildAutomationRule(String n, String t, String c, String a) { return ruleService.buildAutomationRule(n, t, c, a); }

    // ═══ Pattern Demos ═══════════════════════════════════════
    public Map<String, Object> listPatterns() { return patternDemoService.listPatterns(); }
    public List<Map<String, Object>> listPrototypeTemplates() { return patternDemoService.listPrototypeTemplates(); }
    public Map<String, Object> prototypeClone(String t) { return patternDemoService.prototypeClone(t); }
    public Map<String, Object> adapterDemo(String n, String l) { return patternDemoService.adapterDemo(n, l); }
    public Map<String, Object> bridgeDemo() { return patternDemoService.bridgeDemo(); }
    public Map<String, Object> bridgeControl(String r, String d, String a) { return patternDemoService.bridgeControl(r, d, a); }
    public Map<String, Object> bridgeControl(String d, String p) { return patternDemoService.bridgeControl(d, p); }
    public Map<String, Object> roomsComposite() { return patternDemoService.roomsComposite(); }
    public Map<String, Object> compositeControl(String t, String a) { return patternDemoService.compositeControl(t, a); }
    public Map<String, Object> compositeAction(String g, String a) { return patternDemoService.compositeAction(g, a); }
    public Map<String, Object> decoratorWrap(String id, String d) { return patternDemoService.decoratorWrap(id, d); }
    public Map<String, Object> activateScene(String n) { return patternDemoService.activateScene(n); }
    public Map<String, Object> flyweightDemo(Integer c) { return patternDemoService.flyweightDemo(c); }
    public Map<String, Object> flyweightStats(Integer i) { return patternDemoService.flyweightStats(i); }
    public Map<String, Object> proxyRemote(String n, String a) { return patternDemoService.proxyRemote(n, a); }
    public Map<String, Object> chainAlert(String d, String l, String m) { return patternDemoService.chainAlert(d, l, m); }
    public Map<String, Object> commandExecute(String d, String c) { return patternDemoService.commandExecute(d, c); }
    public Map<String, Object> commandUndo() { return patternDemoService.commandUndo(); }
    public Map<String, Object> commandRedo() { return patternDemoService.commandRedo(); }
    public Map<String, Object> interpreterEvaluate(String r, Map<String, Object> v) { return patternDemoService.interpreterEvaluate(r, v); }
    public Map<String, Object> interpreterEvaluate(String r) { return patternDemoService.interpreterEvaluate(r); }
    public Map<String, Object> iteratorIterate(String t, String f) { return patternDemoService.iteratorIterate(t, f); }
    public Map<String, Object> mediatorDemo() { return patternDemoService.mediatorDemo(); }
    public Map<String, Object> mediatorNotify(String s, String e) { return patternDemoService.mediatorNotify(s, e); }
    public Map<String, Object> mementoSave(String n) { return patternDemoService.mementoSave(n); }
    public Map<String, Object> listMementos() { return sceneService.listMementos(); }
    public Map<String, Object> mementoRestore(String id) { return sceneService.mementoRestore(id); }
    public Map<String, Object> observerSubscribe(String d, String t) { return patternDemoService.observerSubscribe(d, t); }
    public Map<String, Object> observerTrigger(String d, String e) { return patternDemoService.observerTrigger(d, e); }
    public Map<String, Object> stateDemo() { return patternDemoService.stateDemo(); }
    public Map<String, Object> stateTransition(String a) { return patternDemoService.stateTransition(a); }
    public Map<String, Object> applyEnergyStrategy(String s) { return patternDemoService.applyEnergyStrategy(s); }
    public Map<String, Object> templateDemo(String t) { return patternDemoService.templateDemo(t); }
    public Map<String, Object> templateInit(String t) { return patternDemoService.templateInit(t); }
    public Map<String, Object> visitorAudit(String t) { return patternDemoService.visitorAudit(t); }
}
