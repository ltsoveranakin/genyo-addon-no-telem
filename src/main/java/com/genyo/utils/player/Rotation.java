package com.genyo.utils.player;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Rotation {

    private static final Rotation INSTANCE = new Rotation();

    public static Rotation get() {
        return INSTANCE;
    }

    // This is only required by grim because of rotation movement checks
    public void setRotationSilentSync()
    {
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        //setRotation(new Rotation(MAX_VALUE, yaw, pitch, true));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
    }

}
