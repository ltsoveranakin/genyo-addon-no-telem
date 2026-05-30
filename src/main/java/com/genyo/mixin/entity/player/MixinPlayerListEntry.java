package com.genyo.mixin.entity.player;

import com.genyo.systems.modules.visual.GenyoCapes;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
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
        if (!Modules.get().isActive(GenyoCapes.class)) return;

        GenyoCapes mod = Modules.get().get(GenyoCapes.class);
        String name = profile.name();

        boolean isSelf = name.equals(mc.player.getGameProfile().name())
            && profile.id().equals(mc.player.getGameProfile().id());
        boolean isDev = mod.isDev(name);

        if (!isDev && !isSelf && !mod.everyoneConfig.get()) return;

        Identifier textureId;
        Identifier texturePath;

        if (isDev) {
            textureId = Identifier.of("genyo", "cape_dev");
            texturePath = Identifier.of("genyo", "textures/cape_dev.png");
        } else {
            textureId = Identifier.of("genyo", "cape");
            texturePath = Identifier.of("genyo", "textures/cape.png");
        }

        SkinTextures original = info.getReturnValue();
        AssetInfo.TextureAssetInfo capeAsset = new AssetInfo.TextureAssetInfo(textureId, texturePath);

        info.setReturnValue(new SkinTextures(
            original.body(),
            capeAsset,
            capeAsset,
            original.model(),
            original.secure()
        ));
    }
}
