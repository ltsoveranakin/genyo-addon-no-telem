package com.genyo.systems.modules.visual;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.Identifier;

import java.util.Set;

public class GenyoCapes extends GenyoModule {

    private static final Set<String> DEV_NAMES = Set.of(
        "Awakeyv",
        "wuritz",
        "Barnika18"
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> everyoneConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Everyone")
        .description("Apply Genyo cape to all players.")
        .defaultValue(false)
        .build()
    );

    private final Identifier capeTexture;
    private final Identifier devCapeTexture;

    public GenyoCapes() {
        super(Genyo.VISUAL, "genyo-capes", "The thing");

        this.capeTexture = Identifier.of(Genyo.MOD_ID, "textures/cape.png");
        this.devCapeTexture = Identifier.of(Genyo.MOD_ID, "textures/cape_dev.png");
    }

    public Identifier getCapeTexture() {return capeTexture;}

    public Identifier getDevCapeTexture() {return devCapeTexture;}

    public boolean isDev(String username) {return DEV_NAMES.contains(username);}
}
