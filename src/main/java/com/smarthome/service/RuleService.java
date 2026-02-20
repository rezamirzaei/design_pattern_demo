package com.smarthome.service;

import com.smarthome.domain.AutomationRuleEntity;
import com.smarthome.domain.DeviceEntity;
import com.smarthome.domain.HomeMode;
import com.smarthome.domain.SceneEntity;
import com.smarthome.pattern.behavioral.interpreter.InterpreterDemo;
import com.smarthome.pattern.creational.builder.AutomationRule;
import com.smarthome.repository.AutomationRuleRepository;
import com.smarthome.repository.SceneRepository;
import com.smarthome.web.viewmodel.AutomationRuleView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages automation rules: CRUD, evaluation (Interpreter), and action execution (Builder).
 */
@Service
public class RuleService {

    private final AutomationRuleRepository ruleRepository;
    private final SceneRepository sceneRepository;
    private final DeviceService deviceService;
    private final List<AutomationRule> builderRules = new CopyOnWriteArrayList<>();

    private static final Pattern ACTION_PATTERN =
            Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(\\s*(.*?)\\s*\\)\\s*$");

    public RuleService(AutomationRuleRepository ruleRepository,
                       SceneRepository sceneRepository,
                       DeviceService deviceService) {
        this.ruleRepository = ruleRepository;
        this.sceneRepository = sceneRepository;
        this.deviceService = deviceService;
    }

    @Transactional(readOnly = true)
    public List<AutomationRuleView> getAutomationRules() {
        return ruleRepository.findAll().stream()
                .sorted(Comparator.comparing(AutomationRuleEntity::getPriority,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(AutomationRuleEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toView)
                .toList();
    }

    @Transactional
    public AutomationRuleView createAutomationRule(String name, String description,
                                                   String triggerCondition, String actionScript,
                                                   Integer priority) {
        String n = ServiceUtils.requireText(name, "Rule name is required");
        String cond = ServiceUtils.requireText(triggerCondition, "Trigger condition is required");
        String act = ServiceUtils.requireText(actionScript, "Action script is required");

        // Builder pattern demo
        builderRules.add(new AutomationRule.Builder()
                .name(n).trigger("expression").condition(cond).action(act).build());

        AutomationRuleEntity entity = new AutomationRuleEntity();
        entity.setName(n);
        entity.setDescription(ServiceUtils.blankToNull(description));
        entity.setTriggerCondition(cond);
        entity.setActionScript(act);
        entity.setIsEnabled(true);
        entity.setPriority(priority == null ? 5 : priority);
        ruleRepository.save(entity);
        return toView(entity);
    }

    @Transactional
    public AutomationRuleView toggleAutomationRule(Long ruleId) {
        if (ruleId == null) throw new IllegalArgumentException("Rule id is required");
        AutomationRuleEntity e = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        e.setIsEnabled(!Boolean.TRUE.equals(e.getIsEnabled()));
        ruleRepository.save(e);
        return toView(e);
    }

    @Transactional
    public Map<String, Object> deleteAutomationRule(Long ruleId) {
        if (ruleId == null) throw new IllegalArgumentException("Rule id is required");
        AutomationRuleEntity e = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        ruleRepository.delete(e);
        return Map.of("ruleId", ruleId, "ruleName", e.getName(), "deleted", true);
    }

    @Transactional
    public Map<String, Object> runAutomationRule(Long ruleId, Map<String, Object> variables,
                                                 boolean executeActions) {
        if (ruleId == null) throw new IllegalArgumentException("Rule id is required");
        AutomationRuleEntity entity = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        Map<String, Object> interpreter = InterpreterDemo.evaluate(entity.getTriggerCondition(), variables);
        boolean matched = Boolean.TRUE.equals(interpreter.get("result"));

        List<Map<String, Object>> actions = new ArrayList<>();
        boolean executed = false;
        if (matched && executeActions && Boolean.TRUE.equals(entity.getIsEnabled())) {
            actions = executeActionScript(entity.getActionScript());
            executed = true;
            entity.setLastTriggered(LocalDateTime.now());
            ruleRepository.save(entity);
        }

        return Map.of("rule", toView(entity), "variables", variables == null ? Map.of() : variables,
                "matched", matched, "executeActions", executeActions,
                "executed", executed, "actions", actions, "interpreter", interpreter);
    }

    public AutomationRule buildAutomationRule(String name, String trigger,
                                              String condition, String action) {
        AutomationRule rule = new AutomationRule.Builder()
                .name(name).trigger(trigger).condition(condition).action(action).build();
        builderRules.add(rule);
        return rule;
    }

    // ── Action script engine ─────────────────────────────────

    private List<Map<String, Object>> executeActionScript(String actionScript) {
        String script = ServiceUtils.blankToNull(actionScript);
        if (script == null) return List.of(Map.of("status", "noop", "message", "No action script"));

        List<Map<String, Object>> results = new ArrayList<>();
        for (String raw : script.split("[;\\n\\r]+")) {
            String stmt = raw == null ? "" : raw.trim();
            if (stmt.isBlank()) continue;

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("statement", stmt);

            Matcher m = ACTION_PATTERN.matcher(stmt);
            if (!m.matches()) {
                result.put("status", "ignored");
                result.put("message", "Unsupported format. Use fn(arg)");
                results.add(result);
                continue;
            }

            String fn = m.group(1).trim().toLowerCase(Locale.ROOT);
            String arg = ServiceUtils.stripQuotes(m.group(2).trim());
            try {
                switch (fn) {
                    case "turn_on", "on" -> { result.put("status", "ok"); result.put("device", deviceService.controlDevice(arg, true)); }
                    case "turn_off", "off" -> { result.put("status", "ok"); result.put("device", deviceService.controlDevice(arg, false)); }
                    case "toggle" -> {
                        DeviceEntity de = deviceService.findOrThrow(arg);
                        result.put("status", "ok");
                        result.put("device", deviceService.controlDevice(arg, !de.isOn()));
                    }
                    case "room_on" -> { result.put("status", "ok"); result.put("devices", deviceService.controlRoom(arg, true)); }
                    case "room_off" -> { result.put("status", "ok"); result.put("devices", deviceService.controlRoom(arg, false)); }
                    case "mode", "set_mode" -> {
                        HomeMode mode = HomeMode.valueOf(arg.trim().toUpperCase(Locale.ROOT));
                        result.put("status", "ok"); result.put("mode", deviceService.setHomeMode(mode));
                    }
                    case "scene", "activate_scene", "apply_scene" -> {
                        SceneEntity scene = sceneRepository.findByName(arg)
                                .orElseThrow(() -> new IllegalArgumentException("Scene not found: " + arg));
                        result.put("status", "ok"); result.put("scene", arg);
                    }
                    default -> { result.put("status", "ignored"); result.put("message", "Unknown action: " + fn); }
                }
            } catch (Exception ex) {
                result.put("status", "error"); result.put("message", ex.getMessage());
            }
            results.add(result);
        }
        return results;
    }

    private AutomationRuleView toView(AutomationRuleEntity r) {
        Integer p = r.getPriority();
        return new AutomationRuleView(r.getId(), r.getName(), r.getDescription(),
                r.getTriggerCondition(), r.getActionScript(),
                Boolean.TRUE.equals(r.getIsEnabled()),
                p == null ? 0 : p, r.getCreatedAt(), r.getLastTriggered());
    }
}

