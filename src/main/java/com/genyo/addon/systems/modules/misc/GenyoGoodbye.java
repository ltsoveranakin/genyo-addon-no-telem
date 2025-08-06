package com.genyo.addon.systems.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.systems.modules.GenyoModule;
import com.genyo.addon.systems.settings.playerlist.ListGroupSetting;
import com.genyo.addon.systems.settings.playerlist.PLGroup;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GenyoGoodbye extends GenyoModule {

    private final List<Message> messageQueue = new LinkedList<>();
    private int timer;

    private ArrayList<PLGroup> groupsList = new ArrayList<>();
    private ArrayList<String> namesList = new ArrayList<>();

    public GenyoGoodbye() {
        super(GenyoAddon.MISC, "genyo-goodbye", "i hate kiwi. i hate kiwi. i hate kiwi. i hate kiwi. i hate kiwi. ");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<PLGroup>> groups = sgGeneral.add(new ListGroupSetting.Builder()
        .name("Groups")
        .description("sdasdjgewqjhgfjhgewjhfg ew gfjhewgfhjgwehjf gjhwe few")
        .onChanged(this::refreshList)
        .build()
    );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("tick delay between the messages.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null && mc.world == null) return;

        if (!messageQueue.isEmpty()) timer++;

        if (timer >= tickDelay.get() && !messageQueue.isEmpty()) {
            Message msg = messageQueue.get(0);
            ChatUtils.sendPlayerMsg(msg.message);
            timer = 0;

            if (msg.kill) messageQueue.clear();
            else messageQueue.removeFirst();
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerRemoveS2CPacket pac) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(pac.profileIds().getFirst());
            if (entry == null) return;

            String name = entry.getProfile().getName();
            if (!namesList.contains(name)) return;

            handleMessage(name);
        }
    }

    public void refreshList(List<PLGroup> newGroups) {
        groupsList.clear();
        groupsList.addAll(newGroups);

        namesList.clear();
        newGroups.forEach(group -> {
            if (group.getPlayers().isEmpty()) return;

            group.getPlayers().forEach(player -> {
                namesList.add(player.getName());
            });
        });
    }

    private void handleMessage(String name) {
        if (mc.world == null) return;

        AtomicReference<String> atomicMSG = new AtomicReference<>("");
        groupsList.forEach(group -> {
            if (group.containsPlayer(name)) {
                if (group.isEnabled()) atomicMSG.set(group.getMessage());
            }
        });

        if (atomicMSG.get() == "") return;

        String toSend = atomicMSG.get();
        toSend = toSend.contains("<NAME>") ? toSend.replace("<NAME>", name) : toSend;

        Message msg = new Message(toSend, false);
        messageQueue.add(msg);
    }

    private record Message(String message, boolean kill) {
    }

}
