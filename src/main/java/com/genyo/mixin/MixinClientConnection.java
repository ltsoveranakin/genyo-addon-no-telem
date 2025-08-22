package com.genyo.mixin;

import com.genyo.GenyoAddon;
import com.genyo.events.network.DecodePacketEvent;
import com.genyo.events.network.DisconnectEvent;
import io.netty.channel.ChannelHandlerContext;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    /**
     * @param disconnectReason
     * @param ci
     */
    @Inject(method = "disconnect*", at = @At(value = "HEAD"))
    private void hookDisconnect(Text disconnectReason, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(new DisconnectEvent());
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void hookExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci)
    {
        DecodePacketEvent decodePacketEvent = new DecodePacketEvent();
        MeteorClient.EVENT_BUS.post(decodePacketEvent);
        if (decodePacketEvent.isCancelled())
        {
            GenyoAddon.LOG.error("Exception caught on network thread:", ex);
            ci.cancel();
        }
    }

}
