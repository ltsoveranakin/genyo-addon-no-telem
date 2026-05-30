package com.genyo.managers.player;

import com.genyo.events.network.PacketSneakingEvent;
import meteordevelopment.orbit.EventHandler;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MovementManager {

    private boolean packetSneaking;

    public void setMotionY(double y) {
        mc.player.setVelocity(mc.player.getVelocity().getX(), y, mc.player.getVelocity().getZ());
    }

    public void setMotionXZ(double x, double z) {
        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

    public void setMotionX(double x) {
        mc.player.setVelocity(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
    }

    public void setMotionZ(double z) {
        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
    }

    public void setPacketSneaking(final boolean packetSneaking) {
        this.packetSneaking = packetSneaking;
        mc.player.setSneaking(packetSneaking);
    }

    @EventHandler
    public void onPacketSneak(PacketSneakingEvent event) {
        event.setCancelled(packetSneaking);
    }

}
