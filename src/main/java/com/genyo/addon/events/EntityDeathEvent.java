package com.genyo.addon.events;

import net.minecraft.entity.LivingEntity;

public class EntityDeathEvent {

    private static final EntityDeathEvent INSTANCE = new EntityDeathEvent();

    public LivingEntity entity;

    public EntityDeathEvent get(LivingEntity entity) {
        INSTANCE.entity = entity;

        return INSTANCE;
    }

}
