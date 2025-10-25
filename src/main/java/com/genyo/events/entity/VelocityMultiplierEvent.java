package com.genyo.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class VelocityMultiplierEvent extends Cancellable {

    private static final VelocityMultiplierEvent INSTANCE = new VelocityMultiplierEvent();

    public BlockState state;

    public static VelocityMultiplierEvent get(BlockState state) {
        INSTANCE.state = state;

        return INSTANCE;
    }

    /**
     * @return
     */
    public Block getBlock() {
        return state.getBlock();
    }

}
