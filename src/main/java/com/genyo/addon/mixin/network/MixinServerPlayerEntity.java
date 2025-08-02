package com.genyo.addon.mixin.network;

import com.genyo.addon.events.world.LoadWorldEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Inject(method = "worldChanged", at = @At(value = "HEAD"))
    private void hookMoveToWorld(ServerWorld origin, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(new LoadWorldEvent());
    }
}
