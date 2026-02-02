package com.smarthome.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Scene Entity - Represents a saved scene (snapshot of device states)
 */
@Entity
@Table(name = "scenes")
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

    public SceneEntity() {}

    public SceneEntity(Long id,
                       String name,
                       String description,
                       String deviceStates,
                       LocalDateTime createdAt,
                       Boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deviceStates = deviceStates;
        this.createdAt = createdAt;
        this.isFavorite = isFavorite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceStates() {
        return deviceStates;
    }

    public void setDeviceStates(String deviceStates) {
        this.deviceStates = deviceStates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean favorite) {
        isFavorite = favorite;
    }
}
