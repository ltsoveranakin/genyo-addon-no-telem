package com.genyo.addon.managers;

import com.genyo.addon.events.DisconnectEvent;
import com.genyo.addon.events.EntityDeathEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TotemManager {

    //
    private final ConcurrentMap<UUID, TotemData> totems = new ConcurrentHashMap<>();

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (mc.world == null) return;

        if (event.packet instanceof EntityStatusS2CPacket packet
            && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING)
        {
            Entity entity = packet.getEntity(mc.world);
            if (entity != null && entity.isAlive())
            {
                if (totems.containsKey(entity.getUuid()))
                {
                    totems.replace(entity.getUuid(), new TotemData(System.currentTimeMillis(),
                        totems.get(entity.getUuid()).getPops() + 1));
                }
                else
                {
                    totems.put(entity.getUuid(), new TotemData(System.currentTimeMillis(), 1));
                }
            }
        }
    }

    @EventHandler(priority = Integer.MIN_VALUE)
    public void onRemoveEntity(EntityDeathEvent event)
    {
        totems.remove(event.entity.getUuid());
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        totems.clear();
    }

    /**
     * Returns the number of totems popped by the given {@link PlayerEntity}
     *
     * @param entity
     * @return Ehe number of totems popped by the player
     */
    public int getTotems(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), new TotemData(0, 0)).getPops();
    }

    public long getLastPopTime(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), new TotemData(-1, 0)).getLastPopTime();
    }

    public static class TotemData
    {
        private final long lastPopTime;
        private final int pops;

        public TotemData(long lastPopTime, int pops)
        {
            this.lastPopTime = lastPopTime;
            this.pops = pops;
        }

        public int getPops()
        {
            return pops;
        }

        public long getLastPopTime()
        {
            return lastPopTime;
        }
    }

}
