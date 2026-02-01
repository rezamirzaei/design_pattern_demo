package com.smarthome.pattern.structural.decorator;

import com.smarthome.pattern.creational.factory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Concrete Decorator - Adds caching to device status queries
 */
public class CachingDecorator extends DeviceDecorator {
    private static final Logger log = LoggerFactory.getLogger(CachingDecorator.class);
    private String cachedStatus;
    private Instant cacheTime;
    private final Duration cacheDuration;
    private int cacheHits = 0;
    private int cacheMisses = 0;

    public CachingDecorator(Device device, Duration cacheDuration) {
        super(device);
        this.cacheDuration = cacheDuration;
    }

    public CachingDecorator(Device device) {
        this(device, Duration.ofSeconds(30)); // Default 30 second cache
    }

    @Override
    public String getStatus() {
        if (isCacheValid()) {
            cacheHits++;
            log.debug("[CACHE] Cache hit for device status");
            return cachedStatus;
        }

        cacheMisses++;
        log.debug("[CACHE] Cache miss - fetching fresh status");
        cachedStatus = super.getStatus();
        cacheTime = Instant.now();
        return cachedStatus;
    }

    @Override
    public void turnOn() {
        super.turnOn();
        invalidateCache();
    }

    @Override
    public void turnOff() {
        super.turnOff();
        invalidateCache();
    }

    @Override
    public void operate(String command) {
        super.operate(command);
        invalidateCache();
    }

    @Override
    public String getDeviceInfo() {
        return "[Cached] " + super.getDeviceInfo();
    }

    private boolean isCacheValid() {
        if (cachedStatus == null || cacheTime == null) {
            return false;
        }
        return Duration.between(cacheTime, Instant.now()).compareTo(cacheDuration) < 0;
    }

    public void invalidateCache() {
        cachedStatus = null;
        cacheTime = null;
        log.debug("[CACHE] Cache invalidated for device");
    }

    public String getCacheStats() {
        return String.format("Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.1f%%",
                cacheHits, cacheMisses,
                (cacheHits + cacheMisses) > 0 ?
                        (cacheHits * 100.0 / (cacheHits + cacheMisses)) : 0);
    }
}
