package com.genyo.addon.managers.combat;

import com.genyo.addon.systems.modules.movement.GenyoPhase;
import com.genyo.addon.systems.modules.movement.GenyoVelocity;
import com.genyo.addon.utils.player.RaycastUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PearlManager {

    private float[] lastThrownAngles;
    private Box pearlBB;

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (mc.player == null || !Modules.get().get(GenyoPhase.class).shouldRaytrace())
        {
            return;
        }

        if (event.packet instanceof PlayerPositionLookS2CPacket packet && lastThrownAngles != null)
        {
            BlockHitResult hitResult = (BlockHitResult) RaycastUtil.rayCast(3.0, lastThrownAngles);
            pearlBB = new Box(hitResult.getPos().subtract(0.4, 0.4, 0.4),
                hitResult.getPos().add(0.4, 0.4, 0.4));

            if (mc.world.getBlockState(hitResult.getBlockPos()).isAir())
            {
                return;
            }

            Vec3d pos = packet.change().position();
            if (!pearlBB.contains(pos.x, pos.y, pos.z))
            {
                event.cancel();
                mc.getNetworkHandler().getConnection().send(new TeleportConfirmC2SPacket(packet.teleportId()));
                mc.getNetworkHandler().getConnection().send(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(),
                    mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision));
            }
            lastThrownAngles = null;
        }
    }

    public void setLastThrownAngles(float[] lastThrownAngles)
    {
        this.lastThrownAngles = lastThrownAngles;
    }

}
