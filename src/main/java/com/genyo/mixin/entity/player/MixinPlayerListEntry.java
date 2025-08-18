package com.genyo.mixin.entity.player;

import com.genyo.systems.modules.visual.GenyoCapes;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry {

    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
    private void getSkinTextures(CallbackInfoReturnable<SkinTextures> info) {
        if (((profile.getName().equals(mc.player.getGameProfile().getName())
            && profile.getId().equals(mc.player.getGameProfile().getId())))
            && Modules.get().isActive(GenyoCapes.class)
            && Modules.get().get(GenyoCapes.class).getCapeTexture() != null)
        {
            Identifier identifier = Modules.get().get(GenyoCapes.class).getCapeTexture();
            SkinTextures texture = info.getReturnValue();

            info.setReturnValue(new SkinTextures(texture.texture(), texture.textureUrl(), identifier, identifier, texture.model(), texture.secure()));
        }
    }

}
