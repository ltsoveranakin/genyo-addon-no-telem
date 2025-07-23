package com.genyo.addon.screens;

import com.genyo.addon.settings.playerlist.ListGroupSetting;
import com.genyo.addon.settings.playerlist.ListPlayer;
import com.genyo.addon.settings.playerlist.PLGroup;
import com.genyo.addon.utils.GenyoChatUtils;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ListGroupScreen extends WindowScreen {

    protected final ListGroupSetting setting;
    private WTable table;
    protected final int index;
    private PLGroup currentGroup;
    protected final Screen beforeScreen;

    public ListGroupScreen(GuiTheme theme, ListGroupSetting setting, int index, Screen beforeScreen) {
        super(theme, "Edit " + setting.get().get(index).getGroupName());

        this.index = index;
        this.setting = setting;
        currentGroup = setting.get().get(index);
        this.beforeScreen = beforeScreen;
    }

    @Override
    public void initWidgets() {
        table = add(theme.table()).expandX().minWidth(400).widget();

        table.add(theme.label("Status"));
        table.add(theme.label((currentGroup.isEnabled() ? "Enabled" : "Disabled")).color(currentGroup.isEnabled() ? Color.GREEN : Color.RED));
        table.row();

        table.add(theme.label("Name")).widget();
        WTextBox textBox = table.add(theme.textBox(currentGroup.getGroupName())).expandX().widget();
        textBox.action = () -> currentGroup.setGroupName(textBox.get());
        textBox.actionOnUnfocused = this::confirmChanges;

        table.row();

        table.add(theme.label("Message")).widget();
        WTextBox messageTB = table.add(theme.textBox(currentGroup.getMessage())).expandX().widget();
        messageTB.action = () -> currentGroup.setMessage(messageTB.get());
        messageTB.actionOnUnfocused = this::confirmChanges;

        table.row();
        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        table.add(theme.label("Players"));
        table.row();

        // New
        WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

        WTextBox nameW = list.add(theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
        //nameW.setFocused(true);

        WPlus add = list.add(theme.plus()).widget();
        add.action = () -> {
            String name = nameW.get().trim();
            if (name.equalsIgnoreCase("")) return;

            ListPlayer player = new ListPlayer(name);
            if (currentGroup.containsPlayer(player)) { // it already exists
                nameW.set("");
                nameW.setFocused(true);
                GenyoChatUtils.sendError("Player '" + name + "' already exists in '" +  currentGroup.getGroupName() + "'!");
                return;
            }

            nameW.set("");

            currentGroup.addPlayer(player);

            confirmChanges();
        };

        table.row();

        initTable(table);

        enterAction = add.action;

        table.row();
        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        WButton save = table.add(theme.button("Save")).expandX().widget();
        save.action = () -> {
            confirmChanges();
            mc.setScreen(beforeScreen);
        };
    }

    private void initTable(WTable table) {
        if (setting.get().isEmpty()) return;

        currentGroup.getPlayers().forEach(player ->
            MeteorExecutor.execute(() -> {
                if (player == null) {
                    currentGroup.removePlayer(player);
                    confirmChanges();
                    return;
                }
                if (player.headTextureNeedsUpdate()) {
                    player.updateInfo(false);
                    reload();
                }
            })
        );

        for (ListPlayer player : currentGroup.getPlayers()) {
            if (player == null) {
                currentGroup.removePlayer(player);
                confirmChanges();
                return;
            }
            table.add(theme.texture(32, 32, player.getHead().needsRotate() ? 90 : 0, player.getHead()));
            table.add(theme.label(player.getName()));

            WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
            remove.action = () -> {
                currentGroup.removePlayer(player);

                confirmChanges();
                table.clear();
            };

            table.row();
        }
    }

    private void confirmChanges() {
        setting.get().set(index, currentGroup);
        MeteorExecutor.execute(this::reload);
        setting.onChanged();
    }

    protected WWidget getValueWidget(ListPlayer value) {
        return theme.label(getValueName(value));
    }

    protected String getValueName(ListPlayer value) {
        return value.getName();
    }

}
