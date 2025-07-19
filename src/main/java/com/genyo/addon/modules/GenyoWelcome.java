package com.genyo.addon.modules;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.settings.PlayerListSetting;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class GenyoWelcome extends GenyoModule {

    public static List<ListPlayer> genyo = new ArrayList<>();

    private Set<UUID> onlinePlayers = new HashSet<>();
    private final List<Message> messageQueue = new LinkedList<>();
    private int timer = 0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<ListPlayer>> players = sgGeneral.add(new PlayerListSetting.Builder()
        .name("Players")
        .description("sdasdjgewqjhgfjhgewjhfg ew gfjhewgfhjgwehjf gjhwe few")
        .onChanged(this::refreshList)
        .build()
    );

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
        .name("Message")
        .description("<NAME> ewhfhewjkfhejwkhfhewkjfhewjkfhkjewhfkhewfke")
        .defaultValue("genyo <NAME>")
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
        super(GenyoAddon.GENYO, "Genyo Welcome", "i love kiwi. i love kiwi. i love kiwi. i love kiwi. i love kiwi.");
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
        timer++;

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
                if (!contains(name)) return;
                UUID playerUuid = profile.getId();
                GenyoAddon.LOG.info("asd");
                if (!pac.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)
                    && playerUuid != null && !onlinePlayers.contains(playerUuid)) return;

                handleMessage(name);
                onlinePlayers.add(playerUuid);
            });
        }
    }

    private void handleMessage(String name) {
        if (mc.world == null) return;

        String toSend = message.get();
        toSend = toSend.contains("<NAME>") ? toSend.replace("<NAME>", name) : toSend;

        Message msg = new Message(toSend, false);
        messageQueue.add(msg);
    }

    private boolean contains(String name) {
        for (ListPlayer listPlayer : players.get()) {
            if (listPlayer.name.equalsIgnoreCase(name)) return true;
        }

        return false;
    }

    private void refreshList(List<ListPlayer> a) {
        genyo = a;
    }

    public static ListPlayer get(String name) {
        for (ListPlayer player : genyo) {
            if (player == null) return null;

            if (player.name.equalsIgnoreCase(name)) {
                return player;
            }
        }

        return null;
    }

    public boolean isInList(String name) {
        return get(name) != null;
    }

    public static ListPlayer createListPlayer(String name) {
        return new ListPlayer(name);
    }

    public static class ListPlayer implements ISerializable<ListPlayer>, Comparable<ListPlayer> {
        private static final MinecraftClient mc = MinecraftClient.getInstance();

        public volatile String name;
        private volatile @Nullable UUID id;
        private volatile @Nullable PlayerHeadTexture headTexture;
        private volatile boolean updating;

        public ListPlayer(String name, @Nullable UUID id) {
            this.name = name;
            this.id = id;
            this.headTexture = null;
        }

        public ListPlayer(PlayerEntity player) {
            this(player.getName().getString(), player.getUuid());
        }

        public ListPlayer(String name) {
            this(name, null);
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public PlayerHeadTexture getHead() {
            return headTexture != null ? headTexture : PlayerHeadUtils.STEVE_HEAD;
        }

        public void updateInfo() {
            updating = true;
            APIResponse res = Http.get("https://api.mojang.com/users/profiles/minecraft/" + name).sendJson(APIResponse.class);
            if (res == null || res.name == null || res.id == null) return;
            name = res.name;
            id = UndashedUuid.fromStringLenient(res.id);
            mc.execute(() -> headTexture = PlayerHeadUtils.fetchHead(id));
            updating = false;
        }

        public boolean headTextureNeedsUpdate() {
            return !this.updating && headTexture == null;
        }

        @Override
        public NbtCompound toTag() {
            NbtCompound tag = new NbtCompound();

            tag.putString("name", name);
            if (id != null) tag.putString("id", UndashedUuid.toString(id));

            return tag;
        }

        @Override
        public ListPlayer fromTag(NbtCompound tag) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListPlayer player = (ListPlayer) o;
            return Objects.equals(name, player.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public int compareTo(@NotNull GenyoWelcome.ListPlayer player) {
            return name.compareTo(player.name);
        }

        private static class APIResponse {
            String name, id;
        }
    }

    private record Message(String message, boolean kill) {
    }


}
