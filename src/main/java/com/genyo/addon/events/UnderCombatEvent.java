package com.genyo.addon.events;

import net.minecraft.entity.player.PlayerEntity;

public class UnderCombatEvent {

    private static final UnderCombatEvent INSTANCE = new UnderCombatEvent();

    public PlayerEntity entity;

    public static UnderCombatEvent get(PlayerEntity entity) {
        INSTANCE.entity = entity;

        return INSTANCE;
    }

}
