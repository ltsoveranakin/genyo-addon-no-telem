package com.genyo.addon.mixin.imixins;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntity {

    @Accessor("lastYaw")
    @Mutable
    float getLastSpoofedYaw();

    @Accessor("lastPitch")
    @Mutable
    float getLastSpoofedPitch();
}
