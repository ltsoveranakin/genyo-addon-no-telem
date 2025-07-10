package com.genyo.addon.managers;

import com.genyo.addon.render.Render3DEngine;
import meteordevelopment.meteorclient.MeteorClient;

public class Managers {

    public static final CombatManager COMBAT = new CombatManager();
    public static final Render3DEngine ENGINE3D = new Render3DEngine();

    public static void subscribe() {
        MeteorClient.EVENT_BUS.subscribe(COMBAT);
        MeteorClient.EVENT_BUS.subscribe(ENGINE3D);
    }

}
