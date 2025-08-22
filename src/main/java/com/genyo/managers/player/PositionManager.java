package com.genyo.managers.player;

import com.genyo.managers.Managers;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PositionManager {

    //
    private double x, y, z;
    private BlockPos blockPos;
    //
    private boolean sneaking, sprinting;
    //
    private boolean onGround;
    private boolean horizontalCollision;

    /**
     * @param vec3d
     */
    public void setPosition(Vec3d vec3d)
    {
        setPosition(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        setPositionClient(x, y, z);
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, isOnGround(), isCollidingHorizontally()));
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(double x, double y, double z)
    {
        setPositionClient(x, y, z);
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, isOnGround(), isCollidingHorizontally()));
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void setPositionClient(double x, double y, double z)
    {
        if (mc.player.isRiding())
        {
            mc.player.getVehicle().setPosition(x, y, z);
            return;
        }
        mc.player.setPosition(x, y, z);
    }

    /**
     * @param x
     * @param z
     */
    public void setPositionXZ(double x, double z)
    {
        setPosition(x, y, z);
    }

    /**
     * @param y
     */
    public void setPositionY(double y)
    {
        setPosition(x, y, z);
    }

    /**
     * @return
     */
    public Vec3d getPos()
    {
        return new Vec3d(getX(), getY(), getZ());
    }

    /**
     * @return
     */
    public Vec3d getEyePos()
    {
        return getPos().add(0.0, mc.player.getStandingEyeHeight(), 0.0);
    }

    /**
     * @param tickDelta
     * @return
     */
    public final Vec3d getCameraPosVec(float tickDelta)
    {
        double d = MathHelper.lerp(tickDelta, mc.player.lastX, getX());
        double e = MathHelper.lerp(tickDelta, mc.player.lastY, getY())
            + (double) mc.player.getStandingEyeHeight();
        double f = MathHelper.lerp(tickDelta, mc.player.lastZ, getZ());
        return new Vec3d(d, e, f);
    }

    /**
     * @param entity
     * @return
     */
    public double squaredDistanceTo(Entity entity)
    {
        float f = (float) (getX() - entity.getX());
        float g = (float) (getY() - entity.getY());
        float h = (float) (getZ() - entity.getZ());
        return MathHelper.squaredMagnitude(f, g, h);
    }

    /**
     * @param entity
     * @return
     */
    public double squaredReachDistanceTo(Entity entity)
    {
        Vec3d cam = getCameraPosVec(1.0f);
        float f = (float) (cam.getX() - entity.getX());
        float g = (float) (cam.getY() - entity.getY());
        float h = (float) (cam.getZ() - entity.getZ());
        return MathHelper.squaredMagnitude(f, g, h);
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        if (mc.player != null && mc.world != null)
        {
            if (event.packet instanceof PlayerMoveC2SPacket packet)
            {
                onGround = packet.isOnGround();
                horizontalCollision = packet.horizontalCollision();
                if (packet.changesPosition())
                {
                    x = packet.getX(x);
                    y = packet.getY(y);
                    z = packet.getZ(z);
                    blockPos = BlockPos.ofFloored(x, y, z);
                }
            }
            else if (event.packet instanceof ClientCommandC2SPacket packet)
            {
                switch (packet.getMode())
                {
                    case START_SPRINTING -> sprinting = true;
                    case STOP_SPRINTING -> sprinting = false;
                    case PRESS_SHIFT_KEY -> sneaking = true;
                    case RELEASE_SHIFT_KEY -> sneaking = false;
                }
            }
        }
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    /**
     * @return
     */
    public BlockPos getBlockPos()
    {
        return blockPos;
    }

    /**
     * @return
     */
    public boolean isSneaking()
    {
        return sneaking;
    }

    /**
     * @return
     */
    public boolean isSprinting()
    {
        return sprinting;
    }

    /**
     * @return
     */
    public boolean isOnGround()
    {
        return onGround;
    }

    public boolean isCollidingHorizontally() {
        return horizontalCollision;
    }
}
