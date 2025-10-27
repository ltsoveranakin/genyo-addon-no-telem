package com.genyo.systems.config;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

public class GenyoTab extends Tab {

    public GenyoTab() {
        super("Genyo");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new GenyoScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof GenyoScreen;
    }

    public static class GenyoScreen extends WindowTabScreen {
        private final Settings settings;

        public GenyoScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            settings = GenyoConfig.get().settings;
            settings.onActivated();
        }

        @Override
        public void initWidgets() {
            add(theme.settings(settings)).expandX();
        }

        @Override
        public void tick() {
            super.tick();

            settings.tick(window, theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(GenyoConfig.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(GenyoConfig.get());
        }
    }

}
