package com.genyo.systems.modules.misc;

import com.genyo.GenyoAddon;
import com.genyo.events.network.DecodePacketEvent;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.orbit.EventHandler;

public class GenyoNoPacketKick extends GenyoModule {

    public GenyoNoPacketKick() {
        super(GenyoAddon.MISC, "genyo-no-packet-kick", "Prevents getting kicked by packets");
    }

    @EventHandler
    public void onDecodePacket(DecodePacketEvent event) {
        event.cancel();
    }

}
