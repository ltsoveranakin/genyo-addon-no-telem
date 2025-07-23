package com.genyo.addon.modules;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.AttackBlockEvent;
import com.genyo.addon.render.animation.Animation;
import com.genyo.addon.settings.FloatSetting;
import com.genyo.addon.utils.GEntityUtils;
import com.genyo.addon.utils.math.GPositionUtils;
import com.genyo.addon.utils.render.ColorUtil;
import com.genyo.addon.utils.world.BlastResistantBlocks;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

public class GenyoAutoMine extends GenyoModule{

    public GenyoAutoMine() {
        super(GenyoAddon.GENYO, "genyo-auto-mine", "dábül");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSelection = settings.createGroup("Selection");

    private final Setting<Boolean> multitask = sgGeneral.add(new BoolSetting.Builder()
        .name("Allow Multitask")
        .description("Allows actions while using items")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> auto = sgSelection.add(new BoolSetting.Builder()
        .name("Auto")
        .description("Automatically mines nearby players feet")
        .defaultValue(false)
        .build()
    );

    private final Setting<Selection> selection = sgSelection.add(new EnumSetting.Builder<Selection>()
        .name("Selection")
        .description("The selection of blocks mine")
        .visible(auto::get)
        .defaultValue(Selection.ALL)
        .build()
    );

    private final Setting<Boolean> avoidSelf = sgSelection.add(new BoolSetting.Builder()
        .name("Avoid Self")
        .description("Avoids mining blocks in your surround")
        .defaultValue(false)
        .visible(auto::get)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgSelection.add(new BlockListSetting.Builder()
        .name("Block Whitelist")
        .description("Valid block whitelist")
        .defaultValue(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
        .build()
    );

    private final Setting<List<Block>> blacklist = sgSelection.add(new BlockListSetting.Builder()
        .name("Block Blacklist")
        .description("Valid block blacklist")
        .defaultValue(Blocks.SHULKER_BOX)
        .build()
    );

    private final Setting<Float> enemyRange = sgSelection.add(new FloatSetting.Builder()
        .name("Enemy Range")
        .description("Only mines on visible faces")
        .defaultValue(5.0f)
        .min(1.0f)
        .max(10.0f)
        .visible(auto::get)
        .build()
    );

    private final Setting<Boolean> strictDirection = sgSelection.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Only mines on visible faces")
        .visible(auto::get)
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> antiCrawl = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Crawl")
        .description("Attempts to stop player from crawling")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> head = sgGeneral.add(new BoolSetting.Builder()
        .name("Target Body")
        .description("Attempts to mine players face blocks")
        .defaultValue(false)
        .visible(auto::get)
        .build()
    );

    private final Setting<Boolean> aboveHead = sgGeneral.add(new BoolSetting.Builder()
        .name("Target Head")
        .description("Attempts to mine above players head")
        .defaultValue(false)
        .visible(auto::get)
        .build()
    );

    private final Setting<Boolean> doubleBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("Double Break")
        .description("Allows you to mine two blocks at once")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> mineTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Mining Ticks")
        .description("The max number of ticks to hold a pickaxe for the packet mine")
        .min(5)
        .max(60)
        .defaultValue(20)
        .visible(doubleBreak::get)
        .build()
    );

    private final Setting<RemineMode> remine = sgGeneral.add(new EnumSetting.Builder<RemineMode>()
        .name("Remine")
        .description("Remines already mined blocks")
        .defaultValue(RemineMode.NORMAL)
        .build()
    );

    private final Setting<Boolean> packetInstant = sgGeneral.add(new BoolSetting.Builder()
        .name("Fast")
        .description("Instant mines on packet")
        .defaultValue(false)
        .visible(() -> remine.get() == RemineMode.INSTANT)
        .build()
    );

    private final Setting<Float> range = sgGeneral.add(new FloatSetting.Builder()
        .name("Range")
        .description("The range to mine blocks")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .build()
    );

    private final Setting<Float> speed = sgGeneral.add(new FloatSetting.Builder()
        .name("Speed")
        .description("The speed to mine blocks")
        .min(0.1f)
        .defaultValue(1.0f)
        .max(1.0f)
        .build()
    );

    private final Setting<Swap> swap = sgGeneral.add(new EnumSetting.Builder<Swap>()
        .name("Auto Swap")
        .description("Swaps to the best tool once the mining is complete")
        .defaultValue(Swap.SILENT)
        .build()
    );

    private final Setting<Boolean> swapBefore = sgGeneral.add(new BoolSetting.Builder()
        .name("Swap Before")
        .description("Swaps before fully done mining")
        .defaultValue(false)
        .visible(() -> swap.get() != Swap.OFF)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Rotates when mining the block")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> switchReset = sgGeneral.add(new BoolSetting.Builder()
        .name("Switch Reset")
        .description("Resets mining after switching items")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grim = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim")
        .description("Uses grim block breaking speeds")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grimNew = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim V3")
        .description("Allows mining on new grim servers")
        .defaultValue(false)
        .visible(grim::get)
        .build()
    );

    private final Setting<Boolean> anticheat = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Cheat")
        .description("grim anti cheat genyo fasz")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Mine Color")
        .description("The mine render color")
        .defaultValue(new SettingColor(255, 0, 0, 45))
        .build()
    );

    private final Setting<SettingColor> colorDone = sgGeneral.add(new ColorSetting.Builder()
        .name("Done Color")
        .description("The done render color")
        .defaultValue(new SettingColor(0, 255, 0, 45))
        .build()
    );

    private final Setting<Integer> fadeTime = sgGeneral.add(new IntSetting.Builder()
        .name("Fade Time")
        .description("Time to fade")
        .min(0)
        .defaultValue(250)
        .max(1000)
        .visible(() -> false) // ??????????
        .build()
    );

    private final Setting<Boolean> smoothColor = sgGeneral.add(new BoolSetting.Builder()
        .name("Smooth Color")
        .description("Interpolates from start to done color")
        .defaultValue(false)
        .visible(() -> false) // ??????????
        .build()
    );

    private PlayerEntity playerTarget;
    private MineData packetMine, instantMine; // mining2 should always be the instant mine
    private boolean packetSwapBack;
    private boolean manualOverride;
    private int remineTimer = 0; // TODO: tick timer

    private boolean changedInstantMine;
    private boolean waitForPacketMine;
    private boolean packetMineStuck;

    private boolean antiCrawlOverride;
    private int antiCrawlTicks;

    private final Queue<MineData> autoMineQueue = new ArrayDeque<>();
    private int autoMineTickDelay;

    private MineAnimation packetMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
    private MineAnimation instantMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));

    @Override
    public void onDeactivate() {
        autoMineQueue.clear();
        playerTarget = null;
        packetMine = null;

        if (instantMine != null) {
            abortMining(instantMine);
            instantMine = null;
        }

        packetMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
        instantMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));

        autoMineTickDelay = 0;
        antiCrawlTicks = 0;
        manualOverride = false;
        antiCrawlOverride = false;
        waitForPacketMine = false;
        packetMineStuck = false;
        if (packetSwapBack) {
            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
            packetSwapBack = false;
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;

        remineTimer++;

        PlayerEntity currentTarget = getClosestPlayer((double) enemyRange.get());
        boolean targetChanged = playerTarget != null && playerTarget != currentTarget;
        playerTarget = currentTarget;

        if (isInstantMineComplete()) {
            if (changedInstantMine) changedInstantMine = false;
            if (waitForPacketMine) waitForPacketMine = false;
        }

        autoMineTickDelay--;
        antiCrawlTicks--;

        // Mining packet handling
        if (packetMine != null && packetMine.getTicksMining() > mineTicks.get()) {
            packetMineStuck = true;
            packetMineAnim.animation.setState(false);
            if (packetSwapBack) {
                ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                packetSwapBack = false;
            }
            packetMine = null;
            if (!isInstantMineComplete()) {
                waitForPacketMine = true;
            }
        }

        if (packetMine != null) {
            final float damageDelta = mc.world.getBlockState(packetMine.getPos()).calcBlockBreakingDelta(mc.player, mc.world, packetMine.getPos());

            packetMine.addBlockDamage(damageDelta);

            int slot = packetMine.getBestSlot();
            float damageDone = packetMine.getBlockDamage() + (swapBefore.get()
                || packetMineStuck ? damageDelta : 0.0f);
            if (damageDone >= 1.0f && slot != -1  && !checkMultitask())
            {
                //Managers.INVENTORY.setSlot(slot);
                InvUtils.move().slot(slot);
                packetSwapBack = true;
                if (packetMineStuck)
                {
                    packetMineStuck = false;
                }
            }
        }

        if (packetSwapBack)
        {
            if (packetMine != null && canMine(packetMine.getState()))
            {
                packetMine.markAttemptedMine();
            }
            else
            {
                ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                packetSwapBack = false;
                packetMineAnim.animation.setState(false);
                packetMine = null;
                if (!isInstantMineComplete())
                {
                    waitForPacketMine = true;
                }
            }
        }

        if (instantMine != null)
        {
            final double distance = mc.player.getEyePos().squaredDistanceTo(instantMine.getPos().toCenterPos());
            if (distance > square(range.get()) || instantMine.getTicksMining() > mineTicks.get()) {
                abortMining(instantMine);
                instantMineAnim.animation.setState(false);
                instantMine = null;
            }
        }

        if (instantMine != null) {
            final float damageDelta = mc.world.getBlockState(instantMine.getPos()).calcBlockBreakingDelta(mc.player, mc.world, instantMine.getPos());

            instantMine.addBlockDamage(damageDelta);

            if (instantMine.getBlockDamage() >= speed.get()) {
                boolean canMine = canMine(instantMine.getState());
                boolean canPlace = mc.world.canPlace(instantMine.getState(), instantMine.getPos(), ShapeContext.absent());
                if (canMine) {
                    instantMine.markAttemptedMine();
                } else {
                    instantMine.resetMiningTicks();
                    if (remine.get() == RemineMode.NORMAL || remine.get() == RemineMode.FAST) {
                        instantMine.setTotalBlockDamage(0.0f, 0.0f);
                    }

                    if (manualOverride) {
                        manualOverride = false;
                        // Clear our old manual mine
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }
                }

                boolean passedRemine = remine.get() == RemineMode.INSTANT || (remineTimer >= 500);

                if (instantMine != null && (remine.get() == RemineMode.INSTANT
                    && packetInstant.get() && packetMine == null && canPlace || canMine && passedRemine)
                    && (!checkMultitask() || multitask.get() || swap.get() == Swap.OFF)) {
                    stopMining(instantMine);
                    remineTimer = 0;

                    if (remine.get() == RemineMode.FAST) {
                        startMining(instantMine);
                    }
                }
            }
        }

        // Clear overrides
        if (manualOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.MANUAL)) {
            manualOverride = false;
        }

        if (antiCrawlOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.PREVENT_CRAWL)) {
            antiCrawlOverride = false;
        }

        if (auto.get()) {
            if (!autoMineQueue.isEmpty() && autoMineTickDelay <= 0) {
                MineData nextMine = autoMineQueue.poll();
                if (nextMine != null) {
                    startMining(nextMine);
                    autoMineTickDelay = 5;
                }
            }

            BlockPos antiCrawlPos = getAntiCrawlPos(playerTarget);
            if (antiCrawlOverride) {
                if (mc.player.getPose().equals(EntityPose.SWIMMING)) {
                    antiCrawlTicks = 10;
                }

                if (antiCrawlTicks <= 0 || !isInstantMineComplete() && antiCrawlPos != null
                    && !instantMine.getPos().equals(antiCrawlPos))
                {
                    antiCrawlOverride = false;
                }
            }

            if (autoMineQueue.isEmpty() && !manualOverride && !antiCrawlOverride) {
                if (antiCrawl.get() && mc.player.getPose().equals(EntityPose.SWIMMING) && antiCrawlPos != null)
                {
                    MineData data = new MineData(antiCrawlPos, strictDirection.get() ?
                        mc.player.getHorizontalFacing() : Direction.UP, MiningGoal.PREVENT_CRAWL);
                    if (isInstantMineComplete() || !instantMine.equals(data))
                    {
                        startAutoMine(data);
                        antiCrawlOverride = true;
                    }
                } else if (playerTarget != null && !targetChanged) {
                    BlockPos targetPos = GEntityUtils.getRoundedBlockPos(playerTarget);
                    boolean bedrockPhased = GPositionUtils.isBedrock(playerTarget.getBoundingBox(), targetPos) && !playerTarget.isCrawling();

                    if (!isInstantMineComplete() && checkDataY(instantMine, targetPos, bedrockPhased))
                    {
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }

                    else if (packetMine != null && checkDataY(packetMine, targetPos, bedrockPhased))
                    {
                        packetMineAnim.animation.setState(false);
                        if (packetSwapBack)
                        {
                            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }

                    else
                    {
                        List<BlockPos> phasedBlocks = getPhaseBlocks(playerTarget, targetPos, bedrockPhased);

                        MineData bestMine;
                        if (!phasedBlocks.isEmpty())
                        {
                            BlockPos pos1 = phasedBlocks.removeFirst();
                            bestMine = new MineData(pos1, strictDirection.get() ? mc.player.getHorizontalFacing() : Direction.UP);

                            if (packetMine == null && doubleBreak.get() || isInstantMineComplete()) {
                                startAutoMine(bestMine);
                            }
                        }

                        else
                        {
                            List<BlockPos> miningBlocks = getMiningBlocks(playerTarget, targetPos, bedrockPhased);
                            bestMine = getInstantMine(miningBlocks, bedrockPhased);

                            if (bestMine != null && (packetMine == null && !changedInstantMine
                                && doubleBreak.get() || isInstantMineComplete()))
                            {
                                startAutoMine(bestMine);
                            }
                        }
                    }
                }

                else
                {
                    if (!isInstantMineComplete() && instantMine.getGoal() == MiningGoal.MINING_ENEMY) {
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }

                    if (packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY) {
                        packetMineAnim.animation.setState(false);
                        if (packetSwapBack) {
                            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttackBlock(AttackBlockEvent event) {
        if (mc.player == null && mc.world == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;

        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.state.getBlock().getHardness() == -1.0f || !canMine(event.state) || isMining(event.pos)) return;

        MineData data = new MineData(event.pos, event.direction, MiningGoal.MANUAL);

        if (instantMine != null && instantMine.getGoal() == MiningGoal.MINING_ENEMY
            || packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY)
        {
            manualOverride = true;
        }

        if (!doubleBreak.get()) {
            instantMine = data;
            startMining(instantMine);
            mc.player.swingHand(Hand.MAIN_HAND, false);
            return;
        }

        boolean updateChanged = false;
        if (!isInstantMineComplete() && !changedInstantMine) {
            if (packetMine == null) {
                packetMine = instantMine.copy();
                packetMineAnim = new MineAnimation(packetMine,
                    new Animation(true, fadeTime.get()));
            } else {
                updateChanged = true;
            }
        }

        instantMine = data;
        startMining(instantMine);
        mc.player.swingHand(Hand.MAIN_HAND, false);
        if (updateChanged) changedInstantMine = true;
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket && switchReset.get() && instantMine != null) {
            instantMine.setTotalBlockDamage(0.0f, 0.0f);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet && canMine(packet.getState())) {
            if (antiCrawlOverride && packet.getPos().equals(getAntiCrawlPos(playerTarget))) {
                antiCrawlTicks = 10;
            }
        }
    }

    public void startAutoMine(MineData data) {
        if (!canMine(data.getState()) || isMining(data.getPos())) return;

        if (!doubleBreak.get()) {
            instantMine = data;
            autoMineQueue.offer(data);
            return;
        }

        if (changedInstantMine && !isInstantMineComplete() || waitForPacketMine) return;

        boolean updateChanged = false;
        if (!isInstantMineComplete() && !changedInstantMine) {
            if (packetMine == null) {
                packetMine = instantMine.copy();
                packetMineAnim = new MineAnimation(packetMine,
                    new Animation(true, fadeTime.get()));
            } else {
                updateChanged = true;
            }
        }

        instantMine = data;
        autoMineQueue.offer(data);

        if (updateChanged) {
            changedInstantMine = true;
        }
    }

    public MineData getInstantMine(List<BlockPos> miningBlocks, boolean bedrockPhased) {
        PriorityQueue<MineData> validInstantMines = new PriorityQueue<>();
        for (BlockPos blockPos : miningBlocks) {
            BlockState state1 = mc.world.getBlockState(blockPos);
            if (!isAutoMineBlock(state1.getBlock())) // bedrock mine exploit!!
            {
                continue;
            }

            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > square(range.get())) continue;

            BlockState state2 = mc.world.getBlockState(blockPos.down());
            if (bedrockPhased || state2.isOf(Blocks.OBSIDIAN) || state2.isOf(Blocks.BEDROCK)) {
                Direction direction = strictDirection.get() ? mc.player.getHorizontalFacing() : Direction.UP;

                validInstantMines.add(new MineData(blockPos, direction));
            }
        }

        if (validInstantMines.isEmpty()) return null;

        return validInstantMines.peek();
    }

    public List<BlockPos> getPhaseBlocks(PlayerEntity player, BlockPos playerPos, boolean targetBedrockPhased)
    {
        List<BlockPos> phaseBlocks = GPositionUtils.getAllInBox(player.getBoundingBox(),
            targetBedrockPhased && head.get() ? playerPos.up() : playerPos);

        phaseBlocks.removeIf(p -> {
            BlockState state = mc.world.getBlockState(p);
            if (!isAutoMineBlock(state.getBlock()) || !canMine(state) || isMining(p)) return true;

            double dist = mc.player.getEyePos().squaredDistanceTo(p.toCenterPos());
            if (dist > square(range.get())) return true;

            return avoidSelf.get() && intersectsPlayer(p);
        });

        if (targetBedrockPhased && aboveHead.get()) phaseBlocks.add(playerPos.up(2));

        return phaseBlocks;
    }

    /**
     *
     * @param player
     * @return A {@link Set} of potential blocks to mine for an enemy player
     */
    public List<BlockPos> getMiningBlocks(PlayerEntity player, BlockPos playerPos, boolean bedrockPhased)
    {
        List<BlockPos> surroundingBlocks = Modules.get().get(GenyoSurround.class).getSurroundNoDown(player, range.get());
        List<BlockPos> miningBlocks;
        if (bedrockPhased)
        {
            List<BlockPos> facePlaceBlocks = new ArrayList<>();
            if (head.get()) {
                facePlaceBlocks.addAll(surroundingBlocks.stream().map(BlockPos::up).toList());
            }

            BlockState belowFeet = mc.world.getBlockState(playerPos.down());
            if (canMine(belowFeet))
            {
                facePlaceBlocks.add(playerPos.down());
            }
            miningBlocks = facePlaceBlocks;
        }
        else
        {
            miningBlocks = surroundingBlocks;
        }

        miningBlocks.removeIf(p -> avoidSelf.get() && intersectsPlayer(p));
        return miningBlocks;
    }

    private BlockPos getAntiCrawlPos(PlayerEntity playerTarget)
    {
        if (!mc.player.isOnGround())
        {
            return null;
        }
        BlockPos crawlingPos = GEntityUtils.getRoundedBlockPos(mc.player);
        boolean playerBelow = playerTarget != null && GEntityUtils.getRoundedBlockPos(playerTarget).getY() < crawlingPos.getY();
        // We want to be same level as our opponent
        if (playerBelow)
        {
            BlockState state = mc.world.getBlockState(crawlingPos.down());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.down();
            }
        }
        else
        {
            BlockState state = mc.world.getBlockState(crawlingPos.up());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.up();
            }
        }
        return null;
    }

    private boolean checkDataY(MineData data, BlockPos targetPos, boolean bedrockPhased)
    {
        return data.getGoal() == MiningGoal.MINING_ENEMY && !bedrockPhased && data.getPos().getY() != targetPos.getY();
    }

    private boolean intersectsPlayer(BlockPos pos)
    {
        List<BlockPos> playerBlocks = Modules.get().get(GenyoSurround.class).getPlayerBlocks(mc.player);
        List<BlockPos> surroundingBlocks = Modules.get().get(GenyoSurround.class).getSurroundNoDown(mc.player);
        return playerBlocks.contains(pos) || surroundingBlocks.contains(pos);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null && mc.world == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;

        if (instantMineAnim != null && instantMineAnim.animation().getFactor() > 0.01f)
        {
            renderMiningData(event.matrices, event.tickDelta, instantMineAnim, true, event.renderer);
        }

        if (doubleBreak.get() && packetMineAnim != null && packetMineAnim.animation().getFactor() > 0.01f)
        {
            renderMiningData(event.matrices, event.tickDelta, packetMineAnim, false, event.renderer);
        }
    }

    public void renderMiningData(MatrixStack matrixStack, float tickDelta, MineAnimation mineAnimation, boolean instantMine, Renderer3D renderer)
    {
        MineData data = mineAnimation.data();
        Animation animation = mineAnimation.animation();
        int boxAlpha = (int) (40 * animation.getFactor());
        int lineAlpha = (int) (100 * animation.getFactor());

        Color boxColor;
        Color lineColor;
        if (smoothColor.get()) {
            boxColor = !canMine(data.getState()) ? colorDone.get() : ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), colorDone.get(), color.get());
            lineColor = !canMine(data.getState()) ? colorDone.get() : ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), colorDone.get(), color.get());
        } else {
            boxColor = data.getBlockDamage() >= 0.95f || !canMine(data.getState()) ? colorDone.get() : color.get();
            lineColor = data.getBlockDamage() >= 0.95f || !canMine(data.getState()) ? colorDone.get() : color.get();
        }

        BlockPos mining = data.getPos();
        VoxelShape outlineShape = VoxelShapes.fullCube();

        if (!instantMine || data.getBlockDamage() < speed.get()) {
            outlineShape = data.getState().getOutlineShape(mc.world, mining);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
        }
        Box render1 = outlineShape.getBoundingBox();
        Vec3d center = render1.offset(mining).getCenter();
        float total = instantMine ? speed.get() : 1.0f;
        float scale = (instantMine && data.getBlockDamage() >= speed.get()) || !canMine(data.getState()) ? 1.0f :
            MathHelper.clamp((data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * tickDelta) / total, 0.0f, 1.0f);
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

        renderer.box(scaled, boxColor, lineColor, ShapeMode.Both, 0);
        //RenderManager.renderBox(matrixStack, scaled, boxColor);
        //RenderManager.renderBoundingBox(matrixStack, scaled, 1.5f, lineColor);
    }

    public void startMining(MineData data)
    {
        /*if (rotate.get())
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            if (grim.get())
            {
                setRotationSilent(rotations[0], rotations[1]);
            }
            else
            {
                setRotation(rotations[0], rotations[1]);
            }
        }*/

        if (doubleBreak.get())
        {
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
            if (grimNew.get())
            {
                if (anticheat.get()) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                } else {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                }

                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        }

        /*if (rotate.get() && grim.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }*/

        instantMineAnim = new MineAnimation(data, new Animation(true, fadeTime.get()));
    }

    public void abortMining(MineData data)
    {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public void stopMining(MineData data)
    {
        /*if (rotate.get())
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            if (grim.get())
            {
                setRotationSilent(rotations[0], rotations[1]);
            }
            else
            {
                setRotation(rotations[0], rotations[1]);
            }
        }*/

        int slot = data.getBestSlot();
        if (slot != -1)
        {
            swapTo(slot);
        }

        stopMiningInternal(data);

        if (slot != -1)
        {
            swapSync(slot);
        }

        /*if (rotate.get() && grim.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }*/
    }

    private void stopMiningInternal(MineData data)
    {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public boolean isInstantMineComplete()
    {
        return instantMine == null || instantMine.getBlockDamage() >= speed.get() && !canMine(instantMine.getState());
    }

    public BlockPos getMiningBlock()
    {
        if (instantMine != null)
        {
            double damage = instantMine.getBlockDamage() / speed.get();
            if (damage > 0.75)
            {
                return instantMine.getPos();
            }
        }
        return null;
    }

    private void swapTo(int slot)
    {
        switch (swap.get())
        {
            //case NORMAL -> Managers.INVENTORY.setClientSlot(slot);
            case NORMAL -> InvUtils.swap(slot, false);
            //case SILENT -> Managers.INVENTORY.setSlot(slot);
            case SILENT -> InvUtils.swap(slot, true);
        }
    }

    private void swapSync(int slot)
    {
        switch (swap.get())
        {
            case SILENT -> ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
        }
    }

    public boolean isSilentSwapping()
    {
        return packetSwapBack;
    }

    private boolean isMining(BlockPos blockPos)
    {
        return instantMine != null && instantMine.getPos().equals(blockPos) ||
            packetMine != null && packetMine.getPos().equals(blockPos);
    }

    private boolean isAutoMineBlock(Block block)
    {
        if (BlastResistantBlocks.isUnbreakable(block))
        {
            return false;
        }
        return switch (selection.get())
        {
            case WHITELIST -> whitelist.get().contains(block);
            case BLACKLIST -> !blacklist.get().contains(block);
            case ALL -> true;
        };
    }

    public boolean canMine(BlockState state)
    {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    public static class MineData implements Comparable<MineData>
    {
        private final MinecraftClient mc = MinecraftClient.getInstance();

        private final BlockPos pos;
        private final Direction direction;
        private final MiningGoal goal;
        //
        private int ticksMining;
        private float blockDamage, lastDamage;

        public MineData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = MiningGoal.MINING_ENEMY;
        }

        public MineData(BlockPos pos, Direction direction, MiningGoal goal)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = goal;
        }

        private double getPriority()
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(pos.down().toCenterPos());
            return 0.0f;
        }

        @Override
        public int compareTo(MineData o)
        {
            return Double.compare(getPriority(), o.getPriority());
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MineData d && d.getPos().equals(pos);
        }

        public void resetMiningTicks()
        {
            ticksMining = 0;
        }

        public void markAttemptedMine()
        {
            ticksMining++;
        }

        public void addBlockDamage(float blockDamage)
        {
            this.lastDamage = this.blockDamage;
            this.blockDamage += blockDamage;
        }

        public void setTotalBlockDamage(float blockDamage, float lastDamage)
        {
            this.blockDamage = blockDamage;
            this.lastDamage = lastDamage;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public MiningGoal getGoal()
        {
            return goal;
        }

        public int getTicksMining()
        {
            return ticksMining;
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public static MineData empty()
        {
            return new MineData(BlockPos.ORIGIN, Direction.UP);
        }

        public MineData copy()
        {
            final MineData data = new MineData(pos, direction, goal);
            data.setTotalBlockDamage(blockDamage, lastDamage);
            return data;
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public int getBestSlot()
        {
            return InvUtils.findFastestTool(getState()).slot();
        }
    }

    private float square(float value) {
        return value*value;
    }

    public record MineAnimation(MineData data, Animation animation) {}

    public enum MiningGoal {
        MANUAL,
        MINING_ENEMY,
        PREVENT_CRAWL
    }

    public enum RemineMode {
        INSTANT,
        NORMAL,
        FAST
    }

    public enum Selection {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    public enum Swap {
        NORMAL,
        SILENT,
        OFF
    }
}
