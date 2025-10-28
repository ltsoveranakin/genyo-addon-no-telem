package com.genyo.api;

import com.genyo.managers.Managers;
import com.genyo.managers.SoundManager;
import com.genyo.systems.config.GenyoConfig;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

import static com.genyo.managers.SoundManager.*;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class Core {

    private final Timer soundTimer = new CacheTimer();

    public Core() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (!mc.inGameHud.getChatHud().isChatFocused()) return;
        if (event.action == KeyAction.Press && GenyoConfig.get().typing.get()) {
            switch (GenyoConfig.get().typingFile.get()) {
                case Genyo -> Managers.SOUND.playSound(KEYPRESS_GENYO, GenyoConfig.get().typingVolume.get());
                case ThunderHack -> Managers.SOUND.playSound(SoundManager.KEYPRESS_TH, GenyoConfig.get().typingVolume.get());
            }
        }
    }

    @EventHandler
    public void onMessageReceive(ReceiveMessageEvent event) {
        String message = event.getMessage().getString().toLowerCase();

        if (message.contains("genyo") && !message.startsWith("§")) {
            Managers.SOUND.playSound(VINE);
        } else if (message.contains("verstappen")) {
            Managers.SOUND.playSound(VERSTAPPEN);
        } else if ((message.contains("nigga") || message.contains("nigger")) &&
            (GenyoConfig.get().blackPerson.get()) && GenyoConfig.get() != null) {
            if (soundTimer.passed(6000)) {
                Managers.SOUND.playSound(BLACK, 10);
                soundTimer.reset();
            }
        }
    }

}
