package com.smarthome.controller;

import com.smarthome.service.SmartHomeService;
import com.smarthome.web.catalog.PatternCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    private final SmartHomeService smartHomeService;
    private final PatternCatalog patternCatalog;

    public WebController(SmartHomeService smartHomeService, PatternCatalog patternCatalog) {
        this.smartHomeService = smartHomeService;
        this.patternCatalog = patternCatalog;
    }

    @GetMapping("/")
    public String index(Model model) {
        var status = smartHomeService.getStatus();
        var devices = smartHomeService.getDevices();
        var rooms = smartHomeService.getRooms();

        model.addAttribute("systemStatus", status.systemStatus());
        model.addAttribute("homeMode", status.homeMode());
        model.addAttribute("activeDevices", status.activeDevices());
        model.addAttribute("devices", devices);
        model.addAttribute("rooms", rooms);
        model.addAttribute("patterns", patternCatalog.all());
        return "index";
    }

    @GetMapping("/ui/patterns")
    public String patterns(Model model) {
        model.addAttribute("patterns", patternCatalog.all());
        model.addAttribute("devices", smartHomeService.getDevices());
        model.addAttribute("rooms", smartHomeService.getRooms());
        return "patterns";
    }

    @GetMapping("/ui/devices")
    public String devices(Model model) {
        model.addAttribute("devices", smartHomeService.getDevices());
        return "devices";
    }

    @GetMapping("/ui/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", smartHomeService.getRoomViews());
        model.addAttribute("devices", smartHomeService.getDevices());
        return "rooms";
    }

    @GetMapping("/ui/scenes")
    public String scenes(Model model) {
        model.addAttribute("scenes", smartHomeService.getScenes());
        return "scenes";
    }

    @GetMapping("/ui/rules")
    public String rules(Model model) {
        model.addAttribute("rules", smartHomeService.getAutomationRules());
        return "rules";
    }
}
