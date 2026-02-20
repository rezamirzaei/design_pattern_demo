package com.smarthome.service;

import java.util.Locale;

/**
 * Shared utility methods for service classes.
 */
public final class ServiceUtils {

    private ServiceUtils() {}

    public static String requireText(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException(message);
        return normalized;
    }

    public static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static String stripQuotes(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.length() >= 2
                && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    public static String toTitleCase(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isBlank()) return raw;
        String[] parts = raw.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) out.append(part.substring(1));
        }
        return out.toString();
    }
}

