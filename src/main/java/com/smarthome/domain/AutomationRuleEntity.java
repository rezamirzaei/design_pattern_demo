package com.smarthome.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Automation Rule Entity - Represents an automation rule
 */
@Entity
@Table(name = "automation_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
