package com.genyo.addon.render;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Render3DEngine {

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }

}
