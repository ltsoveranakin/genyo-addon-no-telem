package com.genyo.mixin.render;

import com.genyo.events.render.TickCounterEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class MixinRenderTickCounter {

    @Shadow
    private float tickProgressBeforePause;

    @Shadow
    private float tickProgress;

    @Shadow
    private long lastTimeMillis;

    @Final
    @Shadow
    private float tickTime;

    /**
     * @param timeMillis
     * @param cir
     */
    @Inject(method = "beginRenderTick(J)I", at = @At(value = "HEAD"), cancellable = true)
    private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir)
    {
        TickCounterEvent tickCounterEvent = new TickCounterEvent();
        MeteorClient.EVENT_BUS.post(tickCounterEvent);
        if (tickCounterEvent.isCancelled())
        {
            tickProgressBeforePause = ((timeMillis - lastTimeMillis) / tickTime) * tickCounterEvent.ticks;
            lastTimeMillis = timeMillis;
            tickProgress += tickProgressBeforePause;
            int i = (int) tickProgress;
            tickProgress -= i;
            cir.setReturnValue(i);
        }
    }

}
