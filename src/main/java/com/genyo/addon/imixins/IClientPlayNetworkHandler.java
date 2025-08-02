package com.genyo.addon.imixins;

import net.minecraft.network.packet.Packet;

@IMixin
public interface IClientPlayNetworkHandler {
    void sendQuietPacket(final Packet<?> packet);
}
