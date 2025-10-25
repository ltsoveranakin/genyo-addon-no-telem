package com.genyo.mixin.block;

import com.genyo.events.block.BlockSlipperinessEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    /**
     * @param cir
     */
    @Inject(method = "getSlipperiness", at = @At(value = "RETURN"),
        cancellable = true)
    private void hookGetSlipperiness(CallbackInfoReturnable<Float> cir) {
        BlockSlipperinessEvent event = BlockSlipperinessEvent.get((Block) (Object) this, cir.getReturnValueF());
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(event.slipperiness);
        }
    }

}
