package com.genyo.addon.mixin;

import com.genyo.addon.settings.FloatSetting;
import com.genyo.addon.settings.playerlist.ListGroupSetting;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WDoubleEdit;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultSettingsWidgetFactory.class)
public abstract class DefaultSettingsWidgetFactoryMixin extends SettingsWidgetFactory {

    @Shadow
    private void selectW(WContainer c, Setting<?> setting, Runnable action) {}

    @Shadow
    private void reset(WContainer c, Setting<?> setting, Runnable action) {}

    public DefaultSettingsWidgetFactoryMixin(GuiTheme theme) {
        super(theme);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void genyo(CallbackInfo info) {
        factories.put(ListGroupSetting.class, (table, setting) -> listGroupW(table, (ListGroupSetting) setting));
        factories.put(FloatSetting.class, (table, setting) -> floatW(table, (FloatSetting) setting));
    }

    @Unique
    private void listGroupW(WTable table, ListGroupSetting setting) {
        WTable wtable = table.add(theme.table()).expandX().widget();
        ListGroupSetting.fillTable(theme, wtable, setting);
    }

    @Unique
    private void floatW(WTable table, FloatSetting setting) {
        WDoubleEdit edit = theme.doubleEdit(setting.get(), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider);
        table.add(edit).expandX();

        Runnable action = () -> {
            if (!setting.set((float) edit.get())) edit.set(setting.get());
        };

        if (setting.onSliderRelease) edit.actionOnRelease = action;
        else edit.action = action;

        reset(table, setting, () -> edit.set(setting.get()));
    }
}
