package com.genyo.mixin.network;

import com.genyo.events.StageEvent;
import com.genyo.events.entity.SwingEvent;
import com.genyo.events.network.*;
import com.genyo.events.sync.SyncEvent;
import com.genyo.imixins.IClientPlayerEntity;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity implements IClientPlayerEntity {

    @Unique
    boolean pre_sprint_state = false;
    @Unique
    private boolean updateLock = false;
    @Unique
    private Runnable postAction;

    @Shadow
    protected abstract void sendMovementPackets();

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    public abstract float getPitch(float tickDelta);

    @Shadow
    public Input input;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/" +
        "minecraft/client/network/AbstractClientPlayerEntity;tick()V",
        shift = At.Shift.BEFORE, ordinal = 0))
    private void hookTickPre(CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(new PlayerTickEvent());
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendMovementPackets(CallbackInfo ci) {
        SyncEvent.Pre event = SyncEvent.Pre.get(getYaw(mc.getRenderTickCounter().getTickProgress(true)), getPitch(mc.getRenderTickCounter().getTickProgress(true)));
        MeteorClient.EVENT_BUS.post(event);
        postAction = event.postAction;

        PlayerUpdateEvent playerUpdateEvent = new PlayerUpdateEvent();
        playerUpdateEvent.setStage(StageEvent.EventStage.PRE);
        MeteorClient.EVENT_BUS.post(playerUpdateEvent);

        MovementPacketsEvent movementPacketsEvent = MovementPacketsEvent.get(mc.player.getX(), mc.player.getY(),
            mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround());
        MeteorClient.EVENT_BUS.post(movementPacketsEvent);

        playerUpdateEvent.setStage(StageEvent.EventStage.POST);
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"), cancellable = true)
    private void sendMovementPacketsPostHook(CallbackInfo info) {
        SyncEvent.Post event = SyncEvent.Post.get();
        MeteorClient.EVENT_BUS.post(event);

        if (postAction != null) {
            postAction.run();
            postAction = null;
        }

        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "setCurrentHand", at = @At(value = "HEAD"))
    private void hookSetCurrentHand(Hand hand, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(SetCurrentHandEvent.get(hand));
    }

    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci)
    {
        PushOutOfBlocksEvent pushOutOfBlocksEvent = new PushOutOfBlocksEvent();
        MeteorClient.EVENT_BUS.post(pushOutOfBlocksEvent);
        if (pushOutOfBlocksEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "swingHand", at = @At(value = "RETURN"))
    private void hookSwingHand(Hand hand, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(SwingEvent.get(hand));
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/input/Input;tick()V", shift = At.Shift.AFTER))
    private void hookTickMovementPost(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(MovementSlowdownEvent.get(input));
    }

    @Override
    public float genyo_addon$getLastSpoofedYaw()
    {
        return ((Entity) (Object) this).lastYaw;
    }

    @Override
    public float genyo_addon$getLastSpoofedPitch()
    {
        return ((Entity) (Object) this).lastPitch;
    }

}
