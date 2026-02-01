package com.smarthome.pattern.behavioral.mediator;

import java.util.Map;

public final class MediatorDemo {
    private MediatorDemo() {}

    public static Map<String, Object> motionScenario() {
        CentralHubMediator mediator = new CentralHubMediator();
        SmartLightColleague light = new SmartLightColleague("light-1");
        CameraColleague camera = new CameraColleague("camera-1");
        MotionSensorColleague sensor = new MotionSensorColleague("sensor-1");

        mediator.registerDevice(light.getDeviceId(), light);
        mediator.registerDevice(camera.getDeviceId(), camera);
        mediator.registerDevice(sensor.getDeviceId(), sensor);

        sensor.detectMotion();

        return Map.of(
                "pattern", "Mediator",
                "devicesRegistered", mediator.getDeviceCount(),
                "lightOn", light.isOn(),
                "cameraRecording", camera.isRecording(),
                "event", "MOTION_DETECTED"
        );
    }
}

