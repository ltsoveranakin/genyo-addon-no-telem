package com.genyo.systems.settings.playerlist;

import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class ListPlayer implements ISerializable<ListPlayer>, Comparable<ListPlayer> {
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

    public void updateInfo(boolean updateName) {
        updating = true;
        ListPlayer.APIResponse res = Http.get("https://api.mojang.com/users/profiles/minecraft/" + name).sendJson(ListPlayer.APIResponse.class);
        if (res == null || res.name == null || res.id == null) return;
        if (updateName) name = res.name;
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
    public int compareTo(@NotNull ListPlayer player) {
        return name.compareTo(player.name);
    }

    private static class APIResponse {
        String name, id;
    }
}
