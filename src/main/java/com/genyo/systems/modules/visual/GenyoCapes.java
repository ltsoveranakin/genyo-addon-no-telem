package com.genyo.systems.modules.visual;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import net.minecraft.util.Identifier;

public class GenyoCapes extends GenyoModule {

    private final Identifier capeTexture;

    public GenyoCapes() {
        super(Genyo.VISUAL, "genyo-capes", "The thing");

        this.capeTexture = Identifier.of(Genyo.MOD_ID, "textures/cape.png");
    }

    public Identifier getCapeTexture() {
        return capeTexture;
    }
}
