package com.genyo.addon.managers.network;

import com.genyo.addon.events.network.ConnectScreenEvent;
import com.genyo.addon.events.network.DisconnectEvent;
import com.genyo.addon.mixin.accessor.AccessorClientWorld;
import com.genyo.addon.utils.math.PerSecondCounter;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.*;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.HashSet;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class NetworkManager {

    private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();

    private ServerAddress address;
    private ServerInfo info;

    private final PerSecondCounter outgoingCounter = new PerSecondCounter();
    private final PerSecondCounter incomingCounter = new PerSecondCounter();

    @EventHandler
    public void onConnectScreen(ConnectScreenEvent event)
    {
        address = event.address;
        info = event.info;
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        PACKET_CACHE.clear();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        outgoingCounter.updateCounter();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event)
    {
        incomingCounter.updateCounter();
    }

    public void connect(final ServerAddress address, final ServerInfo info)
    {
        if (mc.getNetworkHandler() == null)
        {
            return;
        }
        mc.getNetworkHandler().getConnection().connect(address.getAddress(), address.getPort(),
            new ClientLoginNetworkHandler(mc.getNetworkHandler().getConnection(), mc, info, null, false, null, null, null));
    }

    /**
     * @param p
     */
    public void sendPacket(final Packet<?> p)
    {
        if (mc.getNetworkHandler() != null)
        {
            PACKET_CACHE.add(p);
            mc.getNetworkHandler().sendPacket(p);
        }
    }

    /**
     * @param p
     */
    public void sendSequencedPacket(final SequencedPacketCreator p)
    {
        if (mc.world != null)
        {
            PendingUpdateManager updater =
                ((AccessorClientWorld) mc.world).hookGetPendingUpdateManager().incrementSequence();
            try
            {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                sendPacket(packet);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                if (updater != null)
                {
                    try
                    {
                        updater.close();
                    }
                    catch (Throwable e1)
                    {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null)
            {
                updater.close();
            }
        }
    }

    /**
     * @return
     */
    public int getClientLatency()
    {
        if (mc.getNetworkHandler() != null)
        {
            final PlayerListEntry playerEntry =
                mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId());
            if (playerEntry != null)
            {
                return playerEntry.getLatency();
            }
        }
        return 0;
    }

    public ServerAddress getAddress()
    {
        return address;
    }

    public void setAddress(ServerAddress address)
    {
        this.address = address;
    }

    public ServerInfo getInfo()
    {
        return info;
    }

    public void setInfo(ServerInfo info)
    {
        this.info = info;
    }

    public boolean isCrystalPvpCC()
    {
        return getServerIp().contains("crystalpvp.cc");
    }

    public boolean is2b2t()
    {
        return getServerIp().contains("2b2t.org");
    }

    public int getOutgoingPPS()
    {
        return outgoingCounter.getPerSecond();
    }

    public int getIncomingPPS()
    {
        return incomingCounter.getPerSecond();
    }

    public String getServerIp()
    {
        if (info != null)
        {
            return info.address;
        }
        return "Singleplayer";
    }

    /**
     * @param p
     * @return
     */
    public boolean isCached(Packet<?> p)
    {
        return PACKET_CACHE.contains(p);
    }

}
