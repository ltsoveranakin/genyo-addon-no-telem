package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.managers.world.sound.SoundManager;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

import static com.genyo.managers.Managers.SOUND;

public class GenyoSounds extends GenyoModule {

    public GenyoSounds() {
        super(Genyo.MISC, "genyo-sounds", "Adjust the wonderful soundscapes of Genyo.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> fasz = sgGeneral.add(new BoolSetting.Builder()
        .name("Fasz")
        .description("Fasz")
        .defaultValue(false)
        .onChanged((a) -> epic())
        .build()
    );

    public final Setting<Integer> volume = sgGeneral.add(new IntSetting.Builder()
        .name("Volume")
        .description("Global volume")
        .min(0).defaultValue(100).max(100)
        .build()
    );

    private void epic() {
        SOUND.playSound(SoundManager.VINE);
    }
}
