package com.smarthome.web.viewmodel;

import java.time.LocalDateTime;

public record SceneView(
        Long id,
        String name,
        String description,
        boolean isFavorite,
        int deviceCount,
        LocalDateTime createdAt,
        String deviceStates
) {}

