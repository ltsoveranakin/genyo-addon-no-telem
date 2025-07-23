package com.genyo.addon.utils.world;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlastResistantBlocks {

    // All blocks that are unbreakable with tools in survival mode
    private static final Set<Block> UNBREAKABLE = new ReferenceOpenHashSet<>(Set.of(
        Blocks.BEDROCK,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.END_PORTAL_FRAME,
        Blocks.BARRIER
    ));

    /**
     * @param pos
     * @return
     */
    public static boolean isUnbreakable(BlockPos pos)
    {
        if (mc.world == null)
        {
            return false;
        }
        return isUnbreakable(mc.world.getBlockState(pos).getBlock());
    }

    /**
     * @param block
     * @return
     */
    public static boolean isUnbreakable(Block block)
    {
        return UNBREAKABLE.contains(block);
    }
}
