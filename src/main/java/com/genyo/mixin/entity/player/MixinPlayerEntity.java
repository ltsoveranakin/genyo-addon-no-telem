package com.genyo.mixin.entity.player;

import com.genyo.events.entity.player.PushFluidsEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    /**
     * @param cir
     */
    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"),
        cancellable = true)
    private void hookIsPushedByFluids(CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this != mc.player)
        {
            return;
        }

        PushFluidsEvent pushFluidsEvent = new PushFluidsEvent();
        MeteorClient.EVENT_BUS.post(pushFluidsEvent);
        if (pushFluidsEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
