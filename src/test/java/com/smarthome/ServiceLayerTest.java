package com.smarthome;

import com.smarthome.service.*;
import com.smarthome.web.viewmodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the decomposed service layer (DeviceService, RoomService, SceneService, RuleService, PatternDemoService).
 */
@SpringBootTest
@ActiveProfiles("h2")
class ServiceLayerTest {

    @Autowired private DeviceService deviceService;
    @Autowired private RoomService roomService;
    @Autowired private SceneService sceneService;
    @Autowired private RuleService ruleService;
    @Autowired private PatternDemoService patternDemoService;

    // ── DeviceService ────────────────────────────────────────

    @Test
    void deviceServiceGetDevices() {
        List<DeviceView> devices = deviceService.getDevices();
        assertNotNull(devices);
        assertFalse(devices.isEmpty(), "DataSeeder should have created devices");
    }

    @Test
    void deviceServiceGetStatus() {
        StatusView status = deviceService.getStatus();
        assertEquals("ONLINE", status.systemStatus());
        assertNotNull(status.homeMode());
    }

    @Test
    void deviceServiceControlDevice() {
        DeviceView on = deviceService.controlDevice("living-light-1", true);
        assertTrue(on.isOn());
        DeviceView off = deviceService.controlDevice("living-light-1", false);
        assertFalse(off.isOn());
    }

    @Test
    void deviceServiceThrowsOnNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> deviceService.getDevice("nonexistent-device-xyz"));
    }

    // ── RoomService ──────────────────────────────────────────

    @Test
    void roomServiceGetRoomViews() {
        List<RoomView> rooms = roomService.getRoomViews();
        assertNotNull(rooms);
    }

    @Test
    void roomServiceCreateAndAssign() {
        String name = "ServiceTest-" + System.currentTimeMillis();
        RoomView room = roomService.createRoom(name, "3", "LAB");
        assertNotNull(room.id());
        assertEquals(name, room.name());

        RoomView assigned = roomService.assignDeviceToRoom(room.id(), "living-light-1");
        assertTrue(assigned.deviceCount() > 0);
    }

    @Test
    void roomServiceDuplicateThrows() {
        String name = "DupRoom-" + System.currentTimeMillis();
        roomService.createRoom(name, null, null);
        assertThrows(IllegalArgumentException.class, () -> roomService.createRoom(name, null, null));
    }

    // ── SceneService ─────────────────────────────────────────

    @Test
    void sceneServiceCrud() {
        String name = "SceneTest-" + System.currentTimeMillis();
        SceneView scene = sceneService.createSceneSnapshot(name, "desc", true);
        assertNotNull(scene.id());
        assertTrue(scene.isFavorite());

        SceneView toggled = sceneService.toggleSceneFavorite(scene.id());
        assertFalse(toggled.isFavorite());

        Map<String, Object> applied = sceneService.applySceneSnapshot(scene.id());
        assertEquals(name, applied.get("sceneName"));

        Map<String, Object> deleted = sceneService.deleteScene(scene.id());
        assertTrue((Boolean) deleted.get("deleted"));
    }

    // ── RuleService ──────────────────────────────────────────

    @Test
    void ruleServiceCrud() {
        String name = "RuleTest-" + System.currentTimeMillis();
        AutomationRuleView rule = ruleService.createAutomationRule(name, "test", "temp > 30", "turn_on(living-light-1)", 7);
        assertNotNull(rule.id());
        assertEquals(7, rule.priority());

        AutomationRuleView toggled = ruleService.toggleAutomationRule(rule.id());
        assertFalse(toggled.isEnabled());

        Map<String, Object> deleted = ruleService.deleteAutomationRule(rule.id());
        assertTrue((Boolean) deleted.get("deleted"));
    }

    // ── PatternDemoService ───────────────────────────────────

    @Test
    void patternListPatterns() {
        Map<String, Object> patterns = patternDemoService.listPatterns();
        assertTrue(patterns.containsKey("creational"));
        assertTrue(patterns.containsKey("structural"));
        assertTrue(patterns.containsKey("behavioral"));
    }

    @Test
    void patternStateTransition() {
        Map<String, Object> result = patternDemoService.stateTransition("PLAY");
        assertEquals("State", result.get("pattern"));
        assertEquals("PLAYING", result.get("newState"));
    }

    @Test
    void patternObserverSubscribeTrigger() {
        Map<String, Object> sub = patternDemoService.observerSubscribe("sensor-1", "EMAIL");
        assertEquals("Observer", sub.get("pattern"));
        assertEquals("subscribe", sub.get("action"));

        Map<String, Object> trigger = patternDemoService.observerTrigger("sensor-1", "MOTION");
        assertEquals("Observer", trigger.get("pattern"));
    }

    @Test
    void patternFlyweight() {
        Map<String, Object> demo = patternDemoService.flyweightDemo(50);
        assertNotNull(demo);
    }

    @Test
    void patternBridgeDemo() {
        Map<String, Object> result = patternDemoService.bridgeDemo();
        assertEquals("Bridge", result.get("pattern"));
    }

    @Test
    void patternAdapterDemo() {
        Map<String, Object> result = patternDemoService.adapterDemo("Legacy Thermo", "Basement");
        assertEquals("Adapter", result.get("pattern"));
    }

    @Test
    void patternChainAlert() {
        Map<String, Object> result = patternDemoService.chainAlert("sensor-1", "WARNING", "High humidity");
        assertNotNull(result);
    }

    @Test
    void patternMediatorDemo() {
        Map<String, Object> result = patternDemoService.mediatorDemo();
        assertNotNull(result);
    }

    @Test
    void patternProxyRemote() {
        Map<String, Object> result = patternDemoService.proxyRemote("Remote TV", "192.168.1.100");
        assertEquals("Proxy", result.get("pattern"));
    }

    @Test
    void patternVisitorAudit() {
        Map<String, Object> result = patternDemoService.visitorAudit("energy");
        assertNotNull(result);
    }

    @Test
    void patternStrategyApply() {
        Map<String, Object> result = patternDemoService.applyEnergyStrategy("BALANCED");
        assertEquals("Strategy", result.get("pattern"));
    }

    @Test
    void patternTemplateDemo() {
        Map<String, Object> result = patternDemoService.templateDemo("LIGHT");
        assertNotNull(result);
    }

    @Test
    void serviceUtilsRequireText() {
        assertEquals("hello", ServiceUtils.requireText("  hello  ", "fail"));
        assertThrows(IllegalArgumentException.class, () -> ServiceUtils.requireText("", "fail"));
        assertThrows(IllegalArgumentException.class, () -> ServiceUtils.requireText(null, "fail"));
    }

    @Test
    void serviceUtilsToTitleCase() {
        assertEquals("Living Room", ServiceUtils.toTitleCase("living room"));
        assertEquals("Kitchen", ServiceUtils.toTitleCase("kitchen"));
        assertEquals("", ServiceUtils.toTitleCase(""));
    }
}

