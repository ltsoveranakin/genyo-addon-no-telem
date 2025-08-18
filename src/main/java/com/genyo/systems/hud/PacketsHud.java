package com.genyo.systems.hud;

import com.genyo.GenyoAddon;
import com.genyo.managers.Managers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class PacketsHud extends HudElement {

    public static final HudElementInfo<PacketsHud> INFO = new HudElementInfo<>(GenyoAddon.HUD_GROUP, "packets", "Displays the amount of incoming and outgoing packets. (out <- in)", PacketsHud::new);
    public PacketsHud() { super(INFO); }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private double originalWidth, originalHeight;

    public final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the text.")
        .defaultValue(0)
        .onChanged(integer -> super.setSize(originalWidth + integer * 2, originalHeight + integer * 2))
        .build()
    );

    private final Setting<Boolean> invertColors = sgGeneral.add(new BoolSetting.Builder()
        .name("Invert Colors")
        .description("Invert the colors")
        .defaultValue(true)
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

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    @Override
    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (isInEditor() && !isActive()) {
            renderer.line(x, y, x + getWidth(), y + getHeight(), Color.GRAY);
            renderer.line(x, y + getHeight(), x + getWidth(), y, Color.GRAY);
        }

        double x = this.x + border.get();
        double y = this.y + border.get();

        String packetsString = "Packets: ";
        renderer.text(packetsString, x, y, invertColors.get() ? Color.GRAY : Color.WHITE, shadow.get(), getScale());

        String thing = String.format("%s <- %s", Managers.NETWORK.getOutgoingPPS(), Managers.NETWORK.getIncomingPPS());
        renderer.text(thing, x + renderer.textWidth(packetsString, shadow.get(), getScale()) + (renderer.textWidth(" ") * getScale()), y, invertColors.get() ? Color.WHITE : Color.GRAY, shadow.get(), getScale());

        setSize(renderer.textWidth(packetsString + thing), renderer.textHeight(shadow.get(), getScale()));

        if (background.get()) {
            renderer.quad(this.x, y, getWidth(), getHeight(), backgroundColor.get());
        }
    }

}
