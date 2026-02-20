package com.smarthome;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for the new REST DELETE endpoints and service layer decomposition.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class RestEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deleteSceneViaDeleteVerb() throws Exception {
        // Create a scene first
        mockMvc.perform(post("/api/scenes/create")
                        .param("name", "DeleteTest-" + System.currentTimeMillis())
                        .param("description", "For delete test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        // Get scenes and find the ID
        String json = mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Delete via REST DELETE
        com.fasterxml.jackson.databind.JsonNode scenes = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        long sceneId = scenes.get(0).get("id").asLong();

        mockMvc.perform(delete("/api/scenes/" + sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void deleteRuleViaDeleteVerb() throws Exception {
        // Create a rule first
        mockMvc.perform(post("/api/rules/create")
                        .param("name", "DeleteRuleTest-" + System.currentTimeMillis())
                        .param("triggerCondition", "temp > 30")
                        .param("actionScript", "turn_on(living-light-1)"))
                .andExpect(status().isOk());

        String json = mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode rules = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        long ruleId = rules.get(0).get("id").asLong();

        mockMvc.perform(delete("/api/rules/" + ruleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void swaggerUiAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void openApiDocsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void faviconAvailable() throws Exception {
        mockMvc.perform(get("/favicon.svg"))
                .andExpect(status().isOk());
    }

    @Test
    void deviceServiceDirectAccess() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    void prototypeClone() throws Exception {
        mockMvc.perform(post("/api/patterns/prototype/clone")
                        .param("template", "ambient-light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Prototype"))
                .andExpect(jsonPath("$.action").value("clone"));
    }

    @Test
    void compositeControlHouse() throws Exception {
        mockMvc.perform(post("/api/patterns/composite/control")
                        .param("target", "house")
                        .param("action", "off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Composite"))
                .andExpect(jsonPath("$.affectedDevices").isNumber());
    }

    @Test
    void bridgeControlEndpoint() throws Exception {
        mockMvc.perform(post("/api/patterns/bridge/control")
                        .param("device", "TV")
                        .param("platform", "BASIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Bridge"));
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
                .andExpect(jsonPath("$.subscriberCount").value(Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void mementoSaveListRestore() throws Exception {
        mockMvc.perform(post("/api/patterns/memento/save")
                        .param("name", "MementoTest-" + System.currentTimeMillis()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern").value("Memento"));

        mockMvc.perform(get("/api/patterns/memento/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }
}

