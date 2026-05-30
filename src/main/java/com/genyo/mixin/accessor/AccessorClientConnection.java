package com.genyo.mixin.accessor;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface AccessorClientConnection {

    @Invoker("sendInternal")
    void hookSendInternal(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush);
}
