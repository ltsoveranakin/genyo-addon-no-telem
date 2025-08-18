package com.genyo.systems.modules.world;

import com.genyo.GenyoAddon;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.PlacerModule;
import com.genyo.render.animation.Animation;
import com.genyo.utils.math.GPositionUtils;
import com.genyo.utils.player.MovementUtil;
import com.genyo.utils.player.RotationUtil;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenyoScaffold extends PlacerModule {

    public GenyoScaffold() {
        super(GenyoAddon.WORLD, "genyo-scaffold", "i shit the bed regularly");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotateConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Allow to rotate")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotateHoldConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate Hold")
        .description("Holds rotations to scaffold blocks")
        .defaultValue(false)
        .visible(rotateConfig::get)
        .build()
    );

    private final Setting<Boolean> grimConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim")
        .description("Uses grim interactions")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grimNewConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim V3")
        .description("Uses grim new interactions")
        .defaultValue(false)
        .build()
    );

    private final Setting<Selection> selectionConfig = sgGeneral.add(new EnumSetting.Builder<Selection>()
        .name("Selection")
        .description("The selection of blocks to use for scaffold")
        .defaultValue(Selection.ALL)
        .build()
    );

    private final Setting<List<Block>> whitelistConfig = sgGeneral.add(new BlockListSetting.Builder()
        .name("Whitelist")
        .description("Valid block whitelist")
        .defaultValue(Blocks.DIRT, Blocks.OBSIDIAN)
        .build()
    );

    private final Setting<List<Block>> blacklistConfig = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blacklist")
        .description("Valid block blacklist")
        .defaultValue(Blocks.SHULKER_BOX)
        .build()
    );

    private final Setting<Boolean> keepYConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Keep Y")
        .description("Keeps the same y-level")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> towerConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Tower")
        .description("Goes up faster when holding down space")
        .defaultValue(true)
        .visible(() -> !grimNewConfig.get())
        .build()
    );

    private final Setting<BlockPicker> pickerConfig = sgGeneral.add(new EnumSetting.Builder<BlockPicker>()
        .name("Block Selection")
        .description("How to pick a block from the hotbar")
        .defaultValue(BlockPicker.NORMAL)
        .build()
    );

    private final Setting<Boolean> renderConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders where scaffold is placing blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> fadeTimeConfig = sgGeneral.add(new IntSetting.Builder()
        .name("Fade-Time")
        .description("Timer for the fade")
        .min(0)
        .defaultValue(250)
        .max(1000)
        .visible(() -> false)
        .build()
    );

    private final Setting<Boolean> stopMotionConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Stop Motion")
        .description("Stops player motion when placing blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Render Color")
        .description("asdsadsadsadsadsa")
        .defaultValue(new Color(236, 243, 122, 40))
        .build()
    );

    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private BlockData blockData;
    private BlockData renderData;
    private float[] lastAngles;
    private int groundPosY;

    @Override
    public void onDeactivate() {
        if (mc.player != null) Managers.INVENTORY.syncToClient();

        groundPosY = -1;
        lastAngles = null;
        blockData = null;
        renderData = null;
        fadeList.clear();
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event)
    {

        if (!multitaskConfig.get() && checkMultitask())
        {
            blockData = null;
            renderData = null;
            return;
        }

        BlockSlot blockSlot = getBlockSlot();
        int slot = blockSlot.slot();
        if (slot == -1)
        {
            blockData = null;
            renderData = null;
            return;
        }
        renderData = getBlockData(false);
        blockData = getBlockData(rotateHoldConfig.get());
        if (blockData == null)
        {
            if (grimNewConfig.get() && rotateConfig.get())
            {
                float yaw = mc.player.getYaw();
                if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed())
                {
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    // Forward movement - no change to yaw
                }
                else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed())
                {
                    yaw += 180.0f;
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                }
                else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                {
                    yaw -= 90.0f;
                }
                else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                {
                    yaw += 90.0f;
                }
                setRotation(MathHelper.wrapDegrees(yaw), 90.0f);
            }
            return;
        }

        calcRotations(blockData);
        if (blockData.getAngles() == null)
        {
            if (!isGrim() && rotateConfig.get() && lastAngles != null)
            {
                setRotation(lastAngles[0], lastAngles[1]);
            }
            return;
        }

        if (!isGrim() && Managers.INVENTORY.getServerSlot() != slot)
        {
            Managers.INVENTORY.setSlot(slot);
        }

        Vec3d prevMotion = mc.player.getVelocity();
        if (stopMotionConfig.get())
        {
            mc.player.setVelocity(0.0, 0.0, 0.0);
        }

        boolean result = Managers.INTERACT.placeBlock(blockData.getBlockPos(), slot, false, false, false, (state, angles) ->
        {
            if (rotateConfig.get())
            {
                final float[] rotations = blockData.getAngles();
                if (rotations == null)
                {
                    return;
                }
                lastAngles = rotations;
                if (state)
                {
                    if (grimConfig.get())
                    {
                        Managers.ROTATION.setRotationSilent(rotations[0], rotations[1]);
                    }
                    else
                    {
                        setRotation(rotations[0], rotations[1]);
                    }
                }
                else if (grimConfig.get())
                {
                    Managers.ROTATION.setRotationSilentSync();
                }
            }
        });

        if (stopMotionConfig.get())
        {
            mc.player.setVelocity(prevMotion);
        }

        if (result)
        {
            if (!isGrim() && towerConfig.get() && mc.options.jumpKey.isPressed())
            {
                final Vec3d velocity = mc.player.getVelocity();
                final double velocityY = velocity.y;
                if ((mc.player.isOnGround() || velocityY < 0.1) || velocityY <= 0.16477328182606651)
                {
                    mc.player.setVelocity(velocity.x, 0.42f, velocity.z);
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!renderConfig.get() || fadeList.entrySet().isEmpty()) return;

        for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
        {
            set.getValue().setState(false);
            int boxAlpha = (int) (40 * set.getValue().getFactor());
            int lineAlpha = (int) (100 * set.getValue().getFactor());

            Color boxColor = color.get().a(boxAlpha);
            Color lineColor = color.get().a(lineAlpha);

            event.renderer.box(set.getKey(), boxColor, lineColor, ShapeMode.Both, 0);
        }

        if (renderData == null || renderData.getHitResult() == null) return;

        Animation animation = new Animation(true, fadeTimeConfig.get());
        fadeList.put(renderData.getBlockPos(), animation);

        fadeList.entrySet().removeIf(e ->
            e.getValue().getFactor() == 0.0);
    }

    private void calcRotations(final BlockData blockData)
    {
        final BlockPos pos = blockData.getHitResult().getBlockPos();
        final Direction side = blockData.getHitResult().getSide();
        final Vec3d basicHitVec = pos.toCenterPos()
            .add(side.getOffsetX() * 0.5f, side.getOffsetY() * 0.5f, side.getOffsetZ() * 0.5f);
        blockData.setAngles(RotationUtil.getRotationsTo(mc.player.getEyePos(), basicHitVec));
        blockData.setHitResult(new BlockHitResult(basicHitVec, side, pos, false));
    }

    private BlockData getBlockData(boolean hold)
    {
        int posY = (int) Math.round(mc.player.getY()) - 1;
        if (keepYConfig.get() && MovementUtil.isInputtingMovement())
        {
            if (mc.player.isOnGround() || groundPosY == -1)
            {
                groundPosY = (int) Math.floor(mc.player.getY()) - 1;
            }
            posY = groundPosY;
        }
        final BlockPos pos = GPositionUtils.getRoundedBlockPos(
            mc.player.getX(), posY, mc.player.getZ());
        if (!hold && !mc.world.getBlockState(pos).isReplaceable())
        {
            return null;
        }
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (!mc.world.getBlockState(neighbor).isReplaceable())
            {
                return BlockData.basic(neighbor, direction.getOpposite());
            }
        }
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (mc.world.getBlockState(neighbor).isReplaceable())
            {
                for (final Direction direction1 : Direction.values())
                {
                    final BlockPos neighbor1 = neighbor.offset(direction1);
                    if (!mc.world.getBlockState(neighbor1).isReplaceable())
                    {
                        return BlockData.basic(neighbor1, direction1.getOpposite());
                    }
                }
            }
        }
        return null;
    }

    private BlockSlot getBlockSlot()
    {
        final ItemStack serverStack = Managers.INVENTORY.getServerItem();
        if (!serverStack.isEmpty() && serverStack.getItem() instanceof BlockItem blockItem && validScaffoldBlock(blockItem.getBlock()))
        {
            return new BlockSlot(blockItem.getBlock(), Managers.INVENTORY.getServerSlot());
        }

        Block block = null;
        int blockSlot = -1;
        int count = 0;
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem && validScaffoldBlock(blockItem.getBlock()))
            {
                if (pickerConfig.get() == BlockPicker.NORMAL)
                {
                    return new BlockSlot(blockItem.getBlock(), i);
                }

                if (blockSlot == -1 || itemStack.getCount() > count)
                {
                    block = blockItem.getBlock();
                    blockSlot = i;
                    count = itemStack.getCount();
                }
            }
        }

        return new BlockSlot(block, blockSlot);
    }

    private boolean validScaffoldBlock(Block block)
    {
        return switch (selectionConfig.get())
        {
            case WHITELIST -> whitelistConfig.get().contains(block);
            case BLACKLIST -> !blacklistConfig.get().contains(block);
            case ALL -> true;
        };
    }

    private static class BlockData
    {
        private BlockHitResult hitResult;
        private float[] angles;

        public BlockData(final BlockHitResult hitResult, final float[] angles)
        {
            this.hitResult = hitResult;
            this.angles = angles;
        }

        public BlockHitResult getHitResult()
        {
            return hitResult;
        }

        public BlockPos getBlockPos()
        {
            return hitResult.getBlockPos().offset(hitResult.getSide());
        }

        public void setHitResult(BlockHitResult hitResult)
        {
            this.hitResult = hitResult;
        }

        public float[] getAngles()
        {
            return angles;
        }

        public void setAngles(float[] angles)
        {
            this.angles = angles;
        }

        public static BlockData basic(final BlockPos pos, final Direction direction)
        {
            return new BlockData(new BlockHitResult(pos.toCenterPos(), direction, pos, false), null);
        }
    }

    public boolean isGrim()
    {
        return grimConfig.get() || grimNewConfig.get();
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    private enum BlockPicker
    {
        NORMAL,
        GREATEST
    }

    private record BlockSlot(Block block, int slot) {}

}
