package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.events.network.DisconnectEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

public class FastLatency extends GenyoModule {

    public FastLatency() {
        super(Genyo.MISC, "fast-latency", "Calculates server ping");
    }

    private long requestTime;
    private long latency;
    private final Timer lastRequest = new CacheTimer();
    private final Timer requestTimer = new CacheTimer();

    @Override
    public String getInfoString()
    {
        return String.format("%dms", latency);
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        latency = 0;
        requestTime = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event)
    {
        if (lastRequest.passed(5000) && requestTimer.passed(500))
        {
            Managers.NETWORK.sendPacket(new RequestCommandCompletionsC2SPacket(1000, "w "));
            requestTimer.reset();
            lastRequest.reset();
            requestTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.packet instanceof CommandSuggestionsS2CPacket packet && packet.id() == 1000)
        {
            latency = System.currentTimeMillis() - requestTime;
            lastRequest.setElapsedTime(Timer.MAX_TIME);
        }
    }

    public long getLatency()
    {
        return latency;
    }

}
