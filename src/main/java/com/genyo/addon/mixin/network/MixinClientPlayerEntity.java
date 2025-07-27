package com.genyo.addon.mixin.network;

import com.genyo.addon.events.StageEvent;
import com.genyo.addon.events.network.MovementPacketsEvent;
import com.genyo.addon.events.network.PlayerTickEvent;
import com.genyo.addon.events.network.PlayerUpdateEvent;
import com.genyo.addon.events.network.SetCurrentHandEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/" +
        "minecraft/client/network/AbstractClientPlayerEntity;tick()V",
        shift = At.Shift.BEFORE, ordinal = 0))
    private void hookTickPre(CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(new PlayerTickEvent());
    }

    /**
     * @param ci
     */
    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendMovementPackets(CallbackInfo ci)
    {
        PlayerUpdateEvent playerUpdateEvent = new PlayerUpdateEvent();
        playerUpdateEvent.setStage(StageEvent.EventStage.PRE);
        MeteorClient.EVENT_BUS.post(playerUpdateEvent);

        MovementPacketsEvent movementPacketsEvent = MovementPacketsEvent.get(mc.player.getX(), mc.player.getY(),
            mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround());
        MeteorClient.EVENT_BUS.post(movementPacketsEvent);

        playerUpdateEvent.setStage(StageEvent.EventStage.POST);
    }

    /**
     * @param hand
     * @param ci
     */
    @Inject(method = "setCurrentHand", at = @At(value = "HEAD"))
    private void hookSetCurrentHand(Hand hand, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(SetCurrentHandEvent.get(hand));
    }

}
