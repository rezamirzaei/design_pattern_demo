package com.smarthome.web.viewmodel;

import java.util.List;

public record PatternParameter(
        String name,
        String label,
        String type, // text, select, hidden
        List<String> options,
        String defaultValue
) {}
