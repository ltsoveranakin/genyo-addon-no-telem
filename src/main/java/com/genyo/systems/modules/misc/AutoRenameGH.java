package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

public class AutoRenameGH extends GenyoModule {

    public AutoRenameGH() {
        super(Genyo.MISC, "AutoRenameGH", "AutoRename GH");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {

    }

}
