package com.genyo.systems.modules.world;

import com.genyo.GenyoAddon;
import com.genyo.events.AttackBlockEvent;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.player.EnchantmentUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.tag.ItemTags;

public class GenyoAutoTool extends GenyoModule {

    public GenyoAutoTool() {
        super(GenyoAddon.WORLD, "genyo-auto-tool", "Yés");
    }

    @EventHandler
    public void onBreakBlock(AttackBlockEvent event)
    {
        if (mc.world == null) return;

        final BlockState state = mc.world.getBlockState(event.pos);
        final int blockSlot = getBestToolNoFallback(state);
        if (blockSlot != -1)
        {
            mc.player.getInventory().setSelectedSlot(blockSlot);
        }
    }

    public int getBestTool(final BlockState state)
    {
        int slot = getBestToolNoFallback(state);
        if (slot != -1)
        {
            return slot;
        }
        return mc.player.getInventory().getSelectedSlot();
    }

    public int getBestToolNoFallback(final BlockState state)
    {
        if (state.getBlock() == Blocks.COBWEB)
        {
            for (int i = 0; i < 9; i++)
            {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || !(stack.isIn(ItemTags.SWORDS)))
                {
                    continue;
                }
                return i;
            }
        }
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !isTool(stack))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            final int efficiency = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (efficiency > 0)
            {
                speed += efficiency * efficiency + 1.0f;
            }
            if (speed > bestTool)
            {
                bestTool = speed;
                slot = i;
            }
        }
        return slot;
    }

    public static boolean isTool(ItemStack itemStack) {
        return itemStack.isIn(ItemTags.AXES) || itemStack.isIn(ItemTags.HOES) || itemStack.isIn(ItemTags.PICKAXES) || itemStack.isIn(ItemTags.SHOVELS) || itemStack.getItem() instanceof ShearsItem;
    }

}
