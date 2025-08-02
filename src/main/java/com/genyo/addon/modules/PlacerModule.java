package com.genyo.addon.modules;

import com.genyo.addon.managers.Managers;
import com.genyo.addon.managers.player.rotation.Rotation;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Category;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class PlacerModule extends GenyoModule {

    private final int rotationPriority;

    protected static final BlockState DEFAULT_OBSIDIAN_STATE = Blocks.OBSIDIAN.getDefaultState();
    // Blocks that can prevent explosion damage
    private static final List<Block> RESISTANT_BLOCKS = new LinkedList<>()
    {{
        add(Blocks.OBSIDIAN);
        add(Blocks.CRYING_OBSIDIAN);
        add(Blocks.ENDER_CHEST);
    }};

    protected final Setting<Boolean> multitaskConfig = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("Multitask")
        .description("Allows mining while using items")
        .defaultValue(false)
        .build()
    );

    protected final Setting<Boolean> strictDirectionConfig = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Places on visible sides only")
        .defaultValue(false)
        .build()
    );

    public PlacerModule(Category category, String name, String desc)
    {
        super(category, name, desc);
        this.rotationPriority = 100;
    }

    public PlacerModule(Category category, String name, String desc, int priority)
    {
        super(category, name, desc);
        this.rotationPriority = priority;
    }

    protected void setRotation(float yaw, float pitch)
    {
        Managers.ROTATION.setRotation(new Rotation(getRotationPriority(), yaw, pitch));
    }

    protected int getRotationPriority()
    {
        return rotationPriority;
    }

    protected void setRotationSilent(float yaw, float pitch)
    {
        Managers.ROTATION.setRotationSilent(yaw, pitch);
    }

    /**
     * Sets client look yaw and pitch
     *
     * @param yaw
     * @param pitch
     */
    protected void setRotationClient(float yaw, float pitch)
    {
        Managers.ROTATION.setRotationClient(yaw, pitch);
    }

    /**
     * @return
     */
    protected int getResistantBlockItem()
    {
        final Set<BlockSlot> blockSlots = new HashSet<>();
        for (final Block type : RESISTANT_BLOCKS)
        {
            final int slot = getBlockItemSlot(type);
            if (slot != -1)
            {
                blockSlots.add(new BlockSlot(type, slot));
            }
        }

        // Prioritize
        BlockSlot slot = blockSlots.stream().filter(b -> b.block() == Blocks.OBSIDIAN).findFirst().orElse(null);
        if (slot != null)
        {
            return slot.slot();
        }
        BlockSlot slot1 = blockSlots.stream().filter(b -> b.block() == Blocks.CRYING_OBSIDIAN).findFirst().orElse(null);
        if (slot1 != null)
        {
            return slot1.slot();
        }
        BlockSlot slot2 = blockSlots.stream().filter(b -> b.block() == Blocks.ENDER_CHEST).findFirst().orElse(null);
        if (slot2 != null)
        {
            return slot2.slot();
        }
        return -1;
    }

    public record BlockSlot(Block block, int slot)
    {
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof BlockSlot b && b.block() == block;
        }
    }

    protected int getSlot(final Predicate<ItemStack> filter)
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && filter.test(itemStack))
            {
                return i;
            }
        }
        return -1;
    }

    protected int getBlockItemSlot(final Block block)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() == block)
            {
                return i;
            }
        }
        return -1;
    }



}
