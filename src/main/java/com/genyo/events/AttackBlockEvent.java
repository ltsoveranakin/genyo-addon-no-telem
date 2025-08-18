package com.genyo.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AttackBlockEvent extends Cancellable {
    private static final AttackBlockEvent INSTANCE = new AttackBlockEvent();

    public BlockPos pos;
    public BlockState state;
    public Direction direction;

    /**
     * @param pos
     * @param state
     * @param direction
     */
    public static AttackBlockEvent get(BlockPos pos, BlockState state, Direction direction) {
        INSTANCE.pos = pos;
        INSTANCE.state = state;
        INSTANCE.direction = direction;

        return INSTANCE;
    }
}
