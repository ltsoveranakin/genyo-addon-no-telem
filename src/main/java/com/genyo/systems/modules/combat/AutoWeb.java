package com.genyo.systems.modules.combat;

import com.genyo.Genyo;
import com.genyo.events.network.DisconnectEvent;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.managers.Managers;
import com.genyo.render.animation.Animation;
import com.genyo.systems.modules.PlacerModule;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.math.MathUtil;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoWeb extends PlacerModule {

    public AutoWeb() {
        super(Genyo.COMBAT, "auto-web", "Automatically traps nearby entities in webs");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Float> range = sgGeneral.add(new FloatSetting.Builder()
        .name("place-range")
        .description("The range to fill nearby holes")
        .min(0.1f).defaultValue(4.0f).max(6.0f)
        .sliderRange(0.1f, 6.0f)
        .build()
    );

    private final Setting<Float> enemyRange = sgGeneral.add(new FloatSetting.Builder()
        .name("enemy-range")
        .description("The maximum range of targets")
        .min(0.1f).defaultValue(10.0f).max(15.0f)
        .sliderRange(0.1f, 15.0f)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to block before placing")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> coverHead = sgGeneral.add(new BoolSetting.Builder()
        .name("cover-head")
        .description("Places webs on the targets head")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> shiftTicks = sgGeneral.add(new IntSetting.Builder()
        .name("shift-ticks")
        .description("The number of blocks to place per tick")
        .min(1).defaultValue(2).max(5)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Integer> shiftDelay = sgGeneral.add(new IntSetting.Builder()
        .name("shift-delay")
        .description("The delay between each block placement interval")
        .min(0).defaultValue(1).max(5)
        .sliderRange(0, 5)
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

    private int shiftDelayInt;
    private List<BlockPos> webs = new ArrayList<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();

    @Override
    public void onDeactivate()
    {
        fadeList.clear();
        webs.clear();
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        toggle();
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (!multitask.get() && checkMultitask())
        {
            webs.clear();
            return;
        }

        int blocksPlaced = 0;
        int slot = getBlockItemSlot(Blocks.COBWEB);
        if (slot == -1)
        {
            webs.clear();
            return;
        }

        if (shiftDelayInt < shiftDelay.get())
        {
            shiftDelayInt++;
            return;
        }
        List<BlockPos> webPlacements = new ArrayList<>();
        for (PlayerEntity entity : mc.world.getPlayers())
        {
            if (entity == mc.player || Friends.get().isFriend(entity))
            {
                continue;
            }
            double d = mc.player.distanceTo(entity);
            if (d > enemyRange.get())
            {
                continue;
            }
            BlockPos feetPos = entity.getBlockPos();
            double dist = mc.player.getEyePos().squaredDistanceTo(feetPos.toCenterPos());
            if (mc.world.getBlockState(feetPos).isAir() && dist <= MathUtil.squared(range.get()))
            {
                webPlacements.add(feetPos);
            }
            if (coverHead.get())
            {
                BlockPos headPos = feetPos.up();
                double dist2 = mc.player.getEyePos().squaredDistanceTo(headPos.toCenterPos());
                if (mc.world.getBlockState(headPos).isAir() && dist2 <= MathUtil.squared(range.get()))
                {
                    webPlacements.add(headPos);
                }
            }
        }
        webs = webPlacements;
        if (webs.isEmpty())
        {
            return;
        }
        while (blocksPlaced < shiftTicks.get())
        {
            if (blocksPlaced >= webs.size())
            {
                break;
            }
            BlockPos targetPos = webs.get(blocksPlaced);
            blocksPlaced++;
            shiftDelayInt = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeWeb(targetPos, slot);
        }

        if (rotate.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event)
    {
        if (render.get())
        {
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int lineAlpha = (int) (120 * set.getValue().getFactor());
                BlockPos blockPos = set.getKey();
                event.renderer.box(blockPos, renderColor.get().a(lineAlpha), renderColor.get().a(lineAlpha), ShapeMode.Both, 1);
            }

            if (webs.isEmpty())
            {
                return;
            }

            for (BlockPos pos : webs)
            {
                Animation animation = new Animation(true, fadeTime.get());
                fadeList.put(pos, animation);
            }
        }

        fadeList.entrySet().removeIf(e ->
            e.getValue().getFactor() == 0.0);
    }

    private void placeWeb(BlockPos pos, int slot)
    {
        Managers.INTERACT.placeBlock(pos, slot, strictDirection.get(), false, (state, angles) ->
        {
            if (rotate.get() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
    }

    public boolean isPlacing()
    {
        return !webs.isEmpty();
    }

}
