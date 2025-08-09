package com.genyo.addon.systems.screens;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.utils.math.MathUtil;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.text.TextReorderingProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MainMenuScreen extends Screen {

    private static final GuiRenderer RENDERER = new GuiRenderer();

    private final String splashText = "a";
    private final int buttonWidth = 80, buttonHeight = 16;

    public MainMenuScreen() {
        super(Text.literal(GenyoAddon.MOD_ID + "-menu"));

        //splashText = getSplashText();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        /*MatrixStack matrices = new MatrixStack();

        Renderer2D.COLOR.quad(0, 0, width, height, new Color(25, 25, 25, 255));
        TextRenderer.get().begin();
        TextRenderer.get().render("Fasz", 50, 50, Color.WHITE);*/
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        /*if(button == 0) {
            if(width/2f - Sydney.FONT_MANAGER.getWidth("Sydney") <= mouseX && height/2f - Sydney.FONT_MANAGER.getHeight()*2 - 5 <= mouseY && width/2f + Sydney.FONT_MANAGER.getWidth("Sydney") > mouseX && height/2f - 5 > mouseY) {
                try {
                    Util.getOperatingSystem().open(new URI("https://youtu.be/INE4RacaApQ?si=ShQU8VjfpgdxW8nb"));
                } catch (Exception ignored) { }
                playClickSound();
            }
            if(isHoveringButton(width/2f - buttonWidth - 2, height/2f, mouseX, mouseY)) {
                mc.setScreen(new SelectWorldScreen(this));
                playClickSound();
            }
            if(isHoveringButton(width/2f, height/2f, mouseX, mouseY)) {
                mc.setScreen(new MultiplayerScreen(this));
                playClickSound();
            }
            if(isHoveringButton(width/2f + buttonWidth + 2, height/2f, mouseX, mouseY)) {
                mc.setScreen(new OptionsScreen(this, mc.options));
                playClickSound();
            }
            if(isHoveringButton(width - buttonWidth/2f - 2, height - buttonHeight - 2, mouseX, mouseY)) {
                mc.scheduleStop();
                playClickSound();
            }
        }*/

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String getSplashText() {
        String splash = "";
        Identifier identifier = Identifier.of(GenyoAddon.MOD_ID, "splash.txt");

        try {
            Resource resource = mc.getResourceManager().getResource(identifier).orElseThrow();
            List<String> messages = resource.getReader().lines().toList();
            splash = messages.get((int) MathUtil.random(messages.size(), 0));
        } catch (Exception ignored) { }

        return splash;
    }

}
