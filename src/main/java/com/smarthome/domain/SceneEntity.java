package com.smarthome.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Scene Entity - Represents a saved scene (snapshot of device states)
 */
@Entity
@Table(name = "scenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "device_states", length = 5000)
    private String deviceStates; // JSON snapshot of all device states

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;
}
