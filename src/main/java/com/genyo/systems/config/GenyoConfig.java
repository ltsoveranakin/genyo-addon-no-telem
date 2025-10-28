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

    public final Setting<Boolean> typing = sgSounds.add(new BoolSetting.Builder()
        .name("typing")
        .description("Typing sound in chat")
        .defaultValue(false)
        .build()
    );

    public final Setting<TypingFile> typingFile = sgSounds.add(new EnumSetting.Builder<TypingFile>()
        .name("typing-file")
        .description("Choose the source file of the typing sound.")
        .defaultValue(TypingFile.ThunderHack)
        .visible(typing::get)
        .build()
    );

    public final Setting<Integer> typingVolume = sgSounds.add(new IntSetting.Builder()
        .name("typing-volume")
        .description("Adjust the volume of the typing sound")
        .min(1).defaultValue(100).max(100)
        .sliderRange(1, 100)
        .visible(typing::get)
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

    public enum TypingFile {
        ThunderHack,
        Genyo
    }

}
