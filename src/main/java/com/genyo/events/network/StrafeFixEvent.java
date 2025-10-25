package com.genyo.events.network;

import meteordevelopment.meteorclient.events.Cancellable;

public class StrafeFixEvent extends Cancellable {

    private static final StrafeFixEvent INSTANCE = new StrafeFixEvent();

    public float yaw, pitch;

    public static StrafeFixEvent get(float yaw, float pitch) {
        INSTANCE.yaw = yaw;
        INSTANCE.pitch = pitch;

        return INSTANCE;
    }

}
