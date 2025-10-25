package com.genyo.mixin.block;

import com.genyo.events.block.SteppedOnSlimeBlockEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(SlimeBlock.class)
public class MixinSlimeBlock {

    /**
     * @param world
     * @param pos
     * @param state
     * @param entity
     * @param ci
     */
    @Inject(method = "onSteppedOn", at = @At(value = "HEAD"),
        cancellable = true)
    private void hookOnSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (MeteorClient.EVENT_BUS.post(SteppedOnSlimeBlockEvent.get()).isCancelled() && entity == mc.player)
        {
            ci.cancel();
        }
    }

}
