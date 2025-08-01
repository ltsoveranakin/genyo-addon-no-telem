package com.genyo.addon.mixin.accessor;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(ClientConnection.class)
public interface AccessorClientConnection {

    @Invoker("sendInternal")
    void hookSendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);
}
