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
import java.util.Optional;
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
        ensureDemoDevices();
        registerAllDevices();

        if (roomRepository.count() == 0) {
            seedRoomsFromDeviceLocations();
        }

        ensureDefaultScenes();

        if (automationRuleRepository.count() == 0) {
            seedDefaultRule();
        }
    }

    private void ensureDemoDevices() {
        List<DeviceEntity> demoDevices = List.of(
                new DeviceEntity("living-light-1", "Living Room Light", DeviceType.LIGHT, "Living Room", false, 12, "LOCAL"),
                new DeviceEntity("living-tv", "Living Room TV", DeviceType.CAMERA, "Living Room", false, 40, "LOCAL"),
                new DeviceEntity("living-speaker", "Living Room Speaker", DeviceType.SENSOR, "Living Room", true, 5, "LOCAL"),

                new DeviceEntity("kitchen-light", "Kitchen Light", DeviceType.LIGHT, "Kitchen", false, 12, "LOCAL"),
                new DeviceEntity("kitchen-sensor", "Kitchen Sensor", DeviceType.SENSOR, "Kitchen", true, 1, "LOCAL"),

                new DeviceEntity("bed-light", "Bedroom Light", DeviceType.LIGHT, "Bedroom", false, 10, "LOCAL"),
                new DeviceEntity("bed-thermo", "Bedroom Thermostat", DeviceType.THERMOSTAT, "Bedroom", true, 5, "LOCAL"),

                new DeviceEntity("front-camera", "Front Yard Camera", DeviceType.CAMERA, "Front Yard", true, 8, "LOCAL"),
                new DeviceEntity("main-lock", "Front Door Lock", DeviceType.LOCK, "Hallway", true, 2, "LOCAL"),
                new DeviceEntity("sensor-1", "Door Sensor", DeviceType.SENSOR, "Front Door", true, 1, "LOCAL"),

                new DeviceEntity("garage-light", "Garage Light", DeviceType.LIGHT, "Garage", false, 12, "LOCAL"),
                new DeviceEntity("garden-sensor", "Garden Sensor", DeviceType.SENSOR, "Garden", true, 1, "LOCAL")
        );

        for (DeviceEntity device : demoDevices) {
            if (device.getId() == null || device.getId().isBlank()) {
                continue;
            }
            if (deviceRepository.existsById(device.getId())) {
                continue;
            }
            deviceRepository.save(device);
        }
    }

    private void registerAllDevices() {
        HomeController controller = HomeController.INSTANCE;
        for (DeviceEntity entity : deviceRepository.findAll()) {
            if (entity.getId() == null || entity.getId().isBlank()) {
                continue;
            }
            DeviceType type = Optional.ofNullable(entity.getType()).orElse(DeviceType.LIGHT);
            Device runtime = switch (type) {
                case LIGHT -> new SmartLight(entity.getName(), entity.getLocation());
                case THERMOSTAT -> new SmartThermostat(entity.getName(), entity.getLocation());
                case CAMERA -> new SmartCamera(entity.getName(), entity.getLocation());
                case LOCK -> new SmartLock(entity.getName(), entity.getLocation());
                case SENSOR -> new SmartThingsSensor(entity.getName(), entity.getLocation());
            };
            register(controller, entity.getId(), runtime, entity.isOn());
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
            created.setFloor(guessFloor(location));
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

    private void ensureDefaultScenes() {
        ensureScene(
                "Demo Snapshot",
                "Seeded snapshot of current device states",
                true,
                Map.of()
        );

        ensureScene(
                "Morning Default",
                "Preset morning scene (lights on, security relaxed)",
                true,
                Map.of(
                        "living-light-1", true,
                        "kitchen-light", true,
                        "bed-light", false,
                        "main-lock", false
                )
        );

        ensureScene(
                "Night Default",
                "Preset night scene (lights off, security armed)",
                false,
                Map.of(
                        "living-light-1", false,
                        "kitchen-light", false,
                        "bed-light", false,
                        "main-lock", true
                )
        );
    }

    private void ensureScene(String name, String description, boolean favorite, Map<String, Boolean> overrides) {
        if (sceneRepository.findByName(name).isPresent()) {
            return;
        }

        Map<String, Boolean> deviceStates = new LinkedHashMap<>();
        deviceRepository.findAll().stream()
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                .forEach(device -> deviceStates.put(device.getId(), device.isOn()));

        if (overrides != null && !overrides.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : overrides.entrySet()) {
                if (deviceStates.containsKey(entry.getKey())) {
                    deviceStates.put(entry.getKey(), entry.getValue());
                }
            }
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(deviceStates);
        } catch (Exception e) {
            json = "{}";
        }

        SceneEntity scene = new SceneEntity();
        scene.setName(name);
        scene.setDescription(description);
        scene.setDeviceStates(json);
        scene.setIsFavorite(favorite);
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

    private String guessFloor(String name) {
        String normalized = name.toUpperCase(Locale.ROOT);
        if (normalized.contains("YARD")
                || normalized.contains("OUT")
                || normalized.contains("GARAGE")
                || normalized.contains("GARDEN")) {
            return "0";
        }
        if (normalized.contains("HALL")
                || normalized.contains("ENTRY")
                || normalized.contains("DOOR")) {
            return "0";
        }
        return "1";
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
