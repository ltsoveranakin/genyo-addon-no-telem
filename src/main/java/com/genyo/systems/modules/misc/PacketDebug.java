package com.genyo.systems.modules.misc;

import com.genyo.GenyoAddon;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;

import java.util.HashSet;
import java.util.Set;

public class PacketDebug extends GenyoModule {

    public PacketDebug() {
        super(GenyoAddon.MISC, "packet-debug", "yweoikfjwekfjhewkfjwehfkjefhwehjkfhwehfkew.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<Class<? extends Packet<?>>>> blacklist = sgGeneral.add(new PacketListSetting.Builder()
        .name("Blacklisted Packets")
        .description("black >:(")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    private final Setting<Boolean> toChat = sgGeneral.add(new BoolSetting.Builder()
        .name("Send in Chat")
        .description("sends the logs to chat (only client side)")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("Threshold")
        .description("Minimum amount to reach for packets to be logged in a single tick")
        .min(0)
        .defaultValue(10)
        .max(30)
        .build()
    );

    private final Setting<Integer> logTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Tick Amount")
        .description("how many ticks to wait to log all the packets")
        .min(1)
        .defaultValue(10)
        .max(20)
        .visible(() -> threshold.get() > 0)
        .build()
    );

    private int currentAmount;
    private int tickTimer;
    private Set<String> queue = new HashSet<>();

    @Override
    public void onActivate() {
        currentAmount = 0;
        tickTimer = 0;
        queue.clear();
    }

    @Override
    public void onDeactivate() {
        currentAmount = 0;
        tickTimer = 0;
        queue.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        if (threshold.get() == 0) return;

        if (logTicks.get() != 0) tickTimer++;

        if (currentAmount >= threshold.get()) {
            if (logTicks.get() != 0) {
                if (!(tickTimer > logTicks.get())) return;

                if (toChat.get()) {
                    sendInfo("Sent packets (" + currentAmount + ") in the last " + logTicks.get().toString() + " ticks: " + queue.toString());
                } else {
                    GenyoAddon.LOG.info("Sent packets (" + currentAmount + ") in the last " + logTicks.get().toString() + " ticks: " + queue.toString());
                }

                tickTimer = 0;
            } else {
                if (toChat.get()) {
                    sendInfo("Sent packets (" + currentAmount + ") in the last tick: " + queue.toString());
                } else {
                    GenyoAddon.LOG.info("Sent packets (" + currentAmount + ") in the last tick: " + queue.toString());
                }
            }

            currentAmount = 0;
            queue.clear();
        }
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (mc.world == null || mc.player == null) return;

        if (!blacklist.get().contains(event.packet.getClass()) && PacketUtils.getC2SPackets().contains(event.packet.getClass())) {
            if (threshold.get() > 0) {
                currentAmount++;
                queue.add(event.packet.getClass().getSimpleName());
            } else {
                if (toChat.get()) {
                    sendInfo("Sent packet: " + event.packet.getClass().getSimpleName());
                } else {
                    GenyoAddon.LOG.info("Sent packet: " + event.packet.getClass().getSimpleName());
                }
            }
        }
    }

}
