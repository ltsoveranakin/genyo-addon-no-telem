package com.genyo.mixin.game;

import com.genyo.systems.config.GenyoConfig;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(SplashTextResourceSupplier.class)
public abstract class MixinSplashTextResourceSupplier {

    @Unique
    private boolean override = true;
    @Unique
    private static final Random random = new Random();
    @Unique
    private final List<String> splashes = getSplashes();

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashTextRenderer> cir) {
        if (GenyoConfig.get() == null || !GenyoConfig.get().useGenyoSplashes.get()) return;

        if (override) cir.setReturnValue(new SplashTextRenderer(splashes.get(random.nextInt(splashes.size()))));
        override = !override;
    }

    @Unique
    private static List<String> getSplashes() {
        return List.of(
            "genyo",
            "Genyo :D :D",
            "uhhhhhhhhhhhhh",
            "I love kiwi.",
            "hulkenberg",
            "HULKENBERGGGG",
            "TU TU TU TU",
            "Best addon ever :O",
            "§6Verstappen §fis god",
            "Yuki's kinda short",
            "§4we dont have a website",
            "§4Yay"
        );
    }

}
