package com.genyo.addon.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.settings.FloatSetting;
import com.genyo.addon.utils.math.MathUtil;
import com.genyo.addon.utils.math.timer.CacheTimer;
import com.genyo.addon.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;

import java.util.ArrayList;
import java.util.List;

public class CombatBrainrot extends GenyoModule {

    public CombatBrainrot() {
        super(GenyoAddon.GENYO, "combat-brainrot", "says something sigma when punching a crystal.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> brainrots = sgGeneral.add(new StringListSetting.Builder()
        .name("The")
        .description("ewfsdfdsfesfewfwerewrewrwerewrewrew")
        .defaultValue(List.of("hfgjksdhfhdskjfhdsfsd", "Niger biger", "asdadsadsadasdasdasdasdsadsada", "brain damge"))
        .build()
    );

    private final Setting<Float> delay = sgGeneral.add(new FloatSetting.Builder()
        .name("Delay")
        .description("to maybe not get kicked or smth idk (in milliseconds)")
        .min(0.1f)
        .defaultValue(0.2f)
        .max(1f)
        .build()
    );

    //TODO: whisper
    private final Timer timer = new CacheTimer();
    private final List<String> queue = new ArrayList<>();

    @Override
    public void onActivate() {
        timer.reset();
        queue.clear();

        if (brainrots.get().isEmpty()) {
            toggle();
            sendDisableMsg("No brainrots available.");
        }
    }

    @Override
    public void onDeactivate() {
        timer.reset();
        queue.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (queue.isEmpty()) return;

        if (timer.passed(delay.get() * 1000)) {
            String message = queue.getFirst();

            ChatUtils.sendPlayerMsg(message);
            queue.removeFirst();
            timer.reset();
        }
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!(event.entity instanceof EndCrystalEntity)) return;

        queue.add(brainrots.get().get(MathUtil.pickRandom(brainrots.get())));
    }

}
