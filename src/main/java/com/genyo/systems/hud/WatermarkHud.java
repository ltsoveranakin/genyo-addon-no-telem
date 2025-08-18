package com.genyo.systems.hud;

import com.genyo.GenyoAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class WatermarkHud extends HudElement {

    public static final HudElementInfo<WatermarkHud> INFO = new HudElementInfo<>(GenyoAddon.HUD_GROUP, "watermark", "The best thing in the entire addon.", WatermarkHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("The")
        .defaultValue(Color.WHITE)
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

    public WatermarkHud() {
        super(INFO);
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    @Override
    public void tick(HudRenderer renderer) {
        setSize(renderer.textWidth("Genyo " + GenyoAddon.VERSION, shadow.get(), getScale()), renderer.textHeight(shadow.get(), getScale()));
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.text("Genyo ", x, y, color.get(), shadow.get(), getScale());
        renderer.text(GenyoAddon.VERSION.toString(), x + renderer.textWidth("Genyo ", shadow.get(), getScale()), y, color.get(), shadow.get(), getScale());
    }

}
