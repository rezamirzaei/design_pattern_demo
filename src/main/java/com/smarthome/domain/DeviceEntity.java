package com.smarthome.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/**
 * Base Device Entity - Represents a smart device in the home
 */
@Entity
@Table(name = "devices")
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

    public DeviceEntity() {}

    public DeviceEntity(String id,
                        String name,
                        DeviceType type,
                        String location,
                        boolean isOn,
                        int ratedPowerWatts,
                        String ecosystem) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.isOn = isOn;
        this.ratedPowerWatts = ratedPowerWatts;
        this.ecosystem = ecosystem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getRatedPowerWatts() {
        return ratedPowerWatts;
    }

    public void setRatedPowerWatts(int ratedPowerWatts) {
        this.ratedPowerWatts = ratedPowerWatts;
    }

    public String getEcosystem() {
        return ecosystem;
    }

    public void setEcosystem(String ecosystem) {
        this.ecosystem = ecosystem;
    }

    public String getInfo() {
        return String.format("%s (%s) in %s", name, type, location);
    }
}
