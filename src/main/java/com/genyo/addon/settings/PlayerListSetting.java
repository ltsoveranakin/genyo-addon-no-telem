package com.genyo.addon.settings;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.modules.GenyoWelcome;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PlayerListSetting extends Setting<List<GenyoWelcome.ListPlayer>> {

    public PlayerListSetting(String name, String description, List<GenyoWelcome.ListPlayer> defaultValue, Consumer<List<GenyoWelcome.ListPlayer>> onChanged, Consumer<Setting<List<GenyoWelcome.ListPlayer>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<GenyoWelcome.ListPlayer> parseImpl(String str) {
        String[] values = str.split(",");
        List<GenyoWelcome.ListPlayer> players = new ArrayList<>(values.length);

        try {
            for (String value : values) {
                if (GenyoWelcome.get(value) != null) players.add(GenyoWelcome.get(value));
            }
        } catch (Exception ignored) {}

        return players;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    public GenyoWelcome.ListPlayer get(String name) {
        for (GenyoWelcome.ListPlayer player : get()) {
            if (player.name.equalsIgnoreCase(name)) return player;
        }

        return null;
    }

    @Override
    protected boolean isValueValid(List<GenyoWelcome.ListPlayer> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (GenyoWelcome.ListPlayer player : get()) {
            valueTag.add(NbtString.of(player.getName()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public List<GenyoWelcome.ListPlayer> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            GenyoWelcome.ListPlayer player = GenyoWelcome.createListPlayer(tagI.asString());

            get().add(player);
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<GenyoWelcome.ListPlayer>, PlayerListSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(GenyoWelcome.ListPlayer... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        @Override
        public PlayerListSetting build() {
            return new PlayerListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }


}
