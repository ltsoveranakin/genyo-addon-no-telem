package com.genyo.managers.world.sound;

import com.genyo.managers.Managers;
import com.genyo.systems.modules.misc.GenyoSounds;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
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
    private final Timer timer = new CacheTimer();

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null) {
            mc.player.playSound(sound, (float) Modules.get().get(GenyoSounds.class).volume.get() / 100f, 1f);
        }
    }

    public void playSound(SoundEvent sound, float volume) {
        if (mc.player != null && mc.world != null) {
            mc.player.playSound(sound, volume, 1f);
        }
    }

    public static void init() {
        Registry.register(Registries.SOUND_EVENT, VINE_ID, VINE);
        Registry.register(Registries.SOUND_EVENT, SCREAM_ID, SCREAM);
        Registry.register(Registries.SOUND_EVENT, VERSTAPPEN_ID, VERSTAPPEN);
        Registry.register(Registries.SOUND_EVENT, HAMBURGER_ID, HAMBURGER);
        Registry.register(Registries.SOUND_EVENT, BLACK_ID, BLACK);
    }

    @EventHandler
    public void onMessageReceive(ReceiveMessageEvent event) {
        String message = event.getMessage().getString().toLowerCase();

        if (message.contains("genyo") && !message.startsWith("§")) {
            Managers.SOUND.playSound(VINE);
        } else if (message.contains("verstappen")) {
            Managers.SOUND.playSound(VERSTAPPEN);
        } else if ((message.contains("nigga") || message.contains("nigger")) && Modules.get().get(GenyoSounds.class).blackPerson.get()) {
            if (timer.passed(6000)) {
                Managers.SOUND.playSound(BLACK, 0.1f);
                timer.reset();
            }
        }
    }

}
