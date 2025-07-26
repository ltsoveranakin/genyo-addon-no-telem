package com.genyo.addon.managers.combat;

import com.genyo.addon.events.TotemPopEvent;
import com.genyo.addon.events.UnderCombatEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
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
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null && mc.world == null) return;

        if (event.entity instanceof PlayerEntity target) {
            MeteorClient.EVENT_BUS.post(UnderCombatEvent.get(target));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null && mc.world == null) return;

        if (event.packet instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity ent = pac.getEntity(mc.world);
                if (!(ent instanceof PlayerEntity)) return;

                if (popList == null) popList = new HashMap<>();

                String entityName = ent.getName().getString();

                if (popList.get(entityName) == null) {
                    popList.put(entityName, Integer.valueOf(1));
                } else {
                    popList.put(entityName, Integer.valueOf(popList.get(entityName) + 1));
                }

                MeteorClient.EVENT_BUS.post(TotemPopEvent.get((PlayerEntity) ent, popList.get(entityName)));
            }
        }
    }

}
