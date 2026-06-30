package com.genyo.managers.world.tick;

import com.genyo.events.network.DisconnectEvent;
import com.genyo.events.render.TickCounterEvent;
import com.genyo.utils.collection.EvictingQueue;
import com.google.common.collect.Lists;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.ArrayList;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TickManager {

    private final Deque<Float> ticks = new EvictingQueue<>(20);
    // The TPS tick handler.
    //
    private long time;
    //
    private float clientTick = 1.0f;

    @EventHandler
    public void onDisconnect(DisconnectEvent event) {
        ticks.clear();
    }

    /**
     * @param event
     * @see WorldTimeUpdateS2CPacket
     */
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        // ticks/actual
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            float last = 20000.0f / (System.currentTimeMillis() - time);
            ticks.addFirst(last);
            time = System.currentTimeMillis();
        }
    }

    /**
     * @param ticks
     */
    public void setClientTick(float ticks) {
        clientTick = ticks;
    }

    /**
     * @param event
     */
    @EventHandler
    public void onTickCounter(TickCounterEvent event) {
        if (clientTick != 1.0f) {
            event.cancel();
            event.ticks = clientTick;
        }
    }

    /**
     * @return
     */
    public Queue<Float> getTicks() {
        return ticks;
    }

    /**
     * @return
     */
    public float getTpsAverage() {
        float avg = 0.0f;
        try {
            // fix ConcurrentModificationException
            ArrayList<Float> ticksCopy = Lists.newArrayList(ticks);
            if (!ticksCopy.isEmpty()) {
                for (float t : ticksCopy) {
                    avg += t;
                }
                avg /= Math.max(ticksCopy.size(), 1.0f);
            }
        } catch (NullPointerException e) {

        }
        return Math.min(100.0f, avg); // Server may compensate
    }

    /**
     * @return
     */
    public float getTpsCurrent() {
        try {
            if (!ticks.isEmpty()) {
                return Math.min(100.0f, ticks.getFirst());
            }
        } catch (NoSuchElementException ignored) {

        }
        return 20.0f;
    }

    /**
     * @return
     */
    public float getTpsMin() {
        float min = 20.0f;
        try {
            for (float t : ticks) {
                if (t < min) {
                    min = t;
                }
            }
        } catch (NullPointerException e) {

        }
        return min;
    }

    public boolean isTicksFilled() {
        return ticks.size() >= 20;
    }

    /**
     * @param tps
     * @return
     */
    public float getTickSync(TickSync tps) {
        return switch (tps) {
            case AVERAGE -> getTpsAverage();
            case CURRENT -> getTpsCurrent();
            case MINIMAL -> getTpsMin();
            case NONE -> 20.0f;
        };
    }

}
