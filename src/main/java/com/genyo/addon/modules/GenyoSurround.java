package com.genyo.addon.modules;

import com.genyo.addon.GenyoAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GenyoSurround extends GenyoModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> disableOnJump = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-jump")
        .description("auto disable on jump waoooo")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("tick delay between placements")
        .defaultValue(1)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("how many blocks to place per tick")
        .defaultValue(1)
        .sliderRange(1, 4)
        .build()
    );

    private final Setting<Boolean> renderEsp = sgRender.add(new BoolSetting.Builder()
        .name("render-box")
        .description("render a box around the placed blocks")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("waooooooooooooo")
        .visible(renderEsp::get)
        .defaultValue(new Color(255, 0, 0, 150))
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("ewfkjmhewhfhewfjhewkjfhewfewhfjkewhfkew")
        .visible(renderEsp::get)
        .defaultValue(new Color(255, 0, 0, 50))
        .build()
    );

    private final Setting<Integer> espDisplayTime = sgRender.add(new IntSetting.Builder()
        .name("esp-display-time")
        .description("render delay (in ticks) after placement")
        .defaultValue(40)
        .visible(renderEsp::get)
        .sliderRange(0, 200)
        .build()
    );

    private final Setting<Integer> espFadeTime = sgRender.add(new IntSetting.Builder()
        .name("esp-fade-time")
        .description("esp fade time in ticks")
        .defaultValue(20)
        .sliderRange(0, espDisplayTime.get())
        .visible(renderEsp::get)
        .build()
    );

    private int delayTicks = 0;
    private List<BlockPos> targetPositions = new ArrayList<>();
    private final Map<BlockPos, Long> espBlocks = new ConcurrentHashMap<>();

    public GenyoSurround() {
        super(GenyoAddon.GENYO, "genyo-surround", "haaaaaaaaaaaaaa");
    }

    public void onActivate() {
        super.onActivate();
        delayTicks = 0;
        espBlocks.clear();
        if (mc.player != null && mc.world != null) {
            BlockPos playerPos = mc.player.getBlockPos();
            List<BlockPos> initialTargetPositions = Arrays.asList(playerPos.north(), playerPos.south(), playerPos.east(), playerPos.west());

            for (BlockPos pos : initialTargetPositions) {
                if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                    espBlocks.put(pos, System.currentTimeMillis() + (long) espDisplayTime.get() * 50L);
                }
            }
        }

    }

    public void onDeactivate() {
        super.onDeactivate();
        espBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null && mc.world == null) return;
        if (mc.interactionManager == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        targetPositions = Arrays.asList(playerPos.north(), playerPos.south(), playerPos.east(), playerPos.west());

        for (BlockPos pos : targetPositions) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                espBlocks.put(pos, System.currentTimeMillis() + (long) espDisplayTime.get() * 50L);
            } else {
                espBlocks.remove(pos);
            }
        }

        ++delayTicks;
        if (delayTicks >= placeDelay.get()) {
            delayTicks = 0;
            if (disableOnJump.get() && mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
                sendInfo("Player jumped, disabling.");
                toggle();
            } else {
                FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

                if (!obsidian.found()) {
                    sendError("Obsidian not found in hotbar! Disabling.");
                    toggle();
                } else {
                    int placedCount = 0;
                    for (BlockPos placePos : targetPositions) {
                        if (placedCount >= blocksPerTick.get()) break;

                        if (canPlaceAt(placePos) && BlockUtils.place(placePos, obsidian, true, 0, true)) {
                            ++placedCount;
                            espBlocks.put(placePos, System.currentTimeMillis() + (long) espDisplayTime.get() * 50L);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null && mc.world == null) return;
        if (!renderEsp.get()) return;

        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, Long> entry : espBlocks.entrySet()) {
            BlockPos pos = entry.getKey();

            long expirationTime = entry.getValue();
            long currentTime = System.currentTimeMillis();
            long timeLeftMs = expirationTime - currentTime;

            if (timeLeftMs <= 0L) {
                toRemove.add(pos);
            } else {
                int originalLineAlpha = lineColor.get().a;
                int originalSideAlpha = sideColor.get().a;
                double fadeDurationMs = (double) ((long) espFadeTime.get() * 50L);
                double displayDurationMs = (double) ((long) espDisplayTime.get() * 50L);
                double progress = 1.0D;
                if ((double) timeLeftMs <= fadeDurationMs) {
                    progress = (double) timeLeftMs / fadeDurationMs;
                }

                progress = Math.max(0.0D, Math.min(1.0D, progress));
                Color currentLineColor = lineColor.get().copy();
                Color currentSideColor = sideColor.get().copy();
                currentLineColor.a = (int) ((double) originalLineAlpha * progress);
                currentSideColor.a = (int) ((double) originalSideAlpha * progress);
                event.renderer.box(pos, currentSideColor, currentLineColor, ShapeMode.Sides, 1);
                event.renderer.box(pos, currentSideColor, currentLineColor, ShapeMode.Lines, 1);
            }
        }

        for (BlockPos pos : toRemove) {
            espBlocks.remove(pos);
        }
    }

    private boolean canPlaceAt(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR;
    }

}
