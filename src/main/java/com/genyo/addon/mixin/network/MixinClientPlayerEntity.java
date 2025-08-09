package com.genyo.addon.mixin.network;

import com.genyo.addon.events.StageEvent;
import com.genyo.addon.events.sync.SyncEvent;
import com.genyo.addon.events.network.*;
import com.genyo.addon.imixins.IClientPlayerEntity;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
    private float lastYaw;
    @Shadow
    private float lastPitch;

    @Shadow
    protected abstract void sendMovementPackets();

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    public abstract float getPitch(float tickDelta);

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
    private void hookSendMovementPackets(CallbackInfo ci) {
        //if (fullNullCheck()) return;
        SyncEvent.Pre event = SyncEvent.Pre.get(getYaw(mc.getRenderTickCounter().getTickDelta(true)), getPitch(mc.getRenderTickCounter().getTickDelta(true)));
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
        //if (fullNullCheck()) return;
        //mc.player.lastSprinting = pre_sprint_state;

        SyncEvent.Post event = SyncEvent.Post.get();
        MeteorClient.EVENT_BUS.post(event);

        if(postAction != null) {
            postAction.run();
            postAction = null;
        }

        if (event.isCancelled())
            info.cancel();
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

    /**
     * @param x
     * @param z
     * @param ci
     */
    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"),
        cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci)
    {
        PushOutOfBlocksEvent pushOutOfBlocksEvent = new PushOutOfBlocksEvent();
        MeteorClient.EVENT_BUS.post(pushOutOfBlocksEvent);
        if (pushOutOfBlocksEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    @Override
    public float genyo_addon$getLastSpoofedYaw()
    {
        return lastYaw;
    }

    @Override
    public float genyo_addon$getLastSpoofedPitch()
    {
        return lastPitch;
    }

}
