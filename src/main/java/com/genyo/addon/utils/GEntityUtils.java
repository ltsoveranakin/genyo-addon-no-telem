package com.genyo.addon.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
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

    public static boolean isVehicle(Entity e)
    {
        return e instanceof BoatEntity || e instanceof MinecartEntity
            || e instanceof FurnaceMinecartEntity
            || e instanceof ChestMinecartEntity;
    }

}
