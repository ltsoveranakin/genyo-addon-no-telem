package com.genyo.addon.mixin.game;

import com.genyo.addon.events.RunTickEvent;
import com.genyo.addon.systems.modules.misc.GenyoMainMenu;
import com.genyo.addon.systems.screens.MainMenuScreen;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    /*@Unique
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof DeathScreen && player != null) {
            player.requestRespawn();
            info.cancel();
        }

        if (screen instanceof TitleScreen) {
            //Sydney.checkForUpdates(); auto updater

            if (Modules.get().isActive(GenyoMainMenu.class)) {
                this.setScreen(new MainMenuScreen());
                info.cancel();
            }
        }
    }

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
