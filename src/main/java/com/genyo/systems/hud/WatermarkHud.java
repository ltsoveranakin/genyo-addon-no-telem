package com.genyo.systems.hud;

import com.genyo.Genyo;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.ModuleInfosHud;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class WatermarkHud extends HudElement {

    public static final HudElementInfo<WatermarkHud> INFO = new HudElementInfo<>(Genyo.HUD_GROUP, "watermark", "The best thing in the entire addon.", WatermarkHud::new);
    public WatermarkHud() {
        super(INFO);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Boolean> version = sgGeneral.add(new BoolSetting.Builder()
        .name("Version")
        .description("Render the current Genyo version")
        .defaultValue(false)
        .onChanged(this::updateRenderText)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    // Color

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    // Scale

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this hud element.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    private String renderText = "Genyo";

    private void updateRenderText(boolean version) {
        if (version) renderText = "Genyo " + Genyo.VERSION;
        else renderText = "Genyo";
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    @Override
    public void tick(HudRenderer renderer) {
        setSize(renderer.textWidth(renderText, shadow.get(), getScale()), renderer.textHeight(shadow.get(), getScale()));
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.text(renderText, x, y, color.get(), shadow.get(), getScale());
    }

}
