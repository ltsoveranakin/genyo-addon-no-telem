package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class AutoOminous extends GenyoModule {

    public AutoOminous() {
        super(Genyo.MISC, "auto-ominous", "");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    
}
