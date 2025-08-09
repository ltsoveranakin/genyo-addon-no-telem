package com.genyo.addon.systems.modules.world;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.AttackBlockEvent;
import com.genyo.addon.systems.modules.GenyoModule;
import com.genyo.addon.utils.player.EnchantmentUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;

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
            mc.player.getInventory().selectedSlot = blockSlot;
        }
    }

    public int getBestTool(final BlockState state)
    {
        int slot = getBestToolNoFallback(state);
        if (slot != -1)
        {
            return slot;
        }
        return mc.player.getInventory().selectedSlot;
    }

    public int getBestToolNoFallback(final BlockState state)
    {
        if (state.getBlock() == Blocks.COBWEB)
        {
            for (int i = 0; i < 9; i++)
            {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem))
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
            if (stack.isEmpty() || !(stack.getItem() instanceof MiningToolItem))
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

}
