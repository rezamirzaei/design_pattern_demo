package com.smarthome.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.domain.AutomationRuleEntity;
import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.DeviceType;
import com.smarthome.domain.RoomEntity;
import com.smarthome.domain.SceneEntity;
import com.smarthome.pattern.creational.abstractfactory.SmartThingsSensor;
import com.smarthome.pattern.creational.factory.Device;
import com.smarthome.pattern.creational.factory.SmartCamera;
import com.smarthome.pattern.creational.factory.SmartLight;
import com.smarthome.pattern.creational.factory.SmartLock;
import com.smarthome.pattern.creational.factory.SmartThermostat;
import com.smarthome.pattern.creational.singleton.HomeController;
import com.smarthome.repository.AutomationRuleRepository;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.RoomRepository;
import com.smarthome.repository.SceneRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final SceneRepository sceneRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final ObjectMapper objectMapper;

    public DataSeeder(
            DeviceRepository deviceRepository,
            RoomRepository roomRepository,
            SceneRepository sceneRepository,
            AutomationRuleRepository automationRuleRepository,
            ObjectMapper objectMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.sceneRepository = sceneRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        boolean seededDevices = false;
        if (deviceRepository.count() == 0) {
            List<DeviceEntity> seeded = List.of(
                    new DeviceEntity("living-light-1", "Living Room Light", DeviceType.LIGHT, "Living Room", false, 12, "LOCAL"),
                    new DeviceEntity("living-light-2", "Ambient Lamp", DeviceType.LIGHT, "Living Room", false, 8, "LOCAL"),
                    new DeviceEntity("thermostat-1", "Main Thermostat", DeviceType.THERMOSTAT, "Living Room", true, 5, "LOCAL"),
                    new DeviceEntity("camera-1", "Front Yard Camera", DeviceType.CAMERA, "Front Yard", true, 8, "LOCAL"),
                    new DeviceEntity("hall-lock-1", "Front Door Lock", DeviceType.LOCK, "Hallway", true, 2, "LOCAL"),
                    new DeviceEntity("sensor-1", "Door Sensor", DeviceType.SENSOR, "Front Door", true, 1, "LOCAL")
            );
            deviceRepository.saveAll(seeded);
            seededDevices = true;
        }

        if (seededDevices) {
            HomeController controller = HomeController.INSTANCE;
            register(controller, "living-light-1", new SmartLight("Living Room Light", "Living Room"), false);
            register(controller, "living-light-2", new SmartLight("Ambient Lamp", "Living Room"), false);
            register(controller, "thermostat-1", new SmartThermostat("Main Thermostat", "Living Room"), true);
            register(controller, "camera-1", new SmartCamera("Front Yard Camera", "Front Yard"), true);
            register(controller, "hall-lock-1", new SmartLock("Front Door Lock", "Hallway"), true);
            register(controller, "sensor-1", new SmartThingsSensor("Door Sensor", "Front Door"), true);
        }

        if (roomRepository.count() == 0) {
            seedRoomsFromDeviceLocations();
        }

        if (sceneRepository.count() == 0) {
            seedDefaultScene();
        }

        if (automationRuleRepository.count() == 0) {
            seedDefaultRule();
        }
    }

    private static void register(HomeController controller, String id, Device device, boolean turnOn) {
        if (turnOn) {
            device.turnOn();
        } else {
            device.turnOff();
        }
        controller.registerDevice(id, device);
    }

    private void seedRoomsFromDeviceLocations() {
        Map<String, List<DeviceEntity>> devicesByRoom = new LinkedHashMap<>();
        List<DeviceEntity> devices = deviceRepository.findAll();
        for (DeviceEntity device : devices) {
            String location = device.getLocation() == null ? "Unassigned" : device.getLocation().trim();
            if (location.isBlank()) {
                location = "Unassigned";
            }
            devicesByRoom.computeIfAbsent(location, ignored -> new ArrayList<>()).add(device);
        }

        Map<String, RoomEntity> rooms = new LinkedHashMap<>();
        for (String location : devicesByRoom.keySet()) {
            RoomEntity created = new RoomEntity();
            created.setName(location);
            created.setFloor("1");
            created.setRoomType(guessRoomType(location));
            rooms.put(location, created);
        }

        roomRepository.saveAll(rooms.values());

        for (Map.Entry<String, List<DeviceEntity>> entry : devicesByRoom.entrySet()) {
            RoomEntity room = rooms.get(entry.getKey());
            if (room == null) {
                continue;
            }
            room.getDevices().addAll(entry.getValue());
            roomRepository.save(room);
        }
    }

    private void seedDefaultScene() {
        Map<String, Boolean> deviceStates = new LinkedHashMap<>();
        deviceRepository.findAll().stream()
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                .forEach(device -> deviceStates.put(device.getId(), device.isOn()));

        String json;
        try {
            json = objectMapper.writeValueAsString(deviceStates);
        } catch (Exception e) {
            json = "{}";
        }

        SceneEntity scene = new SceneEntity();
        scene.setName("Demo Snapshot");
        scene.setDescription("Seeded snapshot of current device states");
        scene.setDeviceStates(json);
        scene.setIsFavorite(true);
        sceneRepository.save(scene);
    }

    private void seedDefaultRule() {
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setName("Evening Motion Lights");
        rule.setDescription("If motion after 6pm, turn on the Living Room Light");
        rule.setTriggerCondition("motion AND hour >= 18");
        rule.setActionScript("turn_on(living-light-1)");
        rule.setIsEnabled(true);
        rule.setPriority(7);
        automationRuleRepository.save(rule);
    }

    private String guessRoomType(String name) {
        String normalized = name.toUpperCase(Locale.ROOT);
        if (normalized.contains("LIVING")) {
            return "LIVING_ROOM";
        }
        if (normalized.contains("KITCHEN")) {
            return "KITCHEN";
        }
        if (normalized.contains("BED")) {
            return "BEDROOM";
        }
        if (normalized.contains("BATH")) {
            return "BATHROOM";
        }
        if (normalized.contains("HALL")) {
            return "HALLWAY";
        }
        if (normalized.contains("YARD") || normalized.contains("OUT")) {
            return "OUTDOOR";
        }
        if (normalized.contains("DOOR")) {
            return "ENTRY";
        }
        return "ROOM";
    }
}
