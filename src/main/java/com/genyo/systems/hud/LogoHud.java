package com.genyo.systems.hud;

import com.genyo.Genyo;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class LogoHud extends HudElement {

    public static final HudElementInfo<LogoHud> INFO = new HudElementInfo<>(Genyo.HUD_GROUP, "logo", "Acts like a badge of honor or idk", LogoHud::new);
    public LogoHud() {
        super(INFO);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the scale of scale.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    private final Identifier logo = Identifier.of(Genyo.MOD_ID, "genyo512.png");

    @Override
    public void render(HudRenderer renderer) {
        setSize(256 * scale.get(), 256 * scale.get());
        MatrixStack matrices = new MatrixStack();

        GL.bindTexture(logo);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, 256 * scale.get(), 256 * scale.get(), new Color(255, 255, 255, 255));
        Renderer2D.TEXTURE.render(matrices);
    }
}
