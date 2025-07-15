package com.genyo.addon.modules;

import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GenyoModule extends Module {

    private final String prefix = Formatting.GOLD + "" + Formatting.BOLD + "[Genyo]";

    public GenyoModule(Category category, String name, String description) {
        super(category, name, description);
    }

    //  Messages
    public void sendToggledMsg() {
        if (!Config.get().chatFeedback.get() && !chatFeedback && mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.WHITE + title + (isActive() ? Formatting.GREEN + " ON" : Formatting.RED + " OFF");
        sendMessage(Text.of(msg), hashCode());
    }

    public void sendToggledMsg(String message) {
        if (!Config.get().chatFeedback.get() && !chatFeedback && mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.WHITE + title + (isActive() ? Formatting.GREEN + " ON " : Formatting.RED + " OFF ") + Formatting.GRAY + message;
        sendMessage(Text.of(msg), hashCode());
    }

    public void sendDisableMsg(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.WHITE + title + Formatting.RED + " OFF " + Formatting.GRAY + text;
        sendMessage(Text.of(msg), hashCode());
    }

    public void sendInfo(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.WHITE + title + " - " + Formatting.GRAY + text;
        sendMessage(Text.of(msg), Objects.hash(name + "-info"));
    }

    public void sendError(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.RED + title + " - " + text;
        sendMessage(Text.of(msg), hashCode());
    }

    public void debug(String text) {
        if (mc.world == null) return;

        ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + " " + Formatting.WHITE + name + " " + Formatting.AQUA + text;
        sendMessage(Text.of(msg), 0);
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }

    public void sendSequenced(SequencedPacketCreator packetCreator) {
        if (mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null) return;

        PendingUpdateManager sequence = mc.world.getPendingUpdateManager().incrementSequence();
        Packet<?> packet = packetCreator.predict(sequence.getSequence());

        mc.getNetworkHandler().sendPacket(packet);

        sequence.close();
    }

    /*public void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
                eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
                eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        //SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        mc.player.swingHand(hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        //SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    public void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
                eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
                eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        //SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
        mc.player.swingHand(hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        //SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
    }

    public void useItem(Hand hand) {
        //SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
        mc.player.swingHand(hand);
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        sendSequenced(s -> new PlayerInteractItemC2SPacket(hand, s, mc.player.getYaw(tickDelta), mc.player.getPitch(tickDelta)));
        //SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }

    public void clientSwing(SwingHand swingHand, Hand realHand) {
        Hand hand = switch (swingHand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swingHand(hand, true);
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }*/

    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }
}
