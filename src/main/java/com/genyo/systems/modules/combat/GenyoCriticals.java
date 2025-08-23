package com.genyo.systems.modules.combat;

import com.genyo.Genyo;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.modules.world.GenyoAutoMine;
import com.genyo.systems.modules.world.GenyoSelfTrap;
import com.genyo.systems.modules.world.GenyoSurroundV2;
import com.genyo.utils.GEntityUtils;
import com.genyo.utils.math.GPositionUtils;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import com.genyo.utils.player.InventoryUtil;
import com.genyo.utils.player.MovementUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class GenyoCriticals extends GenyoModule {

    public GenyoCriticals() {
        super(Genyo.COMBAT, "genyo-criticals", "crrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> multitaskConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Multitask")
        .description("Allows crits when other combat modules are enabled")
        .defaultValue(true)
        .build()
    );

    private final Setting<CritMode> modeConfig = sgGeneral.add(new EnumSetting.Builder<CritMode>()
        .name("Mode")
        .description("Mode for critical attack modifier")
        .defaultValue(CritMode.PACKET)
        .build()
    );

    private final Setting<Boolean> phaseOnlyConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Phased Only")
        .description("Only attempts criticals when phased")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == CritMode.GRIM_V3 || modeConfig.get() == CritMode.GRIM)
        .build()
    );

    private final Setting<Boolean> wallsOnlyConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Walls Only")
        .description("Only attempts criticals in walls")
        .defaultValue(false)
        .visible(() -> (modeConfig.get() == CritMode.GRIM_V3 || modeConfig.get() == CritMode.GRIM) && phaseOnlyConfig.get())
        .build()
    );

    private final Setting<Boolean> moveFixConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Move Fix")
        .description("Pauses crits when moving")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == CritMode.GRIM_V3 || modeConfig.get() == CritMode.GRIM)
        .build()
    );

    //
    private final Timer attackTimer = new CacheTimer();
    private boolean postUpdateGround;
    private boolean postUpdateSprint;

    @Override
    public void onDeactivate()
    {
        postUpdateGround = false;
        postUpdateSprint = false;
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        // Custom aura crit handling
        if (mc.player == null || mc.world == null) return;

        if (Modules.get().get(GenyoAutoCrystal.class).isAttacking() || Modules.get().get(GenyoAutoCrystal.class).isPlacing())
        {
            return;
        }

        // All combat modules have priority
        if (!multitaskConfig.get() && (Modules.get().get(GenyoSurroundV2.class).isPlacing()
            || Modules.get().get(GenyoSelfTrap.class).isPlacing()
            || Modules.get().isActive(GenyoAutoMine.class)))
        {
            return;
        }

        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet
            && packet.meteor$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK)
        {
            if (mc.player.isRiding()
                || mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isHoldingOntoLadder()
                || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || InventoryUtil.isHolding32k())
            {
                return;
            }

            // Attacked entity
            final Entity e = packet.meteor$getEntity();
            if (e == null || !e.isAlive() || !(e instanceof LivingEntity))
            {
                return;
            }
            if (GEntityUtils.isVehicle(e))
            {
                if (modeConfig.get() == CritMode.PACKET)
                {
                    for (int i = 0; i < 5; ++i)
                    {
                        Managers.NETWORK.sendQuietPacket(PlayerInteractEntityC2SPacket.attack(e,
                            Managers.POSITION.isSneaking()));
                        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }
                return;
            }

            postUpdateSprint = mc.player.isSprinting();
            if (postUpdateSprint)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }

            attackSpoofJump(e);
        }
    }


    public void attackSpoofJump(Entity e)
    {
        double x = Managers.POSITION.getX();
        double y = Managers.POSITION.getY();
        double z = Managers.POSITION.getZ();
        switch (modeConfig.get())
        {
            case VANILLA ->
            {
                if (mc.player.isOnGround() && !mc.player.input.playerInput.jump())
                {
                    double d = 1.0e-7 + 1.0e-7 * (1.0 + RANDOM.nextInt(RANDOM.nextBoolean() ? 34 : 43));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 0.1016f + d * 3.0f, z, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 0.0202f + d * 2.0f, z, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 3.239e-4 + d, z, false, mc.player.horizontalCollision));
                    mc.player.addCritParticles(e);
                }
            }
            case PACKET ->
            {
                if (mc.player.isOnGround() && !mc.player.input.playerInput.jump())
                {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 0.0625f, z, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y, z, false, mc.player.horizontalCollision));
                    mc.player.addCritParticles(e);
                }
            }
            case PACKET_STRICT ->
            {
                if (attackTimer.passed(500) && mc.player.isOnGround() && !mc.player.input.playerInput.jump())
                {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 1.1e-7f, z,false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + 1.0e-8f, z, false, mc.player.horizontalCollision));
                    postUpdateGround = true;
                    attackTimer.reset();
                }
            }
            case GRIM ->
            {
                if (phaseOnlyConfig.get() && (wallsOnlyConfig.get() ? !isDoublePhased() : !isPhased()))
                {
                    return;
                }

                if (moveFixConfig.get() && MovementUtil.isMovingInput())
                {
                    return;
                }

                if (attackTimer.passed(250) && mc.player.isOnGround() && !mc.player.isCrawling())
                {
                    float yaw = Managers.ROTATION.getServerYaw();
                    float pitch = Managers.ROTATION.getServerPitch();
                    if (Managers.ROTATION.isRotating())
                    {
                        yaw = Managers.ROTATION.getRotationYaw();
                        pitch = Managers.ROTATION.getRotationPitch();
                    }
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y + 0.0625, z, yaw, pitch, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y + 0.0625013579, z, yaw, pitch, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y + 1.3579e-6, z, yaw, pitch, false, mc.player.horizontalCollision));
                    attackTimer.reset();
                }
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.00150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.014400000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.001150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
            }
            case GRIM_V3 ->
            {
                if (phaseOnlyConfig.get() && (wallsOnlyConfig.get() ? !isDoublePhased() : !isPhased()))
                {
                    return;
                }

                if (moveFixConfig.get() && MovementUtil.isMovingInput())
                {
                    return;
                }

                if (mc.player.isOnGround() && !mc.player.isCrawling())
                {
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00001058293536f, z, false));
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00000916580235f, z, false));
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00000010371854f, z, false));
                    float yaw = Managers.ROTATION.getServerYaw();
                    float pitch = Managers.ROTATION.getServerPitch();
                    if (Managers.ROTATION.isRotating())
                    {
                        yaw = Managers.ROTATION.getRotationYaw();
                        pitch = Managers.ROTATION.getRotationPitch();
                    }
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y, z, yaw, pitch, true, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y + 0.0625f, z, yaw, pitch, false, mc.player.horizontalCollision));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                        x, y + 0.04535f, z, yaw, pitch, false, mc.player.horizontalCollision));
                }
            }
            case LOW_HOP ->
            {
                // mc.player.jump();
                Managers.MOVEMENT.setMotionY(0.3425);
            }
        }
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (mc.player == null) return;

        if (event.packet instanceof PlayerInteractEntityC2SPacket)
        {
            if (postUpdateGround)
            {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision));
                postUpdateGround = false;
            }

            if (postUpdateSprint)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                postUpdateSprint = false;
            }
        }
    }

    public boolean isGrim()
    {
        return modeConfig.get() == CritMode.GRIM;
    }

    public boolean isDoublePhased()
    {
        for (BlockPos pos : GPositionUtils.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()))
        {
            BlockState state = mc.world.getBlockState(pos);
            BlockState state2 = mc.world.getBlockState(pos.up());
            if (state.blocksMovement() && state2.blocksMovement())
            {
                return true;
            }
        }
        return false;
    }

    public boolean isPhased()
    {
        for (BlockPos pos : GPositionUtils.getAllInBox(mc.player.getBoundingBox()))
        {
            if (mc.world.getBlockState(pos).blocksMovement())
            {
                return true;
            }
        }

        return false;
    }

    public enum CritMode
    {
        PACKET,
        PACKET_STRICT,
        VANILLA,
        GRIM,
        GRIM_V3,
        LOW_HOP
    }

}
