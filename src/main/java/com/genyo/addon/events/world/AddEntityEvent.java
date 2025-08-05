package com.genyo.addon.events.world;

import net.minecraft.entity.Entity;

public class AddEntityEvent {

    private static final AddEntityEvent INSTANCE = new AddEntityEvent();
    public Entity entity;

    public static AddEntityEvent get(Entity entity) {
        INSTANCE.entity = entity;

        return INSTANCE;
    }

}
