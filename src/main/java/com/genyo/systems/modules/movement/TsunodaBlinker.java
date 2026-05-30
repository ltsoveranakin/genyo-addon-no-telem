package com.genyo.systems.modules.movement;

import com.genyo.Genyo;
import com.genyo.events.network.DisconnectEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.settings.FloatSetting;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class TsunodaBlinker extends GenyoModule {

    public TsunodaBlinker() {
        super(Genyo.MOVEMENT, "tsunoda-blinker", "Yuki's short as fuck");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<LagMode> mode = sgGeneral.add(new EnumSetting.Builder<LagMode>()
        .name("Mode")
        .description("The mode for caching packets")
        .defaultValue(LagMode.BLINK)
        .build()
    );

    private final Setting<Boolean> pulse = sgGeneral.add(new BoolSetting.Builder()
        .name("Pulse")
        .description("Releases packets at intervals")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> factor = sgGeneral.add(new FloatSetting.Builder()
        .name("Factor")
        .description("The factor for packet intervals")
        .min(0.0f).defaultValue(1.0f).max(10.0f)
        .visible(pulse::get)
        .build()
    );

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders the serverside player position")
        .defaultValue(true)
        .build()
    );

    private FakePlayerEntity serverModel;
    private boolean blinking;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();

    @Override
    public void onActivate()
    {
        if (mc.player != null && render.get())
        {
            serverModel = new FakePlayerEntity(mc.player, mc.player.getGameProfile().name(), 20, true);
            serverModel.spawn();
            serverModel.setUuid(mc.player.getUuid());
        }
    }

    @Override
    public void onDeactivate()
    {
        if (mc.player == null)
        {
            return;
        }
        if (!packets.isEmpty())
        {
            for (Packet<?> p : packets)
            {
                Managers.NETWORK.sendPacket(p);
            }
            packets.clear();
        }
        if (serverModel != null)
        {
            serverModel.setUuid(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"));
            serverModel.despawn();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (pulse.get() && packets.size() > factor.get() * 10.0f) {
            blinking = true;
            if (!packets.isEmpty()) {
                for (Packet<?> p : packets) {
                    Managers.NETWORK.sendPacket(p);
                }
            }
            packets.clear();
            if (serverModel != null)
            {
                serverModel.copyPositionAndRotation(mc.player);
                serverModel.setHeadYaw(mc.player.headYaw);
            }
            blinking = false;
        }
    }

    @EventHandler
    public void onDisconnectEvent(DisconnectEvent event)
    {
        toggle();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        if (mc.player == null || mc.player.isRiding() || blinking)
        {
            return;
        }
        if (event.packet instanceof PlayerActionC2SPacket || event.packet instanceof PlayerMoveC2SPacket
            || event.packet instanceof ClientCommandC2SPacket || event.packet instanceof HandSwingC2SPacket
            || event.packet instanceof PlayerInteractEntityC2SPacket || event.packet instanceof PlayerInteractBlockC2SPacket
            || event.packet instanceof PlayerInteractItemC2SPacket)
        {
            event.cancel();
            packets.add(event.packet);
        }
    }

    public enum LagMode
    {
        BLINK
    }

}
