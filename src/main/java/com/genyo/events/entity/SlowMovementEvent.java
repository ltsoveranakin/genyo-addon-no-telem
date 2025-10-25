package com.genyo.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.BlockState;

public class SlowMovementEvent extends Cancellable {

    private static final SlowMovementEvent INSTANCE = new SlowMovementEvent();

    public BlockState state;
    public float multiplier = 1.0f;

    public static SlowMovementEvent get(BlockState state) {
        INSTANCE.state = state;

        return INSTANCE;
    }

}
