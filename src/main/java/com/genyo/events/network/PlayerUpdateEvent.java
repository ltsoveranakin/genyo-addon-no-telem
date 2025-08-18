package com.genyo.events.network;

import com.genyo.events.StageEvent;

public class PlayerUpdateEvent extends StageEvent {

    private static final PlayerUpdateEvent INSTANCE = new PlayerUpdateEvent();

    public float yaw;
    public float pitch;

    public static PlayerUpdateEvent get(float yaw, float pitch) {
        INSTANCE.yaw = yaw;
        INSTANCE.pitch = pitch;

        return INSTANCE;
    }

}
