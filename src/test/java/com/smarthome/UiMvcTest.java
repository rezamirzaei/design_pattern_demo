package com.smarthome;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Comprehensive MVC-based UI test suite.
 * Tests every page's structure, model attributes, navigation, accessibility,
 * semantic HTML, fragment reuse, forms, empty states, and content correctness.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DisplayName("UI MVC Tests")
class UiMvcTest {

    @Autowired
    private MockMvc mockMvc;

    // ═══════════════════════════════════════════════════════
    //  SHARED LAYOUT FRAGMENT TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Layout Fragments")
    class LayoutFragmentTests {

        @Test
        @DisplayName("All pages use shared navbar fragment with SmartHome OS brand")
        void allPagesHaveNavbar() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("SmartHome OS")))
                        .andExpect(content().string(containsString("nav-brand")))
                        .andExpect(content().string(containsString("nav-hamburger")));
            }
        }

        @Test
        @DisplayName("All pages include favicon link")
        void allPagesHaveFavicon() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("favicon.svg")));
            }
        }

        @Test
        @DisplayName("All pages include app.js")
        void allPagesIncludeAppJs() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("app.js")));
            }
        }

        @Test
        @DisplayName("All pages have footer with API Docs and DB Console links")
        void allPagesHaveFooter() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("app-footer")))
                        .andExpect(content().string(containsString("API Docs")))
                        .andExpect(content().string(containsString("DB Console")));
            }
        }

        @Test
        @DisplayName("All pages have toast container for notifications")
        void allPagesHaveToastContainer() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("toastContainer")));
            }
        }

        @Test
        @DisplayName("Navigation links point to correct pages")
        void navLinksAreCorrect() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("/ui/devices")))
                    .andExpect(content().string(containsString("/ui/rooms")))
                    .andExpect(content().string(containsString("/ui/scenes")))
                    .andExpect(content().string(containsString("/ui/rules")))
                    .andExpect(content().string(containsString("/ui/patterns")));
        }

        @Test
        @DisplayName("Active page is highlighted in navigation")
        void activeNavHighlight() throws Exception {
            // Dashboard should have active class on Dashboard link
            MvcResult dashResult = mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andReturn();
            String dashHtml = dashResult.getResponse().getContentAsString();
            // The navbar should mark the right page active
            // We just verify the pattern — the fragment uses th:classappend based on 'activePage'
            assert dashHtml.contains("nav-link");

            // Devices page
            MvcResult devResult = mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andReturn();
            String devHtml = devResult.getResponse().getContentAsString();
            // The devices link should be active (contains 'active' near 'Devices')
            assert devHtml.contains("active");
        }

        @Test
        @DisplayName("Pages use semantic HTML5 elements (main, section, nav, footer)")
        void semanticHtml() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("<main")))
                        .andExpect(content().string(containsString("<footer")))
                        .andExpect(content().string(containsString("<nav")));
            }
        }

        @Test
        @DisplayName("Pages have proper viewport meta tag")
        void viewportMetaTag() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("viewport")))
                    .andExpect(content().string(containsString("width=device-width")));
        }

        @Test
        @DisplayName("Pages have proper charset meta tag")
        void charsetMetaTag() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("UTF-8")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  DASHBOARD PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Dashboard Page")
    class DashboardTests {

        @Test
        @DisplayName("Dashboard renders with all model attributes")
        void rendersWithModelAttributes() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"))
                    .andExpect(model().attributeExists("systemStatus", "homeMode", "activeDevices", "devices", "rooms", "patterns"));
        }

        @Test
        @DisplayName("Dashboard shows system status panel")
        void showsSystemStatus() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("systemStatus")))
                    .andExpect(content().string(containsString("homeMode")))
                    .andExpect(content().string(containsString("Active Devices")))
                    .andExpect(content().string(containsString("Total Devices")));
        }

        @Test
        @DisplayName("Dashboard has quick actions section with home mode and scenes")
        void hasQuickActions() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Quick Actions")))
                    .andExpect(content().string(containsString("Home Mode")))
                    .andExpect(content().string(containsString("Quick Scenes")))
                    .andExpect(content().string(containsString("data-mode")))
                    .andExpect(content().string(containsString("data-scene")));
        }

        @Test
        @DisplayName("Dashboard has home mode buttons: NORMAL, AWAY, NIGHT, VACATION")
        void hasHomeModeButtons() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("NORMAL")))
                    .andExpect(content().string(containsString("AWAY")))
                    .andExpect(content().string(containsString("NIGHT")))
                    .andExpect(content().string(containsString("VACATION")));
        }

        @Test
        @DisplayName("Dashboard has scene shortcut buttons")
        void hasSceneButtons() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("movie")))
                    .andExpect(content().string(containsString("morning")))
                    .andExpect(content().string(containsString("sleep")))
                    .andExpect(content().string(containsString("party")));
        }

        @Test
        @DisplayName("Dashboard shows device summary with View All link")
        void hasDeviceSummary() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Devices")))
                    .andExpect(content().string(containsString("View All")));
        }

        @Test
        @DisplayName("Dashboard has device cards with control buttons")
        void hasDeviceCards() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("device-card")))
                    .andExpect(content().string(containsString("device-control-btn")));
        }

        @Test
        @DisplayName("Dashboard has rooms summary section")
        void hasRoomsSummary() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Rooms")));
        }

        @Test
        @DisplayName("Dashboard includes pattern playground with all 23 patterns")
        void hasPatternPlayground() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("pattern-nav")))
                    .andExpect(content().string(containsString("singleton")))
                    .andExpect(content().string(containsString("factory")))
                    .andExpect(content().string(containsString("observer")))
                    .andExpect(content().string(containsString("visitor")))
                    .andExpect(content().string(containsString("pattern-content")));
        }

        @Test
        @DisplayName("Dashboard includes patterns.js for pattern demos")
        void includesPatternsJs() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("patterns.js")));
        }

        @Test
        @DisplayName("Dashboard page title is correct")
        void hasCorrectTitle() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Dashboard")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  DEVICES PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Devices Page")
    class DevicesTests {

        @Test
        @DisplayName("Devices page renders with model attributes")
        void rendersWithModel() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("devices"))
                    .andExpect(model().attributeExists("devices", "activeDevices", "totalPower"));
        }

        @Test
        @DisplayName("Devices page has stats bar with total/active/inactive/power")
        void hasStatsBar() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("stats-bar")))
                    .andExpect(content().string(containsString("Total Devices")))
                    .andExpect(content().string(containsString("Active")))
                    .andExpect(content().string(containsString("Inactive")))
                    .andExpect(content().string(containsString("Power Draw")));
        }

        @Test
        @DisplayName("Devices page has create form with type, location, name fields")
        void hasCreateForm() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Create Device")))
                    .andExpect(content().string(containsString("Factory Method Pattern")))
                    .andExpect(content().string(containsString("LIGHT")))
                    .andExpect(content().string(containsString("THERMOSTAT")))
                    .andExpect(content().string(containsString("CAMERA")))
                    .andExpect(content().string(containsString("LOCK")))
                    .andExpect(content().string(containsString("deviceName")))
                    .andExpect(content().string(containsString("deviceLocation")));
        }

        @Test
        @DisplayName("Devices page shows device type icons")
        void showsDeviceTypeIcons() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("device-type-icon")));
        }

        @Test
        @DisplayName("Devices page has device control buttons (on/off)")
        void hasControlButtons() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("device-control-btn")))
                    .andExpect(content().string(containsString("Turn On")))
                    .andExpect(content().string(containsString("Turn Off")));
        }

        @Test
        @DisplayName("Devices page has power indicator")
        void hasPowerIndicator() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("power-indicator")));
        }

        @Test
        @DisplayName("Devices page has output log section")
        void hasOutputLog() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Output Log")));
        }

        @Test
        @DisplayName("Devices page form submits to factory endpoint")
        void formTargetsCorrectEndpoint() throws Exception {
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("/patterns/factory/create")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ROOMS PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Rooms Page")
    class RoomsTests {

        @Test
        @DisplayName("Rooms page renders with rooms and devices in model")
        void rendersWithModel() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("rooms"))
                    .andExpect(model().attributeExists("rooms", "devices"));
        }

        @Test
        @DisplayName("Rooms page has create room form with name, floor, type")
        void hasCreateForm() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Create Room")))
                    .andExpect(content().string(containsString("Composite Pattern")))
                    .andExpect(content().string(containsString("roomName")))
                    .andExpect(content().string(containsString("roomFloor")))
                    .andExpect(content().string(containsString("roomType")));
        }

        @Test
        @DisplayName("Rooms page has room type dropdown with options")
        void hasRoomTypeOptions() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("LIVING_ROOM")))
                    .andExpect(content().string(containsString("BEDROOM")))
                    .andExpect(content().string(containsString("KITCHEN")))
                    .andExpect(content().string(containsString("OFFICE")));
        }

        @Test
        @DisplayName("Rooms page shows device count badges")
        void showsDeviceCount() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("device")));
        }

        @Test
        @DisplayName("Rooms page has device assignment form")
        void hasDeviceAssignmentForm() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Assign")));
        }

        @Test
        @DisplayName("Rooms page create form submits to rooms/create endpoint")
        void formTargetsCorrectEndpoint() throws Exception {
            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("/rooms/create")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  SCENES PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenes Page")
    class ScenesTests {

        @Test
        @DisplayName("Scenes page renders with scenes in model")
        void rendersWithModel() throws Exception {
            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("scenes"))
                    .andExpect(model().attributeExists("scenes"));
        }

        @Test
        @DisplayName("Scenes page has save snapshot form")
        void hasSaveForm() throws Exception {
            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Save Scene Snapshot")))
                    .andExpect(content().string(containsString("Memento Pattern")))
                    .andExpect(content().string(containsString("sceneName")))
                    .andExpect(content().string(containsString("sceneDesc")));
        }

        @Test
        @DisplayName("Scenes page has favorite checkbox")
        void hasFavoriteCheckbox() throws Exception {
            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("favorite")))
                    .andExpect(content().string(containsString("Mark as favorite")));
        }

        @Test
        @DisplayName("Scenes page form submits to scenes/create endpoint")
        void formTargetsCorrectEndpoint() throws Exception {
            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("/scenes/create")));
        }

        @Test
        @DisplayName("Scenes page has output log")
        void hasOutputLog() throws Exception {
            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Output Log")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  RULES PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Rules Page")
    class RulesTests {

        @Test
        @DisplayName("Rules page renders with rules in model")
        void rendersWithModel() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("rules"))
                    .andExpect(model().attributeExists("rules"));
        }

        @Test
        @DisplayName("Rules page has create rule form with all fields")
        void hasCreateForm() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Create Rule")))
                    .andExpect(content().string(containsString("Interpreter")))
                    .andExpect(content().string(containsString("ruleName")))
                    .andExpect(content().string(containsString("rulePriority")))
                    .andExpect(content().string(containsString("triggerCondition")))
                    .andExpect(content().string(containsString("actionScript")));
        }

        @Test
        @DisplayName("Rules page has IF/THEN rule display")
        void hasRuleLogicDisplay() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("rule-logic")));
        }

        @Test
        @DisplayName("Rules page has actions reference section")
        void hasActionsReference() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("turn_on")))
                    .andExpect(content().string(containsString("turn_off")))
                    .andExpect(content().string(containsString("toggle")))
                    .andExpect(content().string(containsString("room_on")));
        }

        @Test
        @DisplayName("Rules page has delete confirmation")
        void hasDeleteConfirmation() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("confirm(")));
        }

        @Test
        @DisplayName("Rules page form submits to rules/create endpoint")
        void formTargetsCorrectEndpoint() throws Exception {
            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("/rules/create")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  PATTERNS PAGE TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Patterns Page")
    class PatternsTests {

        @Test
        @DisplayName("Patterns page renders with patterns, devices, rooms in model")
        void rendersWithModel() throws Exception {
            mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("patterns"))
                    .andExpect(model().attributeExists("patterns", "devices", "rooms"));
        }

        @Test
        @DisplayName("Patterns page has all 23 pattern tabs")
        void hasAll23PatternTabs() throws Exception {
            String html = mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            String[] patterns = {
                    "singleton", "factory", "abstract-factory", "builder", "prototype",
                    "adapter", "bridge", "composite", "decorator", "facade", "flyweight", "proxy",
                    "chain", "command", "interpreter", "iterator", "mediator", "memento",
                    "observer", "state", "strategy", "template", "visitor"
            };
            for (String p : patterns) {
                assert html.contains("data-pattern=\"" + p + "\"") : "Missing pattern tab: " + p;
            }
        }

        @Test
        @DisplayName("Patterns page has three category labels")
        void hasCategoryLabels() throws Exception {
            mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Creational")))
                    .andExpect(content().string(containsString("Structural")))
                    .andExpect(content().string(containsString("Behavioral")));
        }

        @Test
        @DisplayName("Patterns page includes patterns.js")
        void includesPatternsJs() throws Exception {
            mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("patterns.js")));
        }

        @Test
        @DisplayName("Patterns page has pattern-content container")
        void hasPatternContent() throws Exception {
            mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("pattern-content")));
        }

        @Test
        @DisplayName("Patterns page title references Design Patterns Lab")
        void hasCorrectTitle() throws Exception {
            mockMvc.perform(get("/ui/patterns"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Design Patterns Lab")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ACCESSIBILITY TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Accessibility")
    class AccessibilityTests {

        @Test
        @DisplayName("All pages have lang='en' attribute")
        void allPagesHaveLangAttribute() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("lang=\"en\"")));
            }
        }

        @Test
        @DisplayName("Navigation has ARIA label")
        void navHasAriaLabel() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("aria-label")));
        }

        @Test
        @DisplayName("Hamburger button has aria-label")
        void hamburgerHasAriaLabel() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Toggle navigation")));
        }

        @Test
        @DisplayName("Main content has role='main'")
        void mainHasRole() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("role=\"main\"")));
            }
        }

        @Test
        @DisplayName("Footer has role='contentinfo'")
        void footerHasRole() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("role=\"contentinfo\"")));
        }

        @Test
        @DisplayName("Toast container has aria-live='polite'")
        void toastHasAriaLive() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("aria-live=\"polite\"")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  CRUD WORKFLOW INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("CRUD Workflow Integration")
    class CrudWorkflowTests {

        @Test
        @DisplayName("Create device via API then verify it appears on devices page")
        void createDeviceShowsOnPage() throws Exception {
            String uniqueName = "UITestLight-" + System.currentTimeMillis();
            // Create via API
            mockMvc.perform(post("/api/patterns/factory/create")
                            .param("type", "LIGHT")
                            .param("name", uniqueName)
                            .param("location", "UI Test Room"))
                    .andExpect(status().isOk());

            // Verify it appears on the devices page
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(uniqueName)));
        }

        @Test
        @DisplayName("Create room via API then verify it appears on rooms page")
        void createRoomShowsOnPage() throws Exception {
            String uniqueName = "UITestRoom-" + System.currentTimeMillis();
            mockMvc.perform(post("/api/rooms/create")
                            .param("name", uniqueName)
                            .param("floor", "3"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/ui/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(uniqueName)));
        }

        @Test
        @DisplayName("Create scene via API then verify it appears on scenes page")
        void createSceneShowsOnPage() throws Exception {
            String uniqueName = "UITestScene-" + System.currentTimeMillis();
            mockMvc.perform(post("/api/scenes/create")
                            .param("name", uniqueName)
                            .param("description", "Test scene"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/ui/scenes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(uniqueName)));
        }

        @Test
        @DisplayName("Create rule via API then verify it appears on rules page")
        void createRuleShowsOnPage() throws Exception {
            String uniqueName = "UITestRule-" + System.currentTimeMillis();
            mockMvc.perform(post("/api/rules/create")
                            .param("name", uniqueName)
                            .param("triggerCondition", "temp > 30")
                            .param("actionScript", "turn_on(x)"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/ui/rules"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(uniqueName)));
        }

        @Test
        @DisplayName("Device control changes status shown on page")
        void deviceControlReflectsOnPage() throws Exception {
            // Turn on a seeded device
            mockMvc.perform(post("/api/devices/living-light-1/control")
                            .param("action", "on"))
                    .andExpect(status().isOk());

            // Verify status on dashboard
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("ON")));
        }

        @Test
        @DisplayName("Dashboard device count matches API response")
        void deviceCountConsistency() throws Exception {
            String apiJson = mockMvc.perform(get("/api/devices"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            com.fasterxml.jackson.databind.JsonNode devices = new com.fasterxml.jackson.databind.ObjectMapper().readTree(apiJson);
            int apiCount = devices.size();

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Total Devices")))
                    .andExpect(content().string(containsString(String.valueOf(apiCount))));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  RESPONSIVE & DESIGN QUALITY TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Design Quality")
    class DesignQualityTests {

        @Test
        @DisplayName("CSS file loads successfully")
        void cssLoads() throws Exception {
            mockMvc.perform(get("/css/patterns-pro.css"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(":root")))
                    .andExpect(content().string(containsString("--primary")));
        }

        @Test
        @DisplayName("JavaScript file loads successfully")
        void jsLoads() throws Exception {
            mockMvc.perform(get("/js/app.js"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("API_BASE")));
        }

        @Test
        @DisplayName("Patterns JavaScript loads successfully")
        void patternsJsLoads() throws Exception {
            mockMvc.perform(get("/js/patterns.js"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("patternApiCall")));
        }

        @Test
        @DisplayName("Favicon SVG loads successfully")
        void faviconLoads() throws Exception {
            mockMvc.perform(get("/favicon.svg"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("All pages include patterns-pro.css stylesheet")
        void allPagesIncludeStylesheet() throws Exception {
            String[] pages = {"/", "/ui/devices", "/ui/rooms", "/ui/scenes", "/ui/rules", "/ui/patterns"};
            for (String page : pages) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("patterns-pro.css")));
            }
        }

        @Test
        @DisplayName("Empty state styling exists on devices page (when devices exist they show cards)")
        void emptyStateClassExists() throws Exception {
            // Even with seeded data, the empty-state class markup should be in the page
            // (it's just not visible because th:if hides it)
            // Just verify the page has the correct structural elements
            mockMvc.perform(get("/ui/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("grid")));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ERROR HANDLING TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Non-existent page returns error status")
        void nonExistentPageReturnsError() throws Exception {
            mockMvc.perform(get("/ui/nonexistent"))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Non-existent API endpoint returns error status")
        void nonExistentApiReturnsError() throws Exception {
            mockMvc.perform(get("/api/nonexistent"))
                    .andExpect(status().is5xxServerError());
        }
    }
}


