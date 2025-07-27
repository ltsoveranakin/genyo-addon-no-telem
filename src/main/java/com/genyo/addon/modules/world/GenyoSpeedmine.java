package com.genyo.addon.modules.world;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.AttackBlockEvent;
import com.genyo.addon.events.meteor.SettingChangedEvent;
import com.genyo.addon.managers.Managers;
import com.genyo.addon.managers.player.rotation.Rotation;
import com.genyo.addon.mixin.accessor.AccessorClientPlayerInteractionManager;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.render.animation.Animation;
import com.genyo.addon.settings.FloatSetting;
import com.genyo.addon.utils.collection.FirstOutQueue;
import com.genyo.addon.utils.math.MathUtil;
import com.genyo.addon.utils.player.EnchantmentUtil;
import com.genyo.addon.utils.player.RotationUtil;
import com.genyo.addon.utils.render.ColorUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.HashMap;
import java.util.Map;

public class GenyoSpeedmine extends GenyoModule {

    public GenyoSpeedmine() {
        super(GenyoAddon.GENYO, "genyo-speedmine", "TU TU TU TU..MAX VERSTAPPEN. TU TU TU TU...");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgBehaviour = settings.createGroup("Behaviour");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<SpeedmineMode> modeConfig = sgGeneral.add(new EnumSetting.Builder<SpeedmineMode>()
        .name("Mode")
        .description("The mining mode for speedmine")
        .defaultValue(SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Boolean> multitaskConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Multitask")
        .description("Allows mining while using items")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Boolean> doubleBreakConfig = sgBreak.add(new BoolSetting.Builder()
        .name("Double Break")
        .description("Allows you to mine two blocks at once")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Float> rangeConfig = sgBreak.add(new FloatSetting.Builder()
        .name("Range")
        .description("The range to mine blocks")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Float> speedConfig = sgBreak.add(new FloatSetting.Builder()
        .name("Speed")
        .description("The speed to mine blocks")
        .min(0.1f)
        .defaultValue(1.0f)
        .max(1.0f)
        .build()
    );

    private final Setting<Boolean> instantConfig = sgBehaviour.add(new BoolSetting.Builder()
        .name("Instant")
        .description("Instantly mines already broken blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Swap> swapConfig = sgBehaviour.add(new EnumSetting.Builder<Swap>()
        .name("Auto Swap")
        .description("Swaps to the best tool once the mining is complete")
        .defaultValue(Swap.SILENT)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Boolean> rotateConfig = sgBehaviour.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Rotates when mining the block")
        .defaultValue(true)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Boolean> switchResetConfig = sgBehaviour.add(new BoolSetting.Builder()
        .name("Switch Reset")
        .description("Resets mining after switching items")
        .defaultValue(false)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Boolean> grimConfig = sgBehaviour.add(new BoolSetting.Builder()
        .name("Grim")
        .description("Uses grim block breaking speeds")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grimNewConfig = sgBehaviour.add(new BoolSetting.Builder()
        .name("Grim V3")
        .description("Uses new grim block breaking speeds")
        .defaultValue(false)
        .visible(grimConfig::get)
        .build()
    );

    private final Setting<SettingColor> colorConfig = sgRender.add(new ColorSetting.Builder()
        .name("Mine Color")
        .description("The mine render color")
        .defaultValue(Color.RED)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<SettingColor> colorDoneConfig = sgRender.add(new ColorSetting.Builder()
        .name("Done Color")
        .description("The done render color")
        .defaultValue(Color.GREEN)
        .visible(() -> modeConfig.get() == SpeedmineMode.PACKET)
        .build()
    );

    private final Setting<Integer> fadeTimeConfig = sgRender.add(new IntSetting.Builder()
        .name("Fade Time")
        .description("Time to fade")
        .min(0)
        .defaultValue(250)
        .max(1000)
        .build()
    );

    private final Setting<Boolean> smoothColorConfig = sgRender.add(new BoolSetting.Builder()
        .name("Smooth Color")
        .description("Interpolates from start to done color")
        .defaultValue(false)
        .build()
    );

    private final Map<MiningData, Animation> fadeList = new HashMap<>();
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue<>(2);
    private long lastBreak;

    @Override
    public void onDeactivate() {
        miningQueue.clear();
        fadeList.clear();
        Managers.INVENTORY.syncToClient();
    }

    @Override
    public void onActivate() {
        if (doubleBreakConfig.get())
        {
            miningQueue = new FirstOutQueue<>(2);
        }
        else
        {
            miningQueue = new FirstOutQueue<>(1);
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event)
    {
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) return;

        if (modeConfig.get() == SpeedmineMode.DAMAGE)
        {
            AccessorClientPlayerInteractionManager interactionManager =
                (AccessorClientPlayerInteractionManager) mc.interactionManager;
            if (interactionManager.hookGetCurrentBreakingProgress() >= speedConfig.get())
            {
                interactionManager.hookSetCurrentBreakingProgress(1.0f);
            }
            return;
        }

        if (Modules.get().isActive(GenyoAutoMine.class))
        {
            return;
        }

        if (miningQueue.isEmpty())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.getState().isAir())
            {
                data.resetBreakTime();
            }
            if (isDataPacketMine(data) && (data.getState().isAir() || data.hasAttemptedBreak()
                && data.passedAttemptedBreakTime(500)))
            {
                Managers.INVENTORY.syncToClient();
                miningQueue.remove(data);
                continue;
            }
            final float damageDelta = calcBlockBreakingDelta(data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (isDataPacketMine(data) && data.getBlockDamage() >= 1.0f && data.getSlot() != -1)
            {
                if (mc.player.isUsingItem() && !multitaskConfig.get())
                {
                    return;
                }

                if (data.getSlot() != Managers.INVENTORY.getServerSlot())
                {
                    Managers.INVENTORY.setSlot(data.getSlot());
                }
                if (!data.hasAttemptedBreak())
                {
                    data.setAttemptedBreak(true);
                }
            }
        }
        MiningData miningData2 = miningQueue.getFirst();
        final double distance = mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
        if (distance > MathUtil.squared(rangeConfig.get()))
        {
            // abortMining(miningData);
            miningQueue.remove(miningData2);
            return;
        }
        if (miningData2.getState().isAir())
        {
            return;
        }
        // Something went wrong, remove and remine
        if (miningData2.getBlockDamage() >= speedConfig.get() && miningData2.hasAttemptedBreak()
            && miningData2.passedAttemptedBreakTime(500))
        {
            abortMining(miningData2);
            miningQueue.remove(miningData2);
        }
        if (miningData2.getBlockDamage() >= speedConfig.get())
        {
            if (mc.player.isUsingItem() && !multitaskConfig.get())
            {
                return;
            }
            stopMining(miningData2);

            if (!instantConfig.get())
            {
                miningQueue.remove(miningData2);
            }

            if (!miningData2.hasAttemptedBreak())
            {
                miningData2.setAttemptedBreak(true);
            }
        }
    }

    @EventHandler
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || modeConfig.get() != SpeedmineMode.PACKET) return;

        if (Modules.get().isActive(GenyoAutoMine.class))
        {
            return;
        }
        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.state.getBlock().getHardness() == -1.0f || event.state.isAir())
        {
            return;
        }

        startManualMine(event.pos, event.direction);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        if (event.packet instanceof PlayerActionC2SPacket packet
            && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
            && modeConfig.get() == SpeedmineMode.DAMAGE && grimConfig.get())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(500), packet.getDirection()));
        }

        if (event.packet instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.get()
            && modeConfig.get() == SpeedmineMode.PACKET)
        {
            for (MiningData data : miningQueue)
            {
                data.resetDamage();
            }
        }
    }

    @EventHandler
    public void onPacketInbound(PacketEvent.Receive event)
    {
        if (mc.player == null || modeConfig.get() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (Modules.get().isActive(GenyoAutoMine.class))
        {
            return;
        }

        if (event.packet instanceof BlockUpdateS2CPacket packet)
        {
            handleBlockUpdatePacket(packet);
        }

        else if (event.packet instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof BlockUpdateS2CPacket packet2)
                {
                    handleBlockUpdatePacket(packet2);
                }
            }
        }
    }

    private void handleBlockUpdatePacket(BlockUpdateS2CPacket packet)
    {
        if (!packet.getState().isAir())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.hasAttemptedBreak() && data.getPos().equals(packet.getPos()))
            {
                data.setAttemptedBreak(false);
            }
        }
    }

    @EventHandler
    public void onConfigUpdate(SettingChangedEvent event)
    {
        if ( event.setting == doubleBreakConfig)
        {
            if (doubleBreakConfig.get())
            {
                miningQueue = new FirstOutQueue<>(2);
            }
            else
            {
                miningQueue = new FirstOutQueue<>(1);
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event)
    {
        if (mc.player.isCreative() || modeConfig.get() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (Modules.get().isActive(GenyoAutoMine.class))
        {
            return;
        }

        for (Map.Entry<MiningData, Animation> set : fadeList.entrySet())
        {
            MiningData data = set.getKey();
            set.getValue().setState(false);
            int boxAlpha = (int) (40 * set.getValue().getFactor());
            int lineAlpha = (int) (100 * set.getValue().getFactor());

            Color boxColor;
            Color lineColor;
            if (smoothColorConfig.get())
            {
                boxColor = data.getState().isAir() ? colorDoneConfig.get().a(boxAlpha) :
                    ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), colorDoneConfig.get().a(boxAlpha), colorDoneConfig.get().a(boxAlpha));
                lineColor = data.getState().isAir() ? colorDoneConfig.get().a(lineAlpha) :
                    ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), colorDoneConfig.get().a(lineAlpha), colorDoneConfig.get().a(lineAlpha));
            }
            else
            {
                boxColor = data.getBlockDamage() >= 0.95f || data.getState().isAir() ? colorDoneConfig.get().a(boxAlpha) : colorConfig.get().a(boxAlpha);
                lineColor = data.getBlockDamage() >= 0.95f || data.getState().isAir() ? colorDoneConfig.get().a(lineAlpha) : colorConfig.get().a(lineAlpha);
            }

            BlockPos mining = data.getPos();
            VoxelShape outlineShape = data.getState().getOutlineShape(mc.world, mining);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float total = isDataPacketMine(data) ? 1.0f : speedConfig.get();
            float scale = data.getState().isAir() ? 1.0f : MathHelper.clamp((data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * event.tickDelta) / total, 0.0f, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

            event.renderer.box(scaled, boxColor, lineColor, ShapeMode.Both, 0);
        }
        for (MiningData data : miningQueue)
        {
            if (data.getState().isAir())
            {
                continue;
            }
            Animation animation = new Animation(true, fadeTimeConfig.get());
            fadeList.put(data, animation);
        }
        fadeList.entrySet().removeIf(e ->
            e.getValue().getFactor() == 0.0);
    }

    private void startManualMine(BlockPos pos, Direction direction)
    {
        clickMine(new MiningData(pos, direction));
    }

    public void clickMine(MiningData miningData)
    {
        int queueSize = miningQueue.size();
        if (queueSize <= 2)
        {
            queueMiningData(miningData);
        }
    }

    private void queueMiningData(MiningData data)
    {
        if (data.getState().isAir())
        {
            return;
        }
        if (startMining(data))
        {
            if (miningQueue.stream().anyMatch(p1 -> data.getPos().equals(p1.getPos())))
            {
                return;
            }
            miningQueue.addFirst(data);
        }
    }

    private boolean startMining(MiningData data)
    {
        if (data.isStarted())
        {
            return false;
        }

        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        data.setStarted();
        if (grimNewConfig.get())
        {
            /*if (!AnticheatModule.getInstance().getMiningFix())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            }
            else
            {*/
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            //}

            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return true;
        }

        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    private void abortMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
        {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
        {
            return;
        }
        if (rotateConfig.get())
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            if (grimConfig.get())
            {
                Managers.ROTATION.setRotationSilent(rotations[0], rotations[1]);
            }
            else
            {
                Managers.ROTATION.setRotation(new Rotation(2, rotations[0], rotations[1]));
            }
        }
        int slot = data.getSlot();
        boolean canSwap = slot != -1 && slot != Managers.INVENTORY.getServerSlot();
        if (canSwap)
        {
            swapTo(slot);
        }
        stopMiningInternal(data);
        lastBreak = System.currentTimeMillis();
        if (canSwap)
        {
            swapSync(slot);
        }
        if (rotateConfig.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    private void swapTo(int slot)
    {
        switch (swapConfig.get())
        {
            case NORMAL -> Managers.INVENTORY.setClientSlot(slot);
            case SILENT -> Managers.INVENTORY.setSlot(slot);
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    private void swapSync(int slot)
    {
        switch (swapConfig.get())
        {
            case SILENT -> Managers.INVENTORY.syncToClient();
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    private void stopMiningInternal(MiningData data)
    {
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L80
    public boolean isBlockDelayGrim()
    {
        return System.currentTimeMillis() - lastBreak <= 280 && grimConfig.get();
    }

    private boolean isDataPacketMine(MiningData data)
    {
        return miningQueue.size() == 2 && data == miningQueue.getLast();
    }

    public float calcBlockBreakingDelta(BlockState state, BlockView world, BlockPos pos)
    {
        if (swapConfig.get() == Swap.OFF)
        {
            return state.calcBlockBreakingDelta(mc.player, mc.world, pos);
        }
        float f = state.getHardness(world, pos);
        if (f == -1.0f)
        {
            return 0.0f;
        }
        else
        {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }

    private float getBlockBreakingSpeed(BlockState block)
    {
        int tool = Modules.get().get(GenyoAutoTool.class).getBestTool(block);
        //if (tool == -1) return 0.0f;
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F)
        {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (i > 0 && !stack.isEmpty())
            {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player))
        {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier())
            {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
//        if (mc.player.isSubmergedIn(FluidTags.WATER) && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.AQUA_AFFINITY) <= 0)
//        {
//            f /= 5.0f;
//        }
        if (!mc.player.isOnGround())
        {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state)
    {
        if (state.isToolRequired())
        {
            int tool = InvUtils.findFastestTool(state).slot();
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }

    public boolean isMining()
    {
        return !miningQueue.isEmpty();
    }

    public static class MiningData
    {
        private static final MinecraftClient mc = MinecraftClient.getInstance();

        private boolean attemptedBreak;
        private long breakTime;
        private final BlockPos pos;
        private final Direction direction;
        private float lastDamage;
        private float blockDamage;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
        }

        public void setAttemptedBreak(boolean attemptedBreak)
        {
            this.attemptedBreak = attemptedBreak;
            if (attemptedBreak)
            {
                resetBreakTime();
            }
        }

        public void resetBreakTime()
        {
            breakTime = System.currentTimeMillis();
        }

        public boolean hasAttemptedBreak()
        {
            return attemptedBreak;
        }

        public boolean passedAttemptedBreakTime(long time)
        {
            return System.currentTimeMillis() - breakTime >= time;
        }

        public float damage(final float dmg)
        {
            lastDamage = blockDamage;
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage)
        {
            this.blockDamage = blockDamage;
        }

        public void resetDamage()
        {
            started = false;
            blockDamage = 0.0f;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public int getSlot()
        {
            return Modules.get().get(GenyoAutoTool.class).getBestToolNoFallback(getState());
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public boolean isStarted()
        {
            return started;
        }

        public void setStarted()
        {
            this.started = true;
        }
    }

    public enum SpeedmineMode
    {
        PACKET,
        DAMAGE
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

}
