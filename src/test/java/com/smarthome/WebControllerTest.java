package com.smarthome;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for all Thymeleaf web pages served by WebController.
 * Ensures pages render, model attributes are populated, and navigation works.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardPageRendersWithModelAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("systemStatus", "homeMode", "activeDevices", "devices", "rooms", "patterns"))
                .andExpect(content().string(Matchers.containsString("Smart Home Control Center")))
                .andExpect(content().string(Matchers.containsString("SmartHome OS")));
    }

    @Test
    void devicesPageRendersWithDevices() throws Exception {
        mockMvc.perform(get("/ui/devices"))
                .andExpect(status().isOk())
                .andExpect(view().name("devices"))
                .andExpect(model().attributeExists("devices"))
                .andExpect(content().string(Matchers.containsString("Create Device")))
                .andExpect(content().string(Matchers.containsString("Dashboard")));
    }

    @Test
    void roomsPageRendersWithRoomsAndDevices() throws Exception {
        mockMvc.perform(get("/ui/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms"))
                .andExpect(model().attributeExists("rooms", "devices"))
                .andExpect(content().string(Matchers.containsString("Rooms")));
    }

    @Test
    void scenesPageRendersWithScenes() throws Exception {
        mockMvc.perform(get("/ui/scenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("scenes"))
                .andExpect(model().attributeExists("scenes"))
                .andExpect(content().string(Matchers.containsString("Scenes")));
    }

    @Test
    void rulesPageRendersWithRules() throws Exception {
        mockMvc.perform(get("/ui/rules"))
                .andExpect(status().isOk())
                .andExpect(view().name("rules"))
                .andExpect(model().attributeExists("rules"))
                .andExpect(content().string(Matchers.containsString("Automation Rules")));
    }

    @Test
    void patternsPageRendersWithPatterns() throws Exception {
        mockMvc.perform(get("/ui/patterns"))
                .andExpect(status().isOk())
                .andExpect(view().name("patterns"))
                .andExpect(model().attributeExists("patterns", "devices", "rooms"))
                .andExpect(content().string(Matchers.containsString("Design Patterns")));
    }

    @Test
    void allPagesHaveConsistentNavigation() throws Exception {
        // Every page should contain the Dashboard link
        String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(Matchers.containsString("SmartHome OS")));
        }
    }

    @Test
    void allPagesIncludeAppJs() throws Exception {
        // devices, rooms, scenes, rules should include app.js
        String[] pages = {"/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules"};
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(Matchers.containsString("app.js")));
        }
    }
}

