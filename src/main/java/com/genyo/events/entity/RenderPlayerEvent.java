package com.genyo.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class RenderPlayerEvent extends Cancellable {

    private static final RenderPlayerEvent INSTANCE = new RenderPlayerEvent();

    //
    public AbstractClientPlayerEntity entity;
    //
    public float yaw;
    public float pitch;

    /**
     * @param entity
     */
    public static RenderPlayerEvent get(AbstractClientPlayerEntity entity)
    {
        INSTANCE.entity = entity;

        return INSTANCE;
    }

}
