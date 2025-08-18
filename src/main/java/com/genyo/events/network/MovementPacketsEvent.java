package com.genyo.events.network;

import meteordevelopment.meteorclient.events.Cancellable;

public class MovementPacketsEvent extends Cancellable {

    private static final MovementPacketsEvent INSTANCE = new MovementPacketsEvent();

    //
    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;

    public static MovementPacketsEvent get(double x, double y, double z, float yaw, float pitch, boolean onGround)
    {
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        INSTANCE.yaw = yaw;
        INSTANCE.pitch = pitch;
        INSTANCE.onGround = onGround;

        return INSTANCE;
    }

}
