package com.genyo.mixin.entity;

import com.genyo.events.entity.SlowMovementEvent;
import com.genyo.events.entity.VelocityMultiplierEvent;
import com.genyo.events.entity.player.PushEntityEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Entity.class)
public class MixinEntity {

    @Shadow
    public float fallDistance;

    @Shadow protected Vec3d movementMultiplier;

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    private void hookPushAwayFrom(Entity entity, CallbackInfo ci)
    {
        PushEntityEvent pushEntityEvent = PushEntityEvent.get((Entity) (Object) this, entity);
        MeteorClient.EVENT_BUS.post(pushEntityEvent);

        if (pushEntityEvent.isCancelled()) ci.cancel();
    }

    /**
     * @param state
     * @param multiplier
     * @param ci
     */
    @Inject(method = "slowMovement", at = @At(value = "HEAD"), cancellable = true)
    private void hookSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        SlowMovementEvent slowMovementEvent = SlowMovementEvent.get(state);
        MeteorClient.EVENT_BUS.post(slowMovementEvent);
        if (slowMovementEvent.isCancelled()) {
            ci.cancel();
            this.fallDistance = 0.0f;
            this.movementMultiplier = multiplier.multiply(slowMovementEvent.multiplier);
        }
    }

}
