package com.smarthome;

import static org.junit.jupiter.api.Assertions.*;

import com.smarthome.domain.DeviceType;
import com.smarthome.service.SmartHomeService;
import com.smarthome.web.viewmodel.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for SmartHomeService business logic.
 */
@SpringBootTest
@ActiveProfiles("h2")
class SmartHomeServiceTest {

    @Autowired
    private SmartHomeService service;

    // ─── Device Operations ────────────────────────────────

    @Test
    void getDevicesReturnsSeeded() {
        List<DeviceView> devices = service.getDevices();
        assertNotNull(devices);
        assertFalse(devices.isEmpty(), "Should have seeded demo devices");
    }

    @Test
    void getDeviceById() {
        DeviceView device = service.getDevice("living-light-1");
        assertNotNull(device);
        assertEquals("living-light-1", device.id());
    }

    @Test
    void controlDeviceTurnOnAndOff() {
        DeviceView on = service.controlDevice("living-light-1", true);
        assertTrue(on.isOn());
        assertTrue(on.power() > 0);

        DeviceView off = service.controlDevice("living-light-1", false);
        assertFalse(off.isOn());
        assertEquals(0, off.power());
    }

    @Test
    void getDeviceNotFoundThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.getDevice("no-such-device"));
    }

    // ─── Factory Pattern ─────────────────────────────────

    @Test
    void createDeviceViaFactory() {
        DeviceView light = service.createDeviceViaFactory(DeviceType.LIGHT, "Test Light", "Test Room");
        assertNotNull(light);
        assertEquals(DeviceType.LIGHT, light.type());
        assertEquals("Test Room", light.location());
    }

    @Test
    void createDevicesViaAbstractFactory() {
        List<DeviceView> devices = service.createDevicesViaAbstractFactory("HOMEKIT", "Living Room");
        assertNotNull(devices);
        assertEquals(4, devices.size(), "Abstract factory creates 4 devices");
    }

    // ─── Room Operations ──────────────────────────────────

    @Test
    void getRoomsReturnsSeeded() {
        List<String> rooms = service.getRooms();
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());
    }

    @Test
    void createRoomAndGetViews() {
        String name = "Service Test Room " + System.currentTimeMillis();
        RoomView room = service.createRoom(name, "1", "OFFICE");
        assertNotNull(room);
        assertEquals(name, room.name());

        List<RoomView> views = service.getRoomViews();
        assertTrue(views.stream().anyMatch(r -> r.name().equals(name)));
    }

    @Test
    void createDuplicateRoomThrows() {
        String name = "DupRoom-" + System.currentTimeMillis();
        service.createRoom(name, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(name, null, null));
    }

    // ─── Scene Operations ─────────────────────────────────

    @Test
    void createAndListScenes() {
        String name = "Test Scene " + System.currentTimeMillis();
        SceneView scene = service.createSceneSnapshot(name, "Test", true);
        assertNotNull(scene);
        assertEquals(name, scene.name());
        assertTrue(scene.isFavorite());

        List<SceneView> scenes = service.getScenes();
        assertTrue(scenes.stream().anyMatch(s -> s.name().equals(name)));
    }

    @Test
    void toggleSceneFavorite() {
        String name = "Fav Test " + System.currentTimeMillis();
        SceneView scene = service.createSceneSnapshot(name, null, false);
        assertFalse(scene.isFavorite());

        SceneView toggled = service.toggleSceneFavorite(scene.id());
        assertTrue(toggled.isFavorite());

        SceneView toggledBack = service.toggleSceneFavorite(scene.id());
        assertFalse(toggledBack.isFavorite());
    }

    // ─── Rule Operations ──────────────────────────────────

    @Test
    void createAndListRules() {
        String name = "Test Rule " + System.currentTimeMillis();
        AutomationRuleView rule = service.createAutomationRule(
                name, "description", "motion AND dark", "turn_on(living-light-1)", 7);
        assertNotNull(rule);
        assertEquals(name, rule.name());
        assertEquals(7, rule.priority());
        assertTrue(rule.isEnabled());

        List<AutomationRuleView> rules = service.getAutomationRules();
        assertTrue(rules.stream().anyMatch(r -> r.name().equals(name)));
    }

    @Test
    void toggleAndDeleteRule() {
        String name = "Toggle Rule " + System.currentTimeMillis();
        AutomationRuleView rule = service.createAutomationRule(name, null, "x", "y", 5);
        assertTrue(rule.isEnabled());

        AutomationRuleView disabled = service.toggleAutomationRule(rule.id());
        assertFalse(disabled.isEnabled());

        Map<String, Object> deleted = service.deleteAutomationRule(rule.id());
        assertTrue((Boolean) deleted.get("deleted"));
    }

    // ─── Status ────────────────────────────────────────────

    @Test
    void getStatus() {
        StatusView status = service.getStatus();
        assertNotNull(status);
        assertEquals("ONLINE", status.systemStatus());
        assertNotNull(status.homeMode());
    }

    // ─── Pattern Demos ─────────────────────────────────────

    @Test
    void bridgeDemoReturnsData() {
        Map<String, Object> result = service.bridgeDemo();
        assertEquals("Bridge", result.get("pattern"));
    }

    @Test
    void chainAlertReturnsData() {
        Map<String, Object> result = service.chainAlert("sensor-1", "INFO", "test");
        assertNotNull(result);
    }

    @Test
    void flyweightDemoReturnsData() {
        Map<String, Object> result = service.flyweightDemo(50);
        assertNotNull(result);
    }

    @Test
    void stateTransition() {
        Map<String, Object> result = service.stateTransition("PLAY");
        assertEquals("State", result.get("pattern"));
        assertEquals("PLAYING", result.get("newState"));
    }

    @Test
    void listPatterns() {
        Map<String, Object> patterns = service.listPatterns();
        assertNotNull(patterns.get("creational"));
        assertNotNull(patterns.get("structural"));
        assertNotNull(patterns.get("behavioral"));
    }

    @Test
    void prototypeClone() {
        Map<String, Object> result = service.prototypeClone("ambient-light");
        assertEquals("Prototype", result.get("pattern"));
        assertEquals("clone", result.get("action"));
    }

    @Test
    void observerSubscribeAndTrigger() {
        Map<String, Object> subscribe = service.observerSubscribe("sensor-1", "EMAIL");
        assertEquals("Observer", subscribe.get("pattern"));
        assertEquals("subscribe", subscribe.get("action"));

        Map<String, Object> trigger = service.observerTrigger("sensor-1", "MOTION");
        assertEquals("Observer", trigger.get("pattern"));
        assertEquals("trigger", trigger.get("action"));
    }
}

