package com.smarthome.pattern.behavioral.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * INTERPRETER PATTERN
 *
 * Intent: Given a language, define a representation for its grammar along with
 * an interpreter that uses the representation to interpret sentences in the language.
 *
 * Smart Home Application: A simple domain-specific language for automation rules.
 * Rules like "IF motion AND time > 18:00 THEN lights ON" can be parsed and executed.
 */

/**
 * Context - holds variables for interpretation
 */
public class InterpreterContext {
    private final Map<String, Boolean> booleanVariables = new HashMap<>();
    private final Map<String, Integer> integerVariables = new HashMap<>();
    private final Map<String, String> stringVariables = new HashMap<>();

    public void setBoolean(String name, boolean value) {
        booleanVariables.put(name, value);
    }

    public boolean getBoolean(String name) {
        return booleanVariables.getOrDefault(name, false);
    }

    public void setInteger(String name, int value) {
        integerVariables.put(name, value);
    }

    public int getInteger(String name) {
        return integerVariables.getOrDefault(name, 0);
    }

    public void setString(String name, String value) {
        stringVariables.put(name, value);
    }

    public String getString(String name) {
        return stringVariables.getOrDefault(name, "");
    }
}

/**
 * Abstract Expression interface
 */
interface Expression {
    boolean interpret(InterpreterContext context);
}

/**
 * Terminal Expression - Boolean variable
 */
class BooleanExpression implements Expression {
    private final String variableName;

    public BooleanExpression(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        return context.getBoolean(variableName);
    }
}

/**
 * Terminal Expression - Integer comparison
 */
class ComparisonExpression implements Expression {
    private final String variableName;
    private final String operator;
    private final int value;

    public ComparisonExpression(String variableName, String operator, int value) {
        this.variableName = variableName;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        int varValue = context.getInteger(variableName);
        return switch (operator) {
            case ">" -> varValue > value;
            case "<" -> varValue < value;
            case ">=" -> varValue >= value;
            case "<=" -> varValue <= value;
            case "==" -> varValue == value;
            case "!=" -> varValue != value;
            default -> false;
        };
    }
}

/**
 * Terminal Expression - String equality
 */
class StringEqualsExpression implements Expression {
    private final String variableName;
    private final String expectedValue;

    public StringEqualsExpression(String variableName, String expectedValue) {
        this.variableName = variableName;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        return expectedValue.equals(context.getString(variableName));
    }
}

/**
 * Non-terminal Expression - AND
 */
class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        return left.interpret(context) && right.interpret(context);
    }
}

/**
 * Non-terminal Expression - OR
 */
class OrExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        return left.interpret(context) || right.interpret(context);
    }
}

/**
 * Non-terminal Expression - NOT
 */
class NotExpression implements Expression {
    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean interpret(InterpreterContext context) {
        return !expression.interpret(context);
    }
}

/**
 * Rule Interpreter - Parses and executes automation rule expressions
 */
class RuleInterpreter {
    private static final Logger log = LoggerFactory.getLogger(RuleInterpreter.class);

    /**
     * Parse a simple rule expression
     * Supports: variable, variable OP value, expr AND expr, expr OR expr, NOT expr
     */
    public Expression parse(String rule) {
        rule = rule.trim();
        log.debug("Parsing rule: {}", rule);

        // Handle NOT
        if (rule.startsWith("NOT ")) {
            return new NotExpression(parse(rule.substring(4)));
        }

        // Handle AND (lowest precedence)
        int andIndex = rule.indexOf(" AND ");
        if (andIndex > 0) {
            return new AndExpression(
                parse(rule.substring(0, andIndex)),
                parse(rule.substring(andIndex + 5))
            );
        }

        // Handle OR
        int orIndex = rule.indexOf(" OR ");
        if (orIndex > 0) {
            return new OrExpression(
                parse(rule.substring(0, orIndex)),
                parse(rule.substring(orIndex + 4))
            );
        }

        // Handle comparison operators
        for (String op : new String[]{">=", "<=", "==", "!=", ">", "<"}) {
            int opIndex = rule.indexOf(op);
            if (opIndex > 0) {
                String varName = rule.substring(0, opIndex).trim();
                String valueStr = rule.substring(opIndex + op.length()).trim();
                try {
                    int value = Integer.parseInt(valueStr);
                    return new ComparisonExpression(varName, op, value);
                } catch (NumberFormatException e) {
                    // It's a string comparison
                    return new StringEqualsExpression(varName, valueStr);
                }
            }
        }

        // Handle string equality with '='
        int eqIndex = rule.indexOf("=");
        if (eqIndex > 0 && (eqIndex == 0 || rule.charAt(eqIndex - 1) != '!'
                && rule.charAt(eqIndex - 1) != '>' && rule.charAt(eqIndex - 1) != '<')) {
            String varName = rule.substring(0, eqIndex).trim();
            String value = rule.substring(eqIndex + 1).trim().replace("'", "").replace("\"", "");
            return new StringEqualsExpression(varName, value);
        }

        // Simple boolean variable
        return new BooleanExpression(rule);
    }

    /**
     * Evaluate a rule against a context
     */
    public boolean evaluate(String rule, InterpreterContext context) {
        Expression expression = parse(rule);
        boolean result = expression.interpret(context);
        log.info("Rule '{}' evaluated to: {}", rule, result);
        return result;
    }
}

/**
 * Automation Rule with condition and action
 */
class AutomationRuleExpression {
    private static final Logger log = LoggerFactory.getLogger(AutomationRuleExpression.class);
    private final String name;
    private final String condition;
    private final String action;
    private final RuleInterpreter interpreter;

    public AutomationRuleExpression(String name, String condition, String action) {
        this.name = name;
        this.condition = condition;
        this.action = action;
        this.interpreter = new RuleInterpreter();
    }

    public boolean evaluate(InterpreterContext context) {
        return interpreter.evaluate(condition, context);
    }

    public void execute(InterpreterContext context) {
        if (evaluate(context)) {
            log.info("Rule '{}' triggered! Executing action: {}", name, action);
            executeAction(action, context);
        } else {
            log.debug("Rule '{}' condition not met", name);
        }
    }

    private void executeAction(String action, InterpreterContext context) {
        // Parse and execute action
        // Format: "device.command" or "SET variable value"
        if (action.startsWith("SET ")) {
            String[] parts = action.substring(4).split(" ", 2);
            if (parts.length == 2) {
                context.setString(parts[0], parts[1]);
                log.info("SET {} = {}", parts[0], parts[1]);
            }
        } else {
            log.info("Executing action: {}", action);
        }
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public String getAction() {
        return action;
    }
}
