package com.genyo.systems.modules.combat;

import com.genyo.Genyo;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.managers.Managers;
import com.genyo.render.animation.Animation;
import com.genyo.systems.modules.PlacerModule;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.GEntityUtils;
import com.genyo.utils.entity.EntityUtil;
import com.genyo.utils.math.MathUtil;
import com.genyo.utils.world.BlastResistantBlocks;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import java.util.*;

public class AutoCrawlTrap extends PlacerModule {

    public AutoCrawlTrap() {
        super(Genyo.COMBAT, "auto-crawl-trap", "Places blocks to keep enemies in crawl");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTicks = settings.createGroup("Ticks");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to block before placing")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> range = sgGeneral.add(new FloatSetting.Builder()
        .name("place-range")
        .description("The range to trap enemies")
        .min(0.1f).defaultValue(4.0f).max(6.0f)
        .sliderRange(0.1f, 6.0f)
        .build()
    );

    private final Setting<Float> enemyRange = sgGeneral.add(new FloatSetting.Builder()
        .name("enemy-range")
        .description("The maximum range of targets")
        .min(0.1f).defaultValue(10f).max(15f)
        .sliderRange(0.1f, 15f)
        .build()
    );

    private final Setting<Boolean> down = sgGeneral.add(new BoolSetting.Builder()
        .name("prevent-downwards")
        .description("Prevents diggin downwards")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> serverHitbox = sgGeneral.add(new BoolSetting.Builder()
        .name("hitbox-sync")
        .description("Places on server-side crawling hithoxes")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> mineIgnore = sgGeneral.add(new BoolSetting.Builder()
        .name("prevent-mine")
        .description("Prevents enemies from mining the trap")
        .defaultValue(false)
        .build()
    );

    // Ticks

    private final Setting<Integer> shiftTicks = sgTicks.add(new IntSetting.Builder()
        .name("shift-ticks")
        .description("The number of blocks to place per tick")
        .min(1).defaultValue(2).max(10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Float> shiftDelay = sgTicks.add(new FloatSetting.Builder()
        .name("shift-delay")
        .description("The delay between each block placement interval")
        .min(0f).defaultValue(1f).max(5f)
        .sliderRange(0f, 5f)
        .build()
    );

    private final Setting<Integer> extrapolateTicks = sgTicks.add(new IntSetting.Builder()
        .name("extrapolation-ticks")
        .description("Accounts for motion when calculating enemy positions, not fully accurate")
        .min(0).defaultValue(0).max(10)
        .sliderRange(0, 10)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders web placements")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> renderColor = sgRender.add(new ColorSetting.Builder()
        .name("render-color")
        .description("The render color")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .visible(render::get)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Time to fade")
        .min(0).defaultValue(250).max(1000)
        .sliderRange(0, 1000)
        .visible(render::get)
        .build()
    );

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private PlayerEntity target;
    private int blocksPlaced;

    @Override
    public void onDeactivate()
    {
        surround.clear();
        placements.clear();
        fadeList.clear();
        target = null;
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;

        if (!multitask.get() && checkMultitask())
        {
            surround.clear();
            placements.clear();
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            surround.clear();
            placements.clear();
            return;
        }
        target = getClosestPlayer(enemyRange.get());
        if (target == null)
        {
            surround.clear();
            placements.clear();
            return;
        }

        BlockPos targetPos = GEntityUtils.getRoundedBlockPos(target);
        surround = getCrawlTrap(target, targetPos);
        if (!canCrawlTrap(target, targetPos) || surround.isEmpty())
        {
            return;
        }

        placements = getPlacementsFromTrap(surround);
        if (placements.isEmpty())
        {
            return;
        }
        placements.sort(Comparator.comparingInt(Vec3i::getY));
        while (blocksPlaced < shiftTicks.get())
        {
            if (blocksPlaced >= placements.size())
            {
                break;
            }
            BlockPos targetPlacePos = placements.get(blocksPlaced);
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPlacePos, slot);
        }

        if (rotate.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.packet instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handlePackets(packet1);
            }
        }
        else
        {
            handlePackets(event.packet);
        }
    }

    private void handlePackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState blockState = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos))
            {
                if (blockState.isReplaceable() && mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, targetPos, ShapeContext.absent()))
                {
                    final int slot = getResistantBlockItem();
                    if (slot == -1)
                    {
                        return;
                    }
                    placeBlock(targetPos, slot);
                }
                else if (BlastResistantBlocks.isBlastResistant(blockState))
                {
                    packets.remove(targetPos);
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, int slot)
    {
        Managers.INTERACT.placeBlock(pos, slot, strictDirection.get(), false, true, (state, angles) ->
        {
            if (rotate.get() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
        packets.put(pos, System.currentTimeMillis());
        blocksPlaced++;
    }

    public List<BlockPos> getPlacementsFromTrap(List<BlockPos> surround)
    {
        List<BlockPos> placements = new ArrayList<>();
        for (BlockPos surroundPos : surround)
        {
            Long placed = packets.get(surroundPos);
            if (shiftDelay.get() > 0.0f && placed != null && System.currentTimeMillis() - placed < shiftDelay.get() * 50.0f)
            {
                continue;
            }

            final Box surroundBox = new Box(surroundPos);
            List<Entity> invalid = mc.world.getOtherEntities(null, surroundBox).stream().filter(e -> invalidEntity(e)).toList();

            if (!mc.world.getBlockState(surroundPos).isReplaceable()
                && !(Managers.BLOCK.isPassed(surroundPos, 0.7f) && mineIgnore.get()))
            {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(surroundPos.toCenterPos());
            if (dist > MathUtil.squared(range.get()))
            {
                continue;
            }

            if (mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, surroundPos, ShapeContext.absent()))
            {
                placements.add(surroundPos);
            }
        }

        return placements;
    }

    public List<BlockPos> getCrawlTrap(PlayerEntity entity, BlockPos playerPos)
    {
        final List<BlockPos> crawlTrap = new ArrayList<>();
        crawlTrap.add(playerPos.up());
        if (down.get())
        {
            crawlTrap.add(playerPos.down());
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        int ticks = 0;
        while (ticks <= extrapolateTicks.get())
        {
            double ox = (x - entity.lastX) * ticks;
            double oz = (z - entity.lastZ) * ticks;
            BlockPos blockPos = BlockPos.ofFloored(x + ox, y, z + oz);
            if (!crawlTrap.contains(blockPos.up()))
            {
                crawlTrap.add(blockPos.up());
            }
            if (down.get() && !crawlTrap.contains(blockPos.down()))
            {
                crawlTrap.add(blockPos.down());
            }
            ticks++;
        }
        return crawlTrap;
    }

    public boolean invalidEntity(Entity entity)
    {
        return !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof ArrowEntity);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        fullNullCheck();

        if (render.get())
        {
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = renderColor.get().a(boxAlpha);
                Color lineColor = renderColor.get().a(lineAlpha);
                event.renderer.box(set.getKey(), boxColor, lineColor, ShapeMode.Both, -1);
            }

            if (placements.isEmpty())
            {
                return;
            }

            for (BlockPos pos : placements)
            {
                Animation animation = new Animation(true, fadeTime.get());
                fadeList.put(pos, animation);
            }
        }

        fadeList.entrySet().removeIf(e ->
            e.getValue().getFactor() == 0.0);
    }

    private boolean canCrawlTrap(PlayerEntity player, BlockPos playerPos)
    {
        return player.isOnGround() || !mc.world.getBlockState(playerPos.up()).isReplaceable() || !mc.world.getBlockState(playerPos.up(2)).isReplaceable();
    }

    public boolean isPlacing()
    {
        return !placements.isEmpty();
    }

}
