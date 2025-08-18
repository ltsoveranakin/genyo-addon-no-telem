package com.genyo.mixin;

import com.genyo.events.network.ItemDesyncEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ItemUsageContext.class)
public final class MixinItemUsageContext {

    @Inject(method = "getStack", at = @At("RETURN"), cancellable = true)
    public void hookGetStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        MeteorClient.EVENT_BUS.post(itemDesyncEvent);

        if (mc.player != null && cir.getReturnValue().equals(mc.player.getMainHandStack()) && itemDesyncEvent.isCancelled())
        {
            cir.setReturnValue(itemDesyncEvent.stack);
        }
    }
}
