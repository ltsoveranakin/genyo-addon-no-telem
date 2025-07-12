package com.genyo.addon.hud;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.systems.incombat.CombatPerson;
import com.genyo.addon.systems.incombat.InCombatSystem;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InCombatHud extends HudElement {

    public static final HudElementInfo<InCombatHud> INFO = new HudElementInfo<>(GenyoAddon.HUD_GROUP, "combat-stats", "Fasz fasz fasz fasz fasz fasz.", InCombatHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<SettingColor> personColor = sgGeneral.add(new ColorSetting.Builder()
        .name("person-color")
        .description("jjjjjjjjjjjjjjjjjjjjjjjjjj")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<Boolean> textShadow = sgGeneral.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("asdasdasdsad fogyaték")
        .defaultValue(true)
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(0)
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

    public final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private InCombatHud() {
        super(INFO);

        calculateSize();
    }

    private void calculateSize() {
        setSize(40, 20);
    }

    @Override
    protected double alignX(double width, Alignment alignment) {
        return box.alignX(getWidth() - border.get() * 2, width, alignment);
    }

    @Override
    public void tick(HudRenderer renderer) {
        if (InCombatSystem.get().empty()) {
            setSize(renderer.textWidth("Out of Combat", textShadow.get(), getScale()), renderer.textHeight(textShadow.get(), getScale()));
            return;
        }

        double width = renderer.textWidth("In Combat:", textShadow.get(), getScale());
        double height = renderer.textHeight(textShadow.get(), getScale());

        if (mc.world == null) {
            setSize(width, height);
            return;
        }

        for (CombatPerson person : InCombatSystem.get().getInCombat()) {
            String text = person.getName();
            text += String.format("(%ss)", InCombatSystem.get().getRemainingCooldown());

            width = Math.max(width, renderer.textWidth(text, textShadow.get(), getScale()));
            height += renderer.textHeight(textShadow.get(), getScale()) + 2;
        }

        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double y = this.y + border.get();

        if (InCombatSystem.get().empty()) {
            renderer.text("Out of Combat", x, y, personColor.get(), textShadow.get(), getScale());
            return;
        }

        if (background.get()) renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());

        renderer.text("In Combat:", x + border.get() + alignX(renderer.textWidth("In Combat: ", textShadow.get(), getScale()), alignment.get()), y, new Color(255, 255, 255, 255), textShadow.get(), getScale());

        if (mc.world == null) return;
        double spaceWidth = renderer.textWidth(" ", textShadow.get(), getScale());

        for (CombatPerson person :  InCombatSystem.get().getInCombat()) {
            String text = person.getName();

            double width = renderer.textWidth(text, textShadow.get(), getScale());
            width += spaceWidth;

            String cooldownText = String.format("(%ss)", InCombatSystem.get().getRemainingCooldown());
            width += renderer.textWidth(cooldownText, textShadow.get(), getScale());

            double x = this.x + border.get() + alignX(width, alignment.get());
            y += renderer.textHeight(textShadow.get(), getScale()) + 2;

            x = renderer.text(text, x, y, personColor.get(), textShadow.get(), getScale());
            renderer.text(cooldownText, x + spaceWidth, y, personColor.get(), textShadow.get(), getScale());
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }



}
