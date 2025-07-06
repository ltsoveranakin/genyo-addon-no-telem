package com.genyo.addon.managers;

import meteordevelopment.meteorclient.MeteorClient;

public class Managers {

    public static final CombatManager COMBAT = new CombatManager();

    public static void subscribe() {
        MeteorClient.EVENT_BUS.subscribe(COMBAT);
    }

}
