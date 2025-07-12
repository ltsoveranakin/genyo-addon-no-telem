package com.genyo.addon.systems.incombat;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.client.gui.screen.Screen;

public class InCombatTab extends Tab {

    public InCombatTab() {
        super("InCombat");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new InCombatTab.InCombatScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof InCombatTab.InCombatScreen;
    }

    private static class InCombatScreen extends WindowTabScreen {
        private final Settings settings;

        public InCombatScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            settings = InCombatSystem.get().settings;
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
    }



}
