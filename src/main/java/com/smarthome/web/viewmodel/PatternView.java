package com.smarthome.web.viewmodel;

import java.util.List;

public record PatternView(
        String id,
        String name,
        String category,
        String description,
        String httpMethod,
        String apiEndpoint,
        List<String> classes
) {}

