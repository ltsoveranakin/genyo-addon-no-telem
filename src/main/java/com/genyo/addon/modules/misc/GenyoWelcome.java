package com.genyo.addon.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.settings.playerlist.ListGroupSetting;
import com.genyo.addon.settings.playerlist.PLGroup;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GenyoWelcome extends GenyoModule {

    private Set<UUID> onlinePlayers = new HashSet<>();
    private final List<Message> messageQueue = new LinkedList<>();
    private int timer = 0;

    private ArrayList<PLGroup> groupsList = new ArrayList<>();
    private ArrayList<String> namesList = new ArrayList<>();

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

    @Override
    public void onActivate() {
        onlinePlayers.clear();
    }

    public GenyoWelcome() {
        super(GenyoAddon.GENYO, "genyo-welcome", "i love kiwi. i love kiwi. i love kiwi. i love kiwi. i love kiwi.");
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (mc.player == null && mc.world == null) return;
        onlinePlayers.clear();
        mc.getNetworkHandler().getPlayerList().iterator().forEachRemaining(p -> {
            if (p.getProfile() != null) onlinePlayers.add(p.getProfile().getId());
        });
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        onlinePlayers.clear();
    }

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
        if (event.packet instanceof PlayerListS2CPacket pac) {
            pac.getEntries().forEach(entry -> {
                GameProfile profile = entry.profile();
                if (profile == null) return;

                String name = profile.getName();
                if (!namesList.contains(name)) return;
                UUID playerUuid = profile.getId();

                if (!pac.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)
                    && playerUuid != null && !onlinePlayers.contains(playerUuid)) return;

                handleMessage(name);
                onlinePlayers.add(playerUuid);
            });
        }
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

    private record Message(String message, boolean kill) {
    }


}
