package com.genyo.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class PushEntityEvent extends Cancellable {

    private static final PushEntityEvent INSTANCE = new PushEntityEvent();

    public Entity pushed, pusher;

    public static PushEntityEvent get(Entity pushed, Entity pusher) {
        INSTANCE.pushed = pushed;
        INSTANCE.pusher = pusher;

        return INSTANCE;
    }

}
