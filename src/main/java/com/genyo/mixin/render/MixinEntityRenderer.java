package com.genyo.mixin.render;

import com.genyo.systems.modules.visual.GenyoNametags;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(T entity, CallbackInfoReturnable<Text> cir) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (Modules.get().get(GenyoNametags.class).playerNametags() && !(EntityUtils.getGameMode(player) == null && Modules.get().get(GenyoNametags.class).excludeBots()))
            cir.setReturnValue(null);
    }

}
