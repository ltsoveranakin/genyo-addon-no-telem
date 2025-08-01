package com.genyo.addon.events.world;

import net.minecraft.entity.Entity;

public class RemoveEntityEvent {

    private static final RemoveEntityEvent INSTANCE = new RemoveEntityEvent();

    public Entity entity;
    public Entity.RemovalReason removalReason;

    public static RemoveEntityEvent get(Entity entity, Entity.RemovalReason removalReason) {
        INSTANCE.entity = entity;
        INSTANCE.removalReason = removalReason;

        return INSTANCE;
    }

}
