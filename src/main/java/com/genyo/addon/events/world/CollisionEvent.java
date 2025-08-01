package com.genyo.addon.events.world;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class CollisionEvent extends Cancellable {

    private static final CollisionEvent INSTANCE = new CollisionEvent();

    public BlockState state;
    public BlockPos pos;
    public VoxelShape shape;

    public static CollisionEvent get(BlockState bs, BlockPos bp, VoxelShape vs) {
        CollisionEvent event = INSTANCE;

        if (!RenderSystem.isOnRenderThread()) {
            event = new CollisionEvent();
        }

        event.setCancelled(false);
        event.state = bs;
        event.pos = bp;
        event.shape = vs;

        return event;
    }

}
