package com.genyo.events.block;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.block.Block;

public class BlockSlipperinessEvent extends Cancellable {

    private static final BlockSlipperinessEvent INSTANCE = new BlockSlipperinessEvent();

    public Block block;
    public float slipperiness;

    public static BlockSlipperinessEvent get(Block block, float slipperiness) {
        INSTANCE.block = block;
        INSTANCE.slipperiness = slipperiness;

        return INSTANCE;
    }

}
