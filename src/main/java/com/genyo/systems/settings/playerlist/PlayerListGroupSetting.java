package com.genyo.systems.settings.playerlist;

import com.genyo.Genyo;
import com.genyo.systems.screens.ListGroupScreen;
import com.genyo.utils.GenyoChatUtils;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayerListGroupSetting extends Setting<List<PlayerListGroup>> {

    public PlayerListGroupSetting(String name, String description, List<PlayerListGroup> defaultValue, Consumer<List<PlayerListGroup>> onChanged, Consumer<Setting<List<PlayerListGroup>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    public static void fillTable(GuiTheme theme, WTable table, PlayerListGroupSetting setting) {
        table.clear();

        ArrayList<PlayerListGroup> groups = new ArrayList<>(setting.get());

        for (int i = 0; i < setting.get().size(); i++) {
            int currentIndex = i;
            PlayerListGroup currentGroup = groups.get(currentIndex);

            WCheckbox toggle = table.add(theme.checkbox(currentGroup.isEnabled())).widget();
            toggle.action = () -> {
                currentGroup.setEnabled(toggle.checked);
                GenyoChatUtils.sendInfo("Set group '" + currentGroup.getGroupName() + "' to " + (toggle.checked ? "enabled" : "disabled."));
            };

            //table.add(theme.tooltip(currentGroup.getGroupName())).expandX();
            table.add(theme.label(currentGroup.getGroupName())).padHorizontal(20);
            //table.add(theme.verticalSeparator());

            WButton edit = table.add(theme.button("Edit")).expandX().widget();
            edit.action = () -> {
                mc.setScreen(new ListGroupScreen(theme, setting, currentIndex, mc.currentScreen));
            };


            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                groups.remove(currentIndex);
                setting.set(groups);

                fillTable(theme, table, setting);
            };

            table.row();
        }

        if (!setting.get().isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }

        WButton add = table.add(theme.button("Add Group")).expandX().widget();
        add.action = () -> {
            int currentNew = groups.size() + 1;
            String newName = "Group " + currentNew;
            groups.add(new PlayerListGroup(newName, "asd <NAME>", new ArrayList<ListPlayer>()));
            setting.set(groups);

            fillTable(theme, table, setting);
        };

        WButton reset =  table.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();

            fillTable(theme, table, setting);
        };
    }

    @Override
    protected List<PlayerListGroup> parseImpl(String s) {
        String[] values = s.split(",");
        return List.of();
    }

    @Override
    protected boolean isValueValid(List<PlayerListGroup> plGroups) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList indexes = new NbtList();
        if (get().isEmpty()) {
            indexes.add(null);
            tag.put("indexes", indexes);
            tag.put("message", null);
            tag.put("players", null);
            return tag;
        } else {
            for (int i = 0; i < get().size(); i++) {
                indexes.add(NbtInt.of(i));
            }
        }

        tag.put("indexes", indexes);

        NbtList groupNames = new NbtList();
        NbtList messages = new NbtList();
        NbtList players = new NbtList();

        for (int i = 0; i < indexes.size(); i++) {
            groupNames.add(i, NbtString.of(get().get(i).getGroupName()));
            messages.add(i, NbtString.of(get().get(i).getMessage()));
            NbtList playerNames = new NbtList();
            get().get(i).getPlayers().forEach(player -> {
                playerNames.add(NbtString.of(player.getName()));
            });
            players.addElement(i, playerNames);
        }

        tag.put("group_names", groupNames);
        tag.put("messages", messages);
        tag.put("players", players);
        return tag;
    }

    @Override
    protected List<PlayerListGroup> load(NbtCompound tag) {
        get().clear();
        NbtList nbtIndexes = (NbtList) tag.get("indexes");
        if (nbtIndexes != null && nbtIndexes.isEmpty()) {
            Genyo.LOG.info("nem jo");
            return get();
        }

        ArrayList<Integer> indexes = new ArrayList<>();
        for (NbtElement tagIndex : (NbtList) tag.get("indexes")) {
            NbtInt index = (NbtInt) tagIndex;
            indexes.add(index.intValue());
        }

        NbtList groupNames = (NbtList) tag.get("group_names");
        NbtList messages = (NbtList) tag.get("messages");
        for (int i = 0; i < indexes.size(); i++) {
            String msg = String.valueOf(messages.get(i).asString());
            String groupName = String.valueOf(groupNames.get(i).asString());

            List<ListPlayer> players = new ArrayList<>();
            NbtList playersList = (NbtList) tag.get("players");

            for (NbtElement val : playersList) {
                NbtList currentPlayers = (NbtList) playersList.get(i);
                currentPlayers.forEach(player -> {
                    ListPlayer listPlayer = new ListPlayer(String.valueOf(player.asString()));
                    players.add(listPlayer);
                });
            }

            PlayerListGroup newGroup = new PlayerListGroup(groupName, msg, players);
            get().add(newGroup);
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<PlayerListGroup>, PlayerListGroupSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(PlayerListGroup... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        @Override
        public PlayerListGroupSetting build() {
            return new PlayerListGroupSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }


}
