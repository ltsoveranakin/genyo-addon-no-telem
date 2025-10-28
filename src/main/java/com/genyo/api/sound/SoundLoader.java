package com.genyo.api.sound;

import com.genyo.managers.SoundManager;
import net.fabricmc.api.ModInitializer;

public class SoundLoader implements ModInitializer {

    @Override
    public void onInitialize() {
        SoundManager.init();
    }

}
