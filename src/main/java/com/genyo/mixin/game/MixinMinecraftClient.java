package com.genyo.mixin.game;

import com.genyo.events.RunTickEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
