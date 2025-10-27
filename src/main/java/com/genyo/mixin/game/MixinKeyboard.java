package com.genyo.mixin.game;

import com.genyo.Genyo;
import com.genyo.events.keyboard.KeyPressEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean whitelist = mc.currentScreen == null || mc.currentScreen instanceof HudEditorScreen;
        if (!whitelist) return;

        if (action == 2) action = 1;

        switch (action) {
            /*case 0 -> {
                EventKeyRelease event = new EventKeyRelease(key, scanCode);
                ThunderHack.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }*/
            case 1 -> {
                if (MeteorClient.EVENT_BUS.post(KeyPressEvent.get(key, scanCode).isCancelled())) ci.cancel();
            }
        }
    }

}
