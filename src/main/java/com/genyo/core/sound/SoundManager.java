package com.genyo.core.sound;

import com.genyo.systems.config.GenyoConfig;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SoundManager {

    public static final Identifier VINE_ID = Identifier.of("genyo:vine");
    public static final SoundEvent VINE = SoundEvent.of(VINE_ID);

    public static final Identifier SCREAM_ID = Identifier.of("genyo:scream");
    public static final SoundEvent SCREAM = SoundEvent.of(SCREAM_ID);

    public static final Identifier VERSTAPPEN_ID = Identifier.of("genyo:verstappen");
    public static final SoundEvent VERSTAPPEN = SoundEvent.of(VERSTAPPEN_ID);

    public static final Identifier HAMBURGER_ID = Identifier.of("genyo:hamburger");
    public static final SoundEvent HAMBURGER = SoundEvent.of(HAMBURGER_ID);

    public static final Identifier BLACK_ID = Identifier.of("genyo:blackperson");
    public static final SoundEvent BLACK = SoundEvent.of(BLACK_ID);

    public static final Identifier KEYPRESS_ID = Identifier.of("genyo:keypress");
    public static final SoundEvent KEYPRESS = SoundEvent.of(KEYPRESS_ID);

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null && GenyoConfig.get() != null) {
            mc.player.playSound(sound, (float) GenyoConfig.get().globalVolume.get() / 100f, 1f);
        }
    }

    public void playSound(SoundEvent sound, int volume) {
        if (mc.player != null && mc.world != null) {
            mc.player.playSound(sound, volume / 100f, 1f);
        }
    }

    public void playSound(SoundEvent sound, int volume, float pitch) {
        if (mc.player != null && mc.world != null) {
            mc.player.playSound(sound, volume / 100f, pitch);
        }
    }

    public static void init() {
        Registry.register(Registries.SOUND_EVENT, VINE_ID, VINE);
        Registry.register(Registries.SOUND_EVENT, SCREAM_ID, SCREAM);
        Registry.register(Registries.SOUND_EVENT, VERSTAPPEN_ID, VERSTAPPEN);
        Registry.register(Registries.SOUND_EVENT, HAMBURGER_ID, HAMBURGER);
        Registry.register(Registries.SOUND_EVENT, BLACK_ID, BLACK);
        Registry.register(Registries.SOUND_EVENT, KEYPRESS_ID, KEYPRESS);
    }

}
