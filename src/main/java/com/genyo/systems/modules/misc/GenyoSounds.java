package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class GenyoSounds extends GenyoModule {

    public GenyoSounds() {
        super(Genyo.MISC, "genyo-sounds", "Adjust the wonderful soundscapes of Genyo.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> volume = sgGeneral.add(new IntSetting.Builder()
        .name("Volume")
        .description("Global volume")
        .sliderRange(10, 100)
        .min(10).defaultValue(100).max(100)
        .build()
    );

    public final Setting<Boolean> blackPerson = sgGeneral.add(new BoolSetting.Builder()
        .name("Black Person")
        .description("When")
        .defaultValue(true)
        .build()
    );
}
