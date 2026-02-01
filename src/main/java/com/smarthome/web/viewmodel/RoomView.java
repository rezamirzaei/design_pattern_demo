package com.smarthome.web.viewmodel;

import java.util.List;

public record RoomView(
        Long id,
        String name,
        String floor,
        String roomType,
        int deviceCount,
        List<DeviceView> devices
) {}

