package com.genyo.mixin;

import com.genyo.events.network.DisconnectEvent;
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

}
