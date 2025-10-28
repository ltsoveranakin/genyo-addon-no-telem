package com.genyo.core.sound;

import net.fabricmc.api.ModInitializer;

public class SoundLoader implements ModInitializer {

    @Override
    public void onInitialize() {
        SoundManager.init();
    }

}
