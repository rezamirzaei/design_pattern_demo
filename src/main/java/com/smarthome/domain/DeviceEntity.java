package com.smarthome.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/**
 * Base Device Entity - Represents a smart device in the home
 */
@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEntity {
    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;

    private boolean isOn;

    private int ratedPowerWatts;

    private String ecosystem;

    public String getInfo() {
        return String.format("%s (%s) in %s", name, type, location);
    }
}
