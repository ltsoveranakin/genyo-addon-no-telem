package com.genyo.systems.config;

import com.genyo.Genyo;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;

public class GenyoConfig extends System<GenyoConfig> {

    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<TextPosition> textPosition = sgGeneral.add(new EnumSetting.Builder<TextPosition>()
        .name("text-position")
        .description("Position of the Title Screen text")
        .defaultValue(TextPosition.Top)
        .build()
    );

    // General
    private final SettingGroup sgVisual = settings.createGroup("Visual");

    // Visual
    public final Setting<Boolean> useGenyoSplashes = sgVisual.add(new BoolSetting.Builder()
        .name("use-genyo-splashes")
        .description("Use Genyo's custom splash texts in the title screen.")
        .defaultValue(true)
        .build()
    );

    public GenyoConfig() {
        super("genyo-config");
    }

    public static GenyoConfig get() {
        return Systems.get(GenyoConfig.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("version", Genyo.VERSION.toString());
        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public GenyoConfig fromTag(NbtCompound tag) {
        if (tag.contains("settings")) tag.getCompound("settings").ifPresent(settings::fromTag);

        return this;
    }

    public enum TextPosition {
        Top, Center
    }

}
