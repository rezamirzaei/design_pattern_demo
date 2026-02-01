package com.smarthome.web.viewmodel;

import com.smarthome.domain.HomeMode;

public record StatusView(
        String systemStatus,
        HomeMode homeMode,
        int activeDevices
) {}

