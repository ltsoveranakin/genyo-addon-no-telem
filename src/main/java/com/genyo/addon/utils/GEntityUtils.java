package com.genyo.addon.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class GEntityUtils {

    /**
     *
     * @param entity
     * @return
     */
    public static BlockPos getRoundedBlockPos(Entity entity) {
        return new BlockPos(entity.getBlockX(), (int) Math.round(entity.getY()), entity.getBlockZ());
    }

}
