package com.genyo.mixin.meteor;

import com.genyo.systems.enemies.Enemies;
import com.genyo.systems.enemies.Enemy;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BetterTab.class)
public abstract class MixinBetterTab {

    @Inject(method = "getPlayerName", at = @At("TAIL"), cancellable = true)
    private void injectGetPlayerName(PlayerListEntry playerListEntry, CallbackInfoReturnable<Text> cir) {
        if (Enemies.get().isEnemy(playerListEntry)) {
            Enemy enemy = Enemies.get().get(playerListEntry);
            if (enemy != null) {
                Text enemyName;
                Color enemyColor = Enemies.get().getEnemyColor();

                enemyName = playerListEntry.getDisplayName();
                if (enemyName == null) enemyName = Text.literal(playerListEntry.getProfile().name());

                String nameString = enemyName.getString();

                cir.setReturnValue(Text.literal(nameString).setStyle(enemyName.getStyle().withColor(TextColor.fromRgb(enemyColor.getPacked()))));
            }
        }
    }

}
