package com.genyo.systems.modules.movement;

import com.genyo.GenyoAddon;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class GenyoAntiLadder extends GenyoModule {

    public GenyoAntiLadder() {
        super(GenyoAddon.MOVEMENT, "genyo-anti-ladder", "Prevents you from climbing ladders.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.world == null && mc.player == null) return;

        if(!mc.player.isClimbing() || !mc.player.horizontalCollision)
            return;

        if(mc.player.input.getMovementInput().length() <= 1e-5F)
            return;

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, 0, velocity.z);
    }

}
