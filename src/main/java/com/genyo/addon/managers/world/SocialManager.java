package com.genyo.addon.managers.world;

import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.entity.player.PlayerEntity;

public class SocialManager {

    public boolean isFriend(PlayerEntity player) {
        return Friends.get().isFriend(player);
    }

    public boolean isFriend(String name) {
        if (Friends.get().get(name) != null) return true;
        return false;
    }
}
