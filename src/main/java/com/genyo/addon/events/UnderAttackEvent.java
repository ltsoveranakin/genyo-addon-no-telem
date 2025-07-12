package com.genyo.addon.events;

import net.minecraft.entity.player.PlayerEntity;

public class UnderAttackEvent {

    private static final UnderAttackEvent INSTANCE = new UnderAttackEvent();

    public PlayerEntity entity;

    public static UnderAttackEvent get(PlayerEntity entity) {
        INSTANCE.entity = entity;

        return INSTANCE;
    }

}
