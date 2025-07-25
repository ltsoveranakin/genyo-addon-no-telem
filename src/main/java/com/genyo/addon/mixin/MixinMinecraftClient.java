package com.genyo.addon.mixin;

import com.genyo.addon.events.RunTickEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    /**
     * @param ci
     */
    @Inject(method = "run", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.BEFORE))
    private void hookRun(CallbackInfo ci)
    {
        final RunTickEvent runTickEvent = new RunTickEvent();
        MeteorClient.EVENT_BUS.post(runTickEvent);
    }

}
