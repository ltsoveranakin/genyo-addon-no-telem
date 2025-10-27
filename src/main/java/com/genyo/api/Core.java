package com.genyo.api;

import com.genyo.managers.Managers;
import com.genyo.managers.world.sound.SoundManager;
import com.genyo.systems.config.GenyoConfig;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class Core {

    public Core() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (!mc.inGameHud.getChatHud().isChatFocused()) return;
        if (event.action == KeyAction.Press && GenyoConfig.get().typing.get()) Managers.SOUND.playSound(SoundManager.KEYPRESS, GenyoConfig.get().typingVolume.get());
    }

}
