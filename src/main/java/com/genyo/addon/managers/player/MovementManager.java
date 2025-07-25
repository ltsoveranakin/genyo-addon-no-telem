package com.genyo.addon.managers.player;

import com.genyo.addon.events.network.PacketSneakingEvent;
import com.genyo.addon.managers.Managers;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;

public class MovementManager {

    private boolean packetSneaking;

    /**
     * @param y
     */
    public void setMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().getX(), y, mc.player.getVelocity().getZ());
    }

    /**
     * @param x
     * @param z
     */
    public void setMotionXZ(double x, double z)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

    public void setMotionX(double x)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
    }

    public void setMotionZ(double z)
    {
        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
    }


    public void setPacketSneaking(final boolean packetSneaking)
    {
        this.packetSneaking = packetSneaking;
        if (packetSneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, PRESS_SHIFT_KEY));
        }
        else
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, RELEASE_SHIFT_KEY));
        }
    }

    @EventHandler
    public void onPacketSneak(PacketSneakingEvent event)
    {
        event.setCancelled(packetSneaking);
    }

}
