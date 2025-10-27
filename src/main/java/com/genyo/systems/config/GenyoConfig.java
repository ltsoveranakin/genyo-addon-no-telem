package com.genyo.systems.config;

import com.genyo.Genyo;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;

public class GenyoConfig extends System<GenyoConfig> {

    public final Settings settings = new Settings();

    private final SettingGroup sgSounds = settings.createGroup("Sounds");
    private final SettingGroup sgVisual = settings.createGroup("Visual");

    // Sounds

    public final Setting<Integer> globalVolume = sgSounds.add(new IntSetting.Builder()
        .name("global-volume")
        .description("Adjust the global volume of Genyo sounds.")
        .sliderRange(10, 100)
        .min(10).defaultValue(100).max(100)
        .build()
    );

    public final Setting<Boolean> blackPerson = sgSounds.add(new BoolSetting.Builder()
        .name("black-person")
        .description("Detect when black person")
        .defaultValue(true)
        .build()
    );

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
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));

        return this;
    }

}
