package com.genyo.addon.screens;

import com.genyo.addon.modules.GenyoWelcome;
import com.genyo.addon.settings.PlayerListSetting;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;

public class PlayerListScreen extends WindowScreen {

    protected final PlayerListSetting setting;
    private WTable table;

    public PlayerListScreen(GuiTheme theme, String title, PlayerListSetting setting) {
        super(theme, title);

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        table = add(theme.table()).expandX().minWidth(400).widget();

        initTable(table);

        // New
        WHorizontalList list = add(theme.horizontalList()).expandX().widget();

        WTextBox nameW = list.add(theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
        nameW.setFocused(true);

        WPlus add = list.add(theme.plus()).widget();
        add.action = () -> {
            String name = nameW.get().trim();
            if (name.equalsIgnoreCase("")) return;
            if (setting.get(name) != null) { // it already exists
                nameW.set("");
                return;
            }

            GenyoWelcome.ListPlayer player = GenyoWelcome.createListPlayer(name);
            nameW.set("");
            setting.get().add(player);

            //player.updateInfo();
            MeteorExecutor.execute(this::reload);

            setting.onChanged();
            //table.clear();
        };

        enterAction = add.action;
    }

    private void initTable(WTable table) {
        table.clear();
        if (setting.get().isEmpty()) return;

        setting.get().forEach(player ->
            MeteorExecutor.execute(() -> {
                if (player == null) {
                    setting.get().remove(player);
                    return;
                }
                if (player.headTextureNeedsUpdate()) {
                    player.updateInfo();
                    reload();
                }
            })
        );

        for (GenyoWelcome.ListPlayer player : setting.get()) {
            if (player == null) {
                setting.get().remove(player);
                return;
            }
            table.add(theme.texture(32, 32, player.getHead().needsRotate() ? 90 : 0, player.getHead()));
            table.add(theme.label(player.getName()));

            WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
            remove.action = () -> {
                setting.get().remove(player);

                setting.onChanged();
                table.clear();
                reload();
            };

            table.row();
        }
    }

    protected WWidget getValueWidget(GenyoWelcome.ListPlayer value) {
        return theme.label(getValueName(value));
    }

    protected String getValueName(GenyoWelcome.ListPlayer value) {
        return value.getName();
    }

}
