package com.genyo.addon.events.world;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class BlockCollisionEvent extends Cancellable {

    private static final BlockCollisionEvent INSTANCE = new BlockCollisionEvent();

    public BlockPos pos;
    public BlockState state;
    public VoxelShape shape;

    public static BlockCollisionEvent get(BlockPos pos, BlockState state, VoxelShape shape) {
        INSTANCE.pos = pos;
        INSTANCE.state = state;
        INSTANCE.shape = shape;

        return INSTANCE;
    }

    public Block getBlock() {
        return state.getBlock();
    }

    public void setVoxelShape(VoxelShape shape) {
        this.shape = shape;
    }

}
