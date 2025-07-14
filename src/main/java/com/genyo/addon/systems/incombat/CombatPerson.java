package com.genyo.addon.systems.incombat;

import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.entity.player.PlayerEntity;

public class CombatPerson {

    private volatile PlayerEntity player;
    private volatile String name;
    private volatile boolean wasFriend;
    private volatile Friend friend;

    public CombatPerson(PlayerEntity player) {
        this.player = player;
        name = player.getName().getString();
        friend = null;
        wasFriend = Friends.get().isFriend(player);

        if (wasFriend) this.friend = Friends.get().get(player);
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public boolean wasFriendB() {
        return wasFriend;
    }

    public Friend getFriend() {
        return friend;
    }

}
