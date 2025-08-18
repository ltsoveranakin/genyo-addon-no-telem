package com.genyo.events.render;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.util.math.MatrixStack;

public class RenderShaderEvent extends Cancellable {

    private static final RenderShaderEvent INSTANCE = new RenderShaderEvent();

    public MatrixStack matrices;
    public float tickDelta;

    /**
     * @param matrices
     * @param tickDelta
     * @return
     */
    public static RenderShaderEvent get(MatrixStack matrices, float tickDelta) {
        INSTANCE.matrices = matrices;
        INSTANCE.tickDelta = tickDelta;

        return INSTANCE;
    }

    public static class BlockEntities extends RenderShaderEvent
    {
        /**
         * @param matrices
         * @param tickDelta
         */
        public BlockEntities(MatrixStack matrices, float tickDelta)
        {
            get(matrices, tickDelta);
        }
    }

}
