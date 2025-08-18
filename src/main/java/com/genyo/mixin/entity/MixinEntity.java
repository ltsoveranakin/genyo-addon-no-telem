package com.genyo.mixin.entity;

import com.genyo.events.entity.player.PushEntityEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    private void hookPushAwayFrom(Entity entity, CallbackInfo ci)
    {
        PushEntityEvent pushEntityEvent = PushEntityEvent.get((Entity) (Object) this, entity);
        MeteorClient.EVENT_BUS.post(pushEntityEvent);

        if (pushEntityEvent.isCancelled()) ci.cancel();
    }
}
