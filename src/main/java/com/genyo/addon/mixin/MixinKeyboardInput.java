package com.genyo.addon.mixin;

import com.genyo.addon.events.keyboard.KeyboardTickEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    protected void hookTick$Pre(CallbackInfo ci)
    {
        KeyboardTickEvent event = KeyboardTickEvent.Pre.get((Input) (Object) this);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            ci.cancel();
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "TAIL"), cancellable = true)
    protected void hookTick$Post(CallbackInfo ci)
    {
        KeyboardTickEvent event = KeyboardTickEvent.Post.get((Input) (Object) this);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            ci.cancel();
        }
    }

}
