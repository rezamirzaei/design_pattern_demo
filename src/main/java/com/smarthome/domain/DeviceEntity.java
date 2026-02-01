package com.smarthome.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "devices")
public class DeviceEntity {
    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeviceType type;

    @Column(nullable = false)
    private String location;

    @Column(name = "is_on", nullable = false)
    private boolean on;

    @Column(nullable = false)
    private int ratedPowerWatts;

    @Column(nullable = false, length = 32)
    private String ecosystem;

    protected DeviceEntity() {}

    public DeviceEntity(
            String id,
            String name,
            DeviceType type,
            String location,
            boolean on,
            int ratedPowerWatts,
            String ecosystem
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.on = on;
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
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
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
        return type.getIcon() + " " + name + " • " + location;
    }
}
