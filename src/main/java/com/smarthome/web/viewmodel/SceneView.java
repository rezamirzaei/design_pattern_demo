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
) {
    // Add compatibility method since 'isFavorite' usually generates 'isFavorite()' but Service might be calling 'favorite()' or vice versa
    public boolean favorite() {
        return isFavorite;
    }
}
