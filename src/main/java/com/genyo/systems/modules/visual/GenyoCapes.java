package com.genyo.systems.modules.visual;

import com.genyo.GenyoAddon;
import com.genyo.systems.modules.GenyoModule;
import net.minecraft.util.Identifier;

public class GenyoCapes extends GenyoModule {

    private final Identifier capeTexture;

    public GenyoCapes() {
        super(GenyoAddon.VISUAL, "genyo-capes", "The thing");

        this.capeTexture = Identifier.of(GenyoAddon.MOD_ID, "textures/cape.png");
    }

    public Identifier getCapeTexture() {
        return capeTexture;
    }
}
