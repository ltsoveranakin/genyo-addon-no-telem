package com.genyo.events.render;

import net.minecraft.client.util.math.MatrixStack;

public class RenderWorldEvent {

    private static final RenderWorldEvent INSTANCE = new RenderWorldEvent();

    //
    public MatrixStack matrices;
    public float tickDelta;

    /**
     * @param matrices
     */
    public static RenderWorldEvent get(MatrixStack matrices, float tickDelta) {
        INSTANCE.matrices = matrices;
        INSTANCE.tickDelta = tickDelta;

        return INSTANCE;
    }

    public static class Game extends RenderWorldEvent
    {

        /**
         * @param matrices
         * @param tickDelta
         */
        public Game(MatrixStack matrices, float tickDelta)
        {
            RenderWorldEvent.get(matrices, tickDelta);
        }
    }

    public static class Hand extends RenderWorldEvent
    {
        /**
         * @param matrices
         * @param tickDelta
         */
        public Hand(MatrixStack matrices, float tickDelta)
        {
            RenderWorldEvent.get(matrices, tickDelta);
        }
    }

}
