package com.genyo.render;

import net.minecraft.client.render.BufferBuilder;

import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DEngine {

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }
    // Remove endBuilding entirely — it can't work without BufferRenderer
// All callers in Render3DEngine need to be replaced
   /*  public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null)
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
    } */

}
