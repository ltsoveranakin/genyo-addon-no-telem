package com.genyo.addon.managers.network;

import com.genyo.addon.modules.misc.GenyoDiscord;
import com.genyo.addon.utils.GenyoChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

public class GDTogglerManager {

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Modules.get().isActive(GenyoDiscord.class)) {
            Modules.get().get(GenyoDiscord.class).toggle();
            GenyoChatUtils.sendInfo("Why would you turn it off?");
        }
    }

}
