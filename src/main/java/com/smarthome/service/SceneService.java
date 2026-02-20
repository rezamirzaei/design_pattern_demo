package com.smarthome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.SceneEntity;
import com.smarthome.repository.DeviceRepository;
import com.smarthome.repository.SceneRepository;
import com.smarthome.web.viewmodel.DeviceView;
import com.smarthome.web.viewmodel.SceneView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Scene CRUD (save/apply device-state snapshots — Memento pattern integration).
 */
@Service
public class SceneService {

    private final SceneRepository sceneRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;

    public SceneService(SceneRepository sceneRepository,
                        DeviceRepository deviceRepository,
                        DeviceService deviceService,
                        ObjectMapper objectMapper) {
        this.sceneRepository = sceneRepository;
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SceneView> getScenes() {
        return sceneRepository.findAll().stream()
                .sorted(Comparator.comparing(SceneEntity::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toView)
                .toList();
    }

    @Transactional
    public SceneView createSceneSnapshot(String name, String description, boolean favorite) {
        String normalized = ServiceUtils.requireText(name, "Scene name is required");
        Map<String, Boolean> snapshot = captureCurrentDeviceStates();

        String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize device states: " + e.getMessage(), e);
        }

        SceneEntity scene = sceneRepository.findByName(normalized).orElseGet(SceneEntity::new);
        scene.setName(normalized);
        scene.setDescription(ServiceUtils.blankToNull(description));
        scene.setDeviceStates(json);
        scene.setIsFavorite(favorite);
        sceneRepository.save(scene);
        return toView(scene);
    }

    @Transactional
    public SceneView toggleSceneFavorite(Long sceneId) {
        if (sceneId == null) throw new IllegalArgumentException("Scene id is required");
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));
        scene.setIsFavorite(!Boolean.TRUE.equals(scene.getIsFavorite()));
        sceneRepository.save(scene);
        return toView(scene);
    }

    @Transactional
    public Map<String, Object> applySceneSnapshot(Long sceneId) {
        if (sceneId == null) throw new IllegalArgumentException("Scene id is required");
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));

        Map<String, Boolean> targetStates = parseDeviceStates(scene.getDeviceStates());
        if (targetStates.isEmpty()) {
            return Map.of("sceneId", sceneId, "sceneName", scene.getName(),
                    "message", "Scene has no device states to apply",
                    "timestamp", Instant.now().toString());
        }

        Map<String, Boolean> before = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn,
                        (a, b) -> a, LinkedHashMap::new));

        List<String> missing = new ArrayList<>();
        List<DeviceEntity> updated = new ArrayList<>();
        for (var entry : targetStates.entrySet()) {
            var maybe = deviceRepository.findById(entry.getKey());
            if (maybe.isEmpty()) { missing.add(entry.getKey()); continue; }
            DeviceEntity entity = maybe.get();
            entity.setOn(entry.getValue());
            var rt = deviceService.ensureRuntimeDevice(entity);
            if (entry.getValue()) rt.turnOn(); else rt.turnOff();
            updated.add(entity);
        }
        deviceRepository.saveAll(updated);

        Map<String, Boolean> after = deviceRepository.findAll().stream()
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn,
                        (a, b) -> a, LinkedHashMap::new));

        Map<String, Map<String, Boolean>> diffs = new LinkedHashMap<>();
        for (var e : after.entrySet()) {
            boolean b = before.getOrDefault(e.getKey(), false);
            if (b != e.getValue()) diffs.put(e.getKey(), Map.of("before", b, "after", e.getValue()));
        }

        return Map.of("sceneId", sceneId, "sceneName", scene.getName(),
                "missingDevices", missing, "changedDevices", diffs,
                "timestamp", Instant.now().toString());
    }

    @Transactional
    public Map<String, Object> deleteScene(Long sceneId) {
        if (sceneId == null) throw new IllegalArgumentException("Scene id is required");
        SceneEntity scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + sceneId));
        sceneRepository.delete(scene);
        return Map.of("sceneId", sceneId, "sceneName", scene.getName(), "deleted", true);
    }

    // ── Memento helpers ──────────────────────────────────────

    @Transactional
    public Map<String, Object> mementoSave(String sceneName) {
        SceneView sv = createSceneSnapshot(sceneName, "Memento snapshot", false);
        return Map.of("pattern", "Memento", "action", "save",
                "sceneName", sceneName, "sceneId", sv.id(),
                "deviceCount", sv.deviceCount(), "timestamp", Instant.now().toString());
    }

    @Transactional
    public Map<String, Object> mementoRestore(String snapshotId) {
        try {
            Long id = Long.parseLong(snapshotId);
            var result = applySceneSnapshot(id);
            return Map.of("pattern", "Memento", "action", "restore",
                    "snapshotId", snapshotId, "result", result,
                    "timestamp", Instant.now().toString());
        } catch (NumberFormatException e) {
            String normalized = ServiceUtils.requireText(snapshotId, "sceneName is required");
            SceneEntity scene = sceneRepository.findByName(normalized)
                    .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + normalized));
            var result = applySceneSnapshot(scene.getId());
            return Map.of("pattern", "Memento", "action", "restore",
                    "sceneName", normalized, "sceneId", scene.getId(),
                    "result", result);
        }
    }

    public Map<String, Object> listMementos() {
        List<Map<String, Object>> items = getScenes().stream()
                .<Map<String, Object>>map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.id()); m.put("name", s.name());
                    m.put("favorite", s.favorite()); m.put("deviceCount", s.deviceCount());
                    m.put("createdAt", s.createdAt() == null ? null : s.createdAt().toString());
                    return m;
                }).toList();
        return Map.of("pattern", "Memento", "count", items.size(), "scenes", items);
    }

    // ── private ──────────────────────────────────────────────

    private Map<String, Boolean> captureCurrentDeviceStates() {
        deviceService.syncDatabaseFromRuntime();
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getId, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::isOn,
                        (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, Boolean> parseDeviceStates(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            var parsed = objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Boolean>>() {});
            return parsed == null ? Map.of() : new LinkedHashMap<>(parsed);
        } catch (Exception ignored) { return Map.of(); }
    }

    private SceneView toView(SceneEntity s) {
        String states = Optional.ofNullable(s.getDeviceStates()).orElse("");
        int count = parseDeviceStates(states).size();
        return new SceneView(s.getId(), s.getName(), s.getDescription(),
                Boolean.TRUE.equals(s.getIsFavorite()), count, s.getCreatedAt(), states);
    }
}

