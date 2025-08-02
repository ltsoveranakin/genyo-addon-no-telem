package com.genyo.addon.utils.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SInterpolation {

    /**
     * @param prev
     * @param value
     * @param factor
     * @return
     */
    public static double interpolateDouble(double prev, double value, double factor)
    {
        return prev + ((value - prev) * factor);
    }

    /**
     * @param prevBox
     * @param box
     * @return
     */
    public static Box getInterpolatedBox(Box prevBox, Box box)
    {

        double delta = mc.isPaused() ? 1f : mc.getRenderTickCounter().getTickDelta(true);

        return new Box(interpolateDouble(prevBox.minX, box.minX, delta),
            interpolateDouble(prevBox.minY, box.minY, delta),
            interpolateDouble(prevBox.minZ, box.minZ, delta),
            interpolateDouble(prevBox.maxX, box.maxX, delta),
            interpolateDouble(prevBox.maxY, box.maxY, delta),
            interpolateDouble(prevBox.maxZ, box.maxZ, delta));
    }

    /**
     * @param entity
     * @return
     */
    public static Box getInterpolatedEntityBox(Entity entity)
    {
        Box box = entity.getBoundingBox();
        Box prevBox = entity.getBoundingBox().offset(entity.prevX - entity.getX(), entity.prevY - entity.getY(), entity.prevZ - entity.getZ());
        return getInterpolatedBox(prevBox, box);
    }

}
