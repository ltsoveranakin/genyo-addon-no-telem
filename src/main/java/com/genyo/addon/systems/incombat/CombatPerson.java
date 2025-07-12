package com.genyo.addon.systems.incombat;

import net.minecraft.entity.player.PlayerEntity;

public class CombatPerson {

    private volatile PlayerEntity player;
    private volatile String name;

    public CombatPerson(PlayerEntity player) {
        this.player = player;
        this.name = player.getName().getString();
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

}
