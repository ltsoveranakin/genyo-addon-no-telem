package com.genyo.addon.managers;

import com.genyo.addon.events.TotemPopEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CombatManager {

    public HashMap<String, Integer> popList = new HashMap<>();

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {

        if (event.packet instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity ent = pac.getEntity(mc.world);
                if (!(ent instanceof PlayerEntity)) return;
                if (popList == null) {
                    popList = new HashMap<>();
                }
                if (popList.get(ent.getName().getString()) == null) {
                    popList.put(ent.getName().getString(), 1);
                } else if (popList.get(ent.getName().getString()) != null) {
                    popList.put(ent.getName().getString(), popList.get(ent.getName().getString()) + 1);
                }
                MeteorClient.EVENT_BUS.post(TotemPopEvent.get((PlayerEntity) ent, popList.get(ent.getName().getString())));
            }
        }
    }

}
