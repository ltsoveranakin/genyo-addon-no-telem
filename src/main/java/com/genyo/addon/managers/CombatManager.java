package com.genyo.addon.managers;

import com.genyo.addon.events.TotemPopEvent;
import com.genyo.addon.events.UnderAttackEvent;
import com.genyo.addon.systems.incombat.CombatPerson;
import com.genyo.addon.systems.incombat.InCombatSystem;
import com.genyo.addon.systems.incombat.InCombatTab;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CombatManager {

    public HashMap<String, Integer> popList = new HashMap<>();

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null && mc.world == null) return;
        if (!(event.entity instanceof PlayerEntity target)) return;

        MeteorClient.EVENT_BUS.post(UnderAttackEvent.get(target));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null && mc.world == null) return;

        if (mc.player.getAttacker() != null) {
            MeteorClient.EVENT_BUS.post(UnderAttackEvent.get((PlayerEntity) mc.player.getAttacker()));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null && mc.world == null) return;

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
