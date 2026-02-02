package com.smarthome.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Automation Rule Entity - Represents an automation rule
 */
@Entity
@Table(name = "automation_rules")
public class AutomationRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "trigger_condition", length = 1000)
    private String triggerCondition; // e.g., "motion_detected AND time > 18:00"

    @Column(name = "action_script", length = 1000)
    private String actionScript; // e.g., "turn_on(living_room_lights)"

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "priority")
    private Integer priority = 5;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_triggered")
    private LocalDateTime lastTriggered;

    public AutomationRuleEntity() {}

    public AutomationRuleEntity(Long id,
                                String name,
                                String description,
                                String triggerCondition,
                                String actionScript,
                                Boolean isEnabled,
                                Integer priority,
                                LocalDateTime createdAt,
                                LocalDateTime lastTriggered) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.triggerCondition = triggerCondition;
        this.actionScript = actionScript;
        this.isEnabled = isEnabled;
        this.priority = priority;
        this.createdAt = createdAt;
        this.lastTriggered = lastTriggered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public String getActionScript() {
        return actionScript;
    }

    public void setActionScript(String actionScript) {
        this.actionScript = actionScript;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(LocalDateTime lastTriggered) {
        this.lastTriggered = lastTriggered;
    }
}
