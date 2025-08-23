package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.collection.Message;
import com.genyo.utils.collection.MessageTickQueue;
import com.genyo.utils.math.MathUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.List;

public class CombatBrainrot extends GenyoModule {

    public CombatBrainrot() {
        super(Genyo.MISC, "combat-brainrot", "says something sigma while in crystal pvp.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> brainrots = sgGeneral.add(new StringListSetting.Builder()
        .name("The")
        .description("ewfsdfdsfesfewfwerewrewrwerewrewrew")
        .defaultValue(List.of("hfgjksdhfhdskjfhdsfsd", "Niger biger", "asdadsadsadasdasdasdasdsadsada", "brain damge"))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Tick Delay")
        .description("to maybe not get kicked or smth idk")
        .min(5)
        .defaultValue(10)
        .max(20)
        .range(5, 20)
        .onChanged(this::refreshTimer)
        .build()
    );

    //TODO: whisper
    private final MessageTickQueue queue = new MessageTickQueue(delay.get());

    @Override
    public void onActivate() {
        if (brainrots.get().isEmpty()) {
            toggle();
            sendDisableMsg("No brainrots available.");
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.getServer() == null) return;

        if (event.packet instanceof PlayerInteractEntityC2SPacket packet) {
            Entity entity = packet.getEntity(mc.getServer().getWorld(mc.player.getWorld().getRegistryKey()));
            if (entity == null) return;

            if (entity instanceof EndCrystalEntity) {
                queueNext();
            }
        }
    }

    private void queueNext() {
        if (!queue.isEmpty()) return;

        Message message = new Message(brainrots.get().get(MathUtil.pickRandom(brainrots.get())), false);
        queue.addMessage(message);
    }

    private void refreshTimer(int value) {
        queue.setDelay(value);
    }

}
