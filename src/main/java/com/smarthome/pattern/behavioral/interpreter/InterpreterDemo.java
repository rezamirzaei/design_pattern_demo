package com.smarthome.pattern.behavioral.interpreter;

import java.util.Map;

public final class InterpreterDemo {
    private InterpreterDemo() {}

    public static Map<String, Object> evaluate(String rule, Map<String, Object> variables) {
        InterpreterContext context = new InterpreterContext();
        if (variables != null) {
            variables.forEach((k, v) -> {
                if (v instanceof Boolean b) {
                    context.setBoolean(k, b);
                } else if (v instanceof Number n) {
                    context.setInteger(k, n.intValue());
                } else if (v != null) {
                    context.setString(k, v.toString());
                }
            });
        }

        String effectiveRule = (rule == null || rule.isBlank()) ? "motion AND hour >= 18" : rule;
        RuleInterpreter interpreter = new RuleInterpreter();
        boolean result = interpreter.evaluate(effectiveRule, context);

        return Map.of(
                "pattern", "Interpreter",
                "rule", effectiveRule,
                "variables", variables == null ? Map.of() : variables,
                "result", result
        );
    }
}

