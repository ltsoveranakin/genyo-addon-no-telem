package com.genyo.systems.modules.movement;

import com.genyo.Genyo;
import com.genyo.events.entity.player.PushEntityEvent;
import com.genyo.events.entity.player.PushFluidsEvent;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.events.network.PushOutOfBlocksEvent;
import com.genyo.managers.Managers;
import com.genyo.mixin.accessor.AccessorBundlePacket;
import com.genyo.mixin.accessor.AccessorClientWorld;
import com.genyo.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.modules.world.GenyoSurroundV2;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.math.GPositionUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GenyoVelocity extends GenyoModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<Boolean> explosionConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Explosion")
        .description("Removes player explosion velocity")
        .defaultValue(true)
        .build()
    );
    public final Setting<Double> explosionsHorizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("explosions-horizontal")
        .description("How much velocity you will take from explosions horizontally.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(explosionConfig::get)
        .build()
    );
    public final Setting<Double> explosionsVertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("explosions-vertical")
        .description("How much velocity you will take from explosions vertically.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(explosionConfig::get)
        .build()
    );
    private final Setting<Boolean> knockbackConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Knockback")
        .description("Removes player knockback velocity")
        .defaultValue(true)
        .build()
    );
    private final Setting<VelocityMode> modeConfig = sgGeneral.add(new EnumSetting.Builder<VelocityMode>()
        .name("Mode")
        .description("The mode for velocity")
        .defaultValue(VelocityMode.NORMAL)
        .build()
    );
    private final Setting<Float> horizontalConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Horizontal")
        .description("How much horizontal knock-back to take")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(100.0f)
        .visible(() -> modeConfig.get() == VelocityMode.NORMAL || modeConfig.get() == VelocityMode.WALLS)
        .build()
    );
    private final Setting<Float> verticalConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Vertical")
        .description("How much vertical knock-back to take")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(100.0f)
        .visible(() -> modeConfig.get() == VelocityMode.NORMAL || modeConfig.get() == VelocityMode.WALLS)
        .build()
    );
    private final Setting<Boolean> wallsAirConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Ground Only")
        .description("Only applies velocity in walls while on ground")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == VelocityMode.WALLS)
        .build()
    );
    private final Setting<Boolean> wallsTrappedConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Trapped")
        .description("Applies velocity while player head is trapped")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == VelocityMode.WALLS)
        .build()
    );
    private final Setting<Boolean> concealConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Conceal")
        .description("Fixes velocity on servers with excessive setbacks")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pushEntitiesConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("NoPush-Entities")
        .description("Prevents being pushed away from entities")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pushBlocksConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("NoPush-Blocks")
        .description("Prevents being pushed out of blocks")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pushLiquidsConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("NoPush-Liquids")
        .description("Prevents being pushed by flowing liquids")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pushFishhookConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("NoPush-Fishhook")
        .description("Prevents being pulled by fishing rod hooks")
        .defaultValue(true)
        .build()
    );
    //
    private boolean cancelVelocity;
    private boolean concealVelocity;
    public GenyoVelocity() {
        super(Genyo.MOVEMENT, "genyo-velocity", "Prevents server from applying velocity");
    }

    @Override
    public void onActivate() {
        cancelVelocity = false;
    }

    @Override
    public void onDeactivate() {
        if (cancelVelocity) {
            if (modeConfig.get() == VelocityMode.GRIM) {
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();
                if (Managers.ROTATION.isRotating()) {
                    yaw = Managers.ROTATION.getRotationYaw();
                    pitch = Managers.ROTATION.getRotationPitch();
                }
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                if (Managers.NETWORK.isCrystalPvpCC())
//                {
//                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
//                            mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                }
            }
            cancelVelocity = false;
        }

        concealVelocity = false;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;

        if (event.packet instanceof PlayerPositionLookS2CPacket && concealConfig.get()) concealVelocity = true;

        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && knockbackConfig.get()) {
            if (packet.getEntityId() != mc.player.getId()) return;

            Vec3d vel = ((AccessorEntityVelocityUpdateS2CPacket) packet).getVelocity();

            if (concealVelocity && vel.x == 0 && vel.z == 0) {
                concealVelocity = false;
                return;
            }

            if (modeConfig.get() == VelocityMode.WALLS) {
                if (!isPhased() && (!wallsTrappedConfig.get() || !isWallsTrapped())) return;

                if (wallsAirConfig.get() && !Managers.POSITION.isOnGround()) {
                    return;
                }
            }

            switch (modeConfig.get()) {
                case NORMAL, WALLS -> {
                    if (horizontalConfig.get() == 0.0f && verticalConfig.get() == 0.0f) {
                        event.cancel();
                        return;
                    }
                    ((AccessorEntityVelocityUpdateS2CPacket) packet).setVelocity(new Vec3d(
                        vel.x * (horizontalConfig.get() / 100.0f),
                        vel.y * (verticalConfig.get() / 100.0f),
                        vel.z * (horizontalConfig.get() / 100.0f)
                    ));
                }
                case GRIM -> {
                    if (!Managers.ANTICHEAT.hasPassed(100)) return;
                    event.cancel();
                    cancelVelocity = true;
                }

                case GRIM_V3 -> event.setCancelled(isPhased());
            }
        } else if (event.packet instanceof ExplosionS2CPacket packet && explosionConfig.get()) {
            if (modeConfig.get() == VelocityMode.WALLS && !isPhased()) {
                return;
            }

            switch (modeConfig.get()) {
                case NORMAL, WALLS -> {
                    if (horizontalConfig.get() == 0.0f && verticalConfig.get() == 0.0f) {
                        event.cancel();
                    } else {

                    }
                }
                case GRIM -> {
                    if (!Managers.ANTICHEAT.hasPassed(100)) {
                        return;
                    }
                    event.cancel();
                    cancelVelocity = true;
                }

                case GRIM_V3 -> event.setCancelled(isPhased());
            }

            if (event.isCancelled()) {
                mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet.center().getX(), packet.center().getY(), packet.center().getZ(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                    4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
            }
        } else if (event.packet instanceof BundleS2CPacket packet) {
            List<Packet<?>> allowedBundle = new ArrayList<>();

            for (Packet<?> packet1 : packet.getPackets()) {
                if (packet1 instanceof ExplosionS2CPacket packet2 && explosionConfig.get()) {
                    mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet2.center().getX(), packet2.center().getY(), packet2.center().getZ(),
                        SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                        4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));

                    if (modeConfig.get() == VelocityMode.WALLS && !isPhased()) {
                        allowedBundle.add(packet1);
                        continue;
                    }

                    switch (modeConfig.get()) {
                        case NORMAL, WALLS -> {
                            if (horizontalConfig.get() == 0.0f && verticalConfig.get() == 0.0f) {
                                continue;
                            } else {

                            }
                        }
                        case GRIM -> {
                            if (Managers.ANTICHEAT.hasPassed(100)) {
                                allowedBundle.add(packet1);
                                continue;
                            }

                            cancelVelocity = true;
                            continue;
                        }
                        case GRIM_V3 -> {
                            if (isPhased()) {
                                continue;
                            }
                        }
                    }
                } else if (packet1 instanceof EntityVelocityUpdateS2CPacket packet2 && knockbackConfig.get()) {
                    if (packet2.getEntityId() != mc.player.getId()) {
                        allowedBundle.add(packet1);
                        continue;
                    }

                    if (modeConfig.get() == VelocityMode.WALLS) {
                        if (!isPhased() && (!wallsTrappedConfig.get() || !isWallsTrapped())) {
                            allowedBundle.add(packet1);
                            continue; // was "return" — bug fix
                        }

                        if (wallsAirConfig.get() && !Managers.POSITION.isOnGround()) {
                            allowedBundle.add(packet1);
                            continue;
                        }
                    }

                    switch (modeConfig.get()) {
                        case NORMAL, WALLS -> {
                            if (horizontalConfig.get() == 0.0f && verticalConfig.get() == 0.0f) {
                                continue;
                            } else {
                                Vec3d vel2 = ((AccessorEntityVelocityUpdateS2CPacket) packet2).getVelocity();
                                ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocity(new Vec3d(
                                    vel2.x * (horizontalConfig.get() / 100.0f),
                                    vel2.y * (verticalConfig.get() / 100.0f),
                                    vel2.z * (horizontalConfig.get() / 100.0f)
                                ));
                            }
                        }
                        case GRIM -> {
                            if (!Managers.ANTICHEAT.hasPassed(100)) {
                                allowedBundle.add(packet1);
                                continue;
                            }

                            cancelVelocity = true;
                            continue;
                        }
                        case GRIM_V3 -> {
                            if (isPhased()) {
                                continue;
                            }
                        }
                    }
                }

                allowedBundle.add(packet1);
            }

            ((AccessorBundlePacket) packet).setIterable(allowedBundle);
        } else if (event.packet instanceof EntityDamageS2CPacket packet
            && packet.entityId() == mc.player.getId()
            && modeConfig.get() == VelocityMode.GRIM_V3 && isPhased()) {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
        } else if (event.packet instanceof EntityStatusS2CPacket packet
            && packet.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY && pushFishhookConfig.get()) {
            Entity entity = packet.getEntity(mc.world);
            if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == mc.player) {
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event) {
        concealVelocity = false;

        if (cancelVelocity) {
            if (modeConfig.get() == VelocityMode.GRIM) {
                // Fixes issue with rotations
                float yaw = Managers.ROTATION.getServerYaw();
                float pitch = Managers.ROTATION.getServerPitch();
                if (Managers.ROTATION.isRotating()) {
                    yaw = Managers.ROTATION.getRotationYaw();
                    pitch = Managers.ROTATION.getRotationPitch();
                }
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                if (Managers.NETWORK.isCrystalPvpCC())
//                {
//                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
//                            mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                }
            }
            cancelVelocity = false;
        }
    }

    @EventHandler
    public void onPushEntity(PushEntityEvent event) {
        if (pushEntitiesConfig.get() && event.pushed.equals(mc.player)) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
        if (pushBlocksConfig.get()) event.cancel();
    }

    @EventHandler
    public void onPushFluid(PushFluidsEvent event) {
        if (pushLiquidsConfig.get()) event.cancel();
    }

    private boolean isWallsTrapped() {
        BlockPos headPos = mc.player.getBlockPos().up(mc.player.isCrawling() ? 1 : 2);
        if (mc.world.getBlockState(headPos).isReplaceable()) {
            return false;
        }

        return Modules.get().get(GenyoSurroundV2.class).getSurroundNoDown(mc.player).stream()
            .noneMatch(blockPos -> mc.world.getBlockState(mc.player.isCrawling() ? blockPos : blockPos.up()).isReplaceable());
    }

    private boolean isPhased() {
        return GPositionUtils.getAllInBox(mc.player.getBoundingBox()).stream()
            .anyMatch(blockPos -> !mc.world.getBlockState(blockPos).isReplaceable());
    }

    public double getHorizontal(Setting<Double> setting) {
        return isActive() ? setting.get() : 1;
    }

    public double getVertical(Setting<Double> setting) {
        return isActive() ? setting.get() : 1;
    }

    private enum VelocityMode {
        NORMAL,
        WALLS,
        GRIM,
        GRIM_V3
    }

}
