package com.smarthome;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for all REST API endpoints in SmartHomeController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SmartHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ─── Core Endpoints ──────────────────────────────────

    @Test
    void statusReturnsOnline() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemStatus").value("ONLINE"))
                .andExpect(jsonPath("$.homeMode").exists())
                .andExpect(jsonPath("$.activeDevices").isNumber());
    }

    @Test
    void listDevicesReturnsArray() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    void getDeviceById() throws Exception {
        mockMvc.perform(get("/api/devices/living-light-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("living-light-1"))
                .andExpect(jsonPath("$.info").exists());
    }

    @Test
    void getDeviceByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/devices/nonexistent-device"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void controlDeviceOn() throws Exception {
        mockMvc.perform(post("/api/devices/living-light-1/control").param("action", "on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOn").value(true));
    }

    @Test
    void controlDeviceOff() throws Exception {
        mockMvc.perform(post("/api/devices/living-light-1/control").param("action", "off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOn").value(false));
    }

    @Test
    void setModeNormal() throws Exception {
        mockMvc.perform(post("/api/mode/NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeMode").value("NORMAL"));
    }

    @Test
    void setModeAway() throws Exception {
        mockMvc.perform(post("/api/mode/AWAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeMode").value("AWAY"));
    }

    // ─── Pattern Endpoints ───────────────────────────────

    @Test
    void listPatterns() throws Exception {
        mockMvc.perform(get("/api/patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creational").isArray())
                .andExpect(jsonPath("$.structural").isArray())
                .andExpect(jsonPath("$.behavioral").isArray());
    }

    @Test
    void factoryCreate() throws Exception {
        mockMvc.perform(post("/api/patterns/factory/create")
                        .param("type", "LIGHT")
                        .param("name", "Test Light")
                        .param("location", "Test Room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LIGHT"))
                .andExpect(jsonPath("$.location").value("Test Room"));
    }

    @Test
    void abstractFactoryCreate() throws Exception {
        mockMvc.perform(post("/api/patterns/abstract-factory/create")
                        .param("ecosystem", "SMARTTHINGS")
                        .param("location", "Demo Room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void builderRule() throws Exception {
        mockMvc.perform(post("/api/patterns/builder/rule")
                        .param("name", "TestRule")
                        .param("trigger", "motion")
                        .param("condition", "dark")
                        .param("action", "lights on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestRule"));
    }

    @Test
    void prototypeTemplates() throws Exception {
        mockMvc.perform(get("/api/patterns/prototype/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adapterLegacy() throws Exception {
        mockMvc.perform(post("/api/patterns/adapter/legacy")
                        .param("name", "Old Thermo")
                        .param("location", "Basement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Adapter"));
    }

    @Test
    void bridgeDemo() throws Exception {
        mockMvc.perform(get("/api/patterns/bridge/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Bridge"));
    }

    @Test
    void compositeRooms() throws Exception {
        mockMvc.perform(get("/api/patterns/composite/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Composite"));
    }

    @Test
    void decoratorWrap() throws Exception {
        mockMvc.perform(post("/api/patterns/decorator/wrap")
                        .param("deviceId", "living-light-1")
                        .param("decorators", "LOGGING,SECURITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Decorator"));
    }

    @Test
    void facadeScene() throws Exception {
        mockMvc.perform(post("/api/patterns/facade/scene/morning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Facade"))
                .andExpect(jsonPath("$.scene").value("morning"));
    }

    @Test
    void proxyRemote() throws Exception {
        mockMvc.perform(post("/api/patterns/proxy/remote")
                        .param("name", "Test Cam")
                        .param("address", "192.168.1.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Proxy"));
    }

    @Test
    void chainAlert() throws Exception {
        mockMvc.perform(post("/api/patterns/chain/alert")
                        .param("deviceId", "sensor-1")
                        .param("level", "WARNING")
                        .param("message", "Motion detected"))
                .andExpect(status().isOk());
    }

    @Test
    void commandExecute() throws Exception {
        mockMvc.perform(post("/api/patterns/command/execute")
                        .param("deviceId", "living-light-1")
                        .param("command", "ON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Command"));
    }

    @Test
    void commandUndoRedo() throws Exception {
        // Execute first
        mockMvc.perform(post("/api/patterns/command/execute")
                        .param("deviceId", "living-light-1")
                        .param("command", "ON"))
                .andExpect(status().isOk());

        // Undo
        mockMvc.perform(post("/api/patterns/command/undo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Command"));

        // Redo
        mockMvc.perform(post("/api/patterns/command/redo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Command"));
    }

    @Test
    void interpreterEvaluate() throws Exception {
        mockMvc.perform(post("/api/patterns/interpreter/evaluate")
                        .param("rule", "motion AND dark")
                        .param("motion", "true")
                        .param("dark", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void observerSubscribeAndTrigger() throws Exception {
        mockMvc.perform(post("/api/patterns/observer/subscribe")
                        .param("deviceId", "sensor-1")
                        .param("observerType", "MOBILE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Observer"));

        mockMvc.perform(post("/api/patterns/observer/trigger")
                        .param("deviceId", "sensor-1")
                        .param("event", "MOTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Observer"));
    }

    @Test
    void strategyApply() throws Exception {
        mockMvc.perform(post("/api/patterns/strategy/apply")
                        .param("strategy", "ECO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Strategy"));
    }

    @Test
    void visitorAudit() throws Exception {
        mockMvc.perform(get("/api/patterns/visitor/audit")
                        .param("type", "SECURITY"))
                .andExpect(status().isOk());
    }

    @Test
    void stateTransition() throws Exception {
        mockMvc.perform(post("/api/patterns/state/transition")
                        .param("action", "PLAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("State"))
                .andExpect(jsonPath("$.newState").value("PLAYING"));
    }

    // ─── GET Demo Endpoints (used by app.js runPatternDemo) ──

    @Test
    void flyweightDemoGet() throws Exception {
        mockMvc.perform(get("/api/patterns/flyweight/demo"))
                .andExpect(status().isOk());
    }

    @Test
    void iteratorDemoGet() throws Exception {
        mockMvc.perform(get("/api/patterns/iterator/demo"))
                .andExpect(status().isOk());
    }

    @Test
    void mediatorDemoGet() throws Exception {
        mockMvc.perform(get("/api/patterns/mediator/demo"))
                .andExpect(status().isOk());
    }

    @Test
    void stateDemoGet() throws Exception {
        mockMvc.perform(get("/api/patterns/state/demo"))
                .andExpect(status().isOk());
    }

    @Test
    void templateDemoGet() throws Exception {
        mockMvc.perform(get("/api/patterns/template/demo"))
                .andExpect(status().isOk());
    }

    @Test
    void observerRegisterAlias() throws Exception {
        mockMvc.perform(post("/api/patterns/observer/register")
                        .param("deviceId", "sensor-1")
                        .param("observerType", "EMAIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Observer"));
    }

    // ─── Entity CRUD Endpoints ────────────────────────────

    @Test
    void roomsCrud() throws Exception {
        // List
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Create
        mockMvc.perform(post("/api/rooms/create")
                        .param("name", "Test Room API")
                        .param("floor", "2")
                        .param("roomType", "OFFICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Room API"));
    }

    @Test
    void scenesCrud() throws Exception {
        // Create
        mockMvc.perform(post("/api/scenes/create")
                        .param("name", "Test Scene API")
                        .param("description", "Test description")
                        .param("favorite", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Scene API"))
                .andExpect(jsonPath("$.isFavorite").value(true));

        // List
        mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    void rulesCrud() throws Exception {
        // Create
        mockMvc.perform(post("/api/rules/create")
                        .param("name", "Test Rule API")
                        .param("triggerCondition", "temp > 30")
                        .param("actionScript", "turn_on(living-light-1)")
                        .param("priority", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Rule API"))
                .andExpect(jsonPath("$.priority").value(8));
    }

    // ─── Validation / Edge Cases ──────────────────────────

    @Test
    void factoryCreateMissingParam() throws Exception {
        mockMvc.perform(post("/api/patterns/factory/create")
                        .param("type", "LIGHT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roomCreateDuplicate() throws Exception {
        String uniqueName = "DupRoom-" + System.currentTimeMillis();
        mockMvc.perform(post("/api/rooms/create").param("name", uniqueName))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/rooms/create").param("name", uniqueName))
                .andExpect(status().isBadRequest());
    }
}


