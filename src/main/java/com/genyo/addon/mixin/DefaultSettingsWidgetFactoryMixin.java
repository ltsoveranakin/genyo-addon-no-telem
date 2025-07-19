package com.genyo.addon.mixin;

import com.genyo.addon.screens.PlayerListScreen;
import com.genyo.addon.settings.PlayerListSetting;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(DefaultSettingsWidgetFactory.class)
public abstract class DefaultSettingsWidgetFactoryMixin extends SettingsWidgetFactory {

    @Shadow
    private void selectW(WContainer c, Setting<?> setting, Runnable action) {}

    public DefaultSettingsWidgetFactoryMixin(GuiTheme theme) {
        super(theme);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void genyo(CallbackInfo info) {
        factories.put(PlayerListSetting.class, (table, setting) -> playerListW(table, (PlayerListSetting) setting));
    }

    private void playerListW(WTable table, PlayerListSetting setting) {
        selectW(table, setting, () -> mc.setScreen(new PlayerListScreen(theme, "Add Players", setting)));
    }
}
