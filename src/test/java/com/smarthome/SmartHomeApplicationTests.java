package com.smarthome;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SmartHomeApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void statusEndpointReturnsJson() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemStatus").value("ONLINE"))
                .andExpect(jsonPath("$.homeMode").exists())
                .andExpect(jsonPath("$.activeDevices").isNumber());
    }

    @Test
    void indexPageRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Smart Home Control Center")));
    }

    @Test
    void uiPagesRender() throws Exception {
        mockMvc.perform(get("/ui/devices"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create Device")));

        mockMvc.perform(get("/ui/patterns"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Design Patterns")));

        mockMvc.perform(get("/ui/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Rooms")));

        mockMvc.perform(get("/ui/scenes"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Scenes")));

        mockMvc.perform(get("/ui/rules"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Automation Rules")));
    }

    @Test
    void entityApisReturnJson() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void ruleRunAcceptsVarsText() throws Exception {
        // Create a dedicated rule so the test is independent of seed data / test ordering
        String createJson = mockMvc.perform(post("/api/rules/create")
                        .param("name", "VarsTextTestRule")
                        .param("triggerCondition", "motion AND hour >= 18")
                        .param("actionScript", "turn_on(living-light-1)"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long ruleId = objectMapper.readTree(createJson).get("id").asLong();

        mockMvc.perform(post("/api/rules/{id}/run", ruleId)
                        .param("vars", "motion=true\nhour=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(true));
    }
}
