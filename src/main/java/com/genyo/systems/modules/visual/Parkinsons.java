package com.genyo.systems.modules.visual;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Parkinsons extends GenyoModule {

    public Parkinsons() {
        super(Genyo.VISUAL, "parkinsons", "Funnier freecam");
    }

    public static boolean shouldFreeze = false;

    Vec3d storedPos;
    float storedYaw, storedPitch;

    @Override
    public void onActivate() {
        shouldFreeze = true;

        storedPos = mc.player.getEntityPos();
        storedYaw = mc.player.getYaw();
        storedPitch = mc.player.getPitch();

        OtherClientPlayerEntity freecamEntity = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());

        freecamEntity.copyPositionAndRotation(mc.player);
        freecamEntity.setYaw(mc.player.getYaw());
        freecamEntity.setPitch(mc.player.getPitch());
        freecamEntity.setNoGravity(true);
        freecamEntity.noClip = true;
        freecamEntity.setOnGround(false);

        mc.setCameraEntity(freecamEntity);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        Entity camera = mc.getCameraEntity();

        Vec3d forward = Vec3d.fromPolar(0, camera.getYaw()).normalize();
        Vec3d right = new Vec3d(-forward.z, 0, forward.x).normalize();
        Vec3d velocity = Vec3d.ZERO;

        if (mc.options.forwardKey.isPressed()) velocity = velocity.add(forward);
        if (mc.options.backKey.isPressed()) velocity = velocity.subtract(forward);
        if (mc.options.leftKey.isPressed()) velocity = velocity.subtract(right);
        if (mc.options.rightKey.isPressed()) velocity = velocity.add(right);
        if (mc.options.jumpKey.isPressed()) velocity = velocity.add(0, 1, 0);
        if (mc.options.sneakKey.isPressed()) velocity = velocity.add(0, -1, 0);

        if (velocity.lengthSquared() > 0) {
            velocity = velocity.normalize().multiply(1);
            Vec3d pos = camera.getEntityPos();

            camera.setPos(pos.x + velocity.x, pos.y + velocity.y, pos.z + velocity.z);
        }

//        Vec3d cameraPos = camera.getEntityPos().add(velocity.multiply(speed.getValue()));
//        camera.setPos(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ());
    }

    @Override
    public void onDeactivate() {
        shouldFreeze = false;

        mc.setCameraEntity(mc.player);

        mc.player.updatePosition(storedPos.x, storedPos.y, storedPos.z);
        mc.player.setYaw(storedYaw);
        mc.player.setPitch(storedPitch);
    }

}
