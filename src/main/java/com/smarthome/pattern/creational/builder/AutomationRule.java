package com.smarthome.pattern.creational.builder;

import java.time.Instant;
import java.util.UUID;

public class AutomationRule {
    private final String id;
    private final String name;
    private final String trigger;
    private final String condition;
    private final String action;
    private final Instant createdAt;

    private AutomationRule(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.trigger = builder.trigger;
        this.condition = builder.condition;
        this.action = builder.action;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getCondition() {
        return condition;
    }

    public String getAction() {
        return action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private String id = "rule-" + UUID.randomUUID().toString().substring(0, 8);
        private String name;
        private String trigger;
        private String condition;
        private String action;
        private Instant createdAt = Instant.now();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder trigger(String trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public AutomationRule build() {
            return new AutomationRule(this);
        }
    }
}

