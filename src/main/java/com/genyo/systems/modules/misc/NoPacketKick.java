package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.events.network.DecodePacketEvent;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.orbit.EventHandler;

public class NoPacketKick extends GenyoModule {

    public NoPacketKick() {
        super(Genyo.MISC, "no-packet-kick", "Prevents getting kicked by packets");
    }

    @EventHandler
    public void onDecodePacket(DecodePacketEvent event) {
        event.cancel();
    }

}
