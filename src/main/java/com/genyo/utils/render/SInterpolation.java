package com.genyo.utils.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SInterpolation {

    /**
     * @param last
     * @param value
     * @param factor
     * @return
     */
    public static double interpolateDouble(double last, double value, double factor)
    {
        return last + ((value - last) * factor);
    }

    /**
     * @param lastBox
     * @param box
     * @return
     */
    public static Box getInterpolatedBox(Box lastBox, Box box)
    {

        double delta = mc.isPaused() ? 1f : mc.getRenderTickCounter().getTickProgress(true);

        return new Box(interpolateDouble(lastBox.minX, box.minX, delta),
            interpolateDouble(lastBox.minY, box.minY, delta),
            interpolateDouble(lastBox.minZ, box.minZ, delta),
            interpolateDouble(lastBox.maxX, box.maxX, delta),
            interpolateDouble(lastBox.maxY, box.maxY, delta),
            interpolateDouble(lastBox.maxZ, box.maxZ, delta));
    }

    /**
     * @param entity
     * @return
     */
    public static Box getInterpolatedEntityBox(Entity entity)
    {
        Box box = entity.getBoundingBox();
        Box lastBox = entity.getBoundingBox().offset(entity.lastX - entity.getX(), entity.lastY - entity.getY(), entity.lastZ - entity.getZ());
        return getInterpolatedBox(lastBox, box);
    }

}
