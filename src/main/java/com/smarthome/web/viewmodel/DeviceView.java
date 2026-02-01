package com.smarthome.web.viewmodel;

import com.smarthome.domain.DeviceType;

public record DeviceView(
        String id,
        String info,
        DeviceType type,
        String location,
        boolean isOn,
        int power
) {}

