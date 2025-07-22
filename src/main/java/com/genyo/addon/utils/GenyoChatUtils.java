package com.genyo.addon.utils;

import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GenyoChatUtils {

    private static final String prefix = Formatting.GOLD + "" + Formatting.BOLD + "[Genyo]";

    public static void sendInfo(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(GenyoChatUtils.class);
        String msg = prefix + " " + Formatting.GRAY + text;
        sendMessage(Text.of(msg), Objects.hash("genyo-info"));
    }

    public static void sendError(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(GenyoChatUtils.class);
        String msg = prefix + " " + Formatting.RED + text;
        sendMessage(Text.of(msg), Objects.hash("genyo-error"));
    }

    public static void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

}
