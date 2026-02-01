package com.smarthome.web.viewmodel;

import java.time.LocalDateTime;

public record AutomationRuleView(
        Long id,
        String name,
        String description,
        String triggerCondition,
        String actionScript,
        boolean isEnabled,
        int priority,
        LocalDateTime createdAt,
        LocalDateTime lastTriggered
) {}

