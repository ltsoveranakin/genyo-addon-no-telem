package com.genyo.addon.mixin;

import com.genyo.addon.events.PlayerTickEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/" +
        "minecraft/client/network/AbstractClientPlayerEntity;tick()V",
        shift = At.Shift.BEFORE, ordinal = 0))
    private void hookTickPre(CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(new PlayerTickEvent());
    }

}
