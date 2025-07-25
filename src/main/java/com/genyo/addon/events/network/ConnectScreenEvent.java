package com.genyo.addon.events.network;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ConnectScreenEvent {

    private static final ConnectScreenEvent INSTANCE = new ConnectScreenEvent();

    public ServerAddress address;
    public ServerInfo info;

    public static ConnectScreenEvent get(ServerAddress address, ServerInfo info) {
        INSTANCE.address = address;
        INSTANCE.info = info;

        return INSTANCE;
    }
}
