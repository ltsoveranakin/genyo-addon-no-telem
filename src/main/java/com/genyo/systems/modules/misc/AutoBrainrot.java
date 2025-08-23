package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.math.MathUtil;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutoBrainrot extends GenyoModule {

    public AutoBrainrot() {
        super(Genyo.MISC, "auto-brainrot", "Traumatize your surroundings or the whole server.");

        loadBrainrots();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Cooldown> cooldownSetting = sgGeneral.add(new EnumSetting.Builder<Cooldown>()
        .name("Cooldown")
        .description("Cooldown in minutes")
        .defaultValue(Cooldown.Ten)
        .onChanged(this::updateCooldown)
        .build()
    );

    private final Setting<Range> mode = sgGeneral.add(new EnumSetting.Builder<Range>()
        .name("Mode")
        .description("Either tell the whole server or players in a radius")
        .defaultValue(Range.Radius)
        .build()
    );

    private final Setting<ChatMode> chatMode = sgGeneral.add(new EnumSetting.Builder<ChatMode>()
        .name("9b9t chat color")
        .description("Only works on 9b9t")
        .defaultValue(ChatMode.Default)
        .build()
    );

    private final Setting<Float> radius = sgGeneral.add(new FloatSetting.Builder()
        .name("Radius")
        .description("The radius of the circle to send message to")
        .min(10f)
        .defaultValue(10f)
        .max(50f)
        .sliderRange(10f, 50f)
        .visible(() -> mode.get().equals(Range.Radius))
        .build()
    );

    private final Identifier file = Identifier.of(Genyo.MOD_ID, "txtfiles/brainrot.txt");
    private final List<String> brainrots = new ArrayList<>();

    private final Timer cooldownTimer = new CacheTimer();
    private int cooldown = 10; // in minutes

    @Override
    public void onActivate() {
        cooldownTimer.reset();
    }

    @Override
    public void onDeactivate() {
        cooldownTimer.reset();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.world == null && mc.player == null) return;

        if (cooldownTimer.passed(cooldown * 60000)) {
            String brainrot = pickBrainrot();

            cooldownTimer.reset();
            switch (mode.get()) {
                case Server:
                    String output = "";

                    switch (chatMode.get()) {
                        case Blue -> output += "`";
                        case Green -> output += ">";
                    }

                    output += brainrot;

                    ChatUtils.sendPlayerMsg(output);
                    break;
                case Radius:
                    List<PlayerEntity> players = getPlayersInRadius();
                    if (players.isEmpty()) return;

                    for (PlayerEntity player : players) {
                        ChatUtils.sendPlayerMsg("/whisper " + player.getName().getString() + " " + brainrot);
                    }

                    sendInfo("Sent brainrot to " + players.size() + " players.");
                    break;
            }
        }
    }

    private List<PlayerEntity> getPlayersInRadius() {
        return mc.world.getPlayers().stream()
            .filter(e -> !(e instanceof ClientPlayerEntity) && !e.isSpectator())
            .filter(e -> mc.player.squaredDistanceTo(e) <= radius.get() * radius.get())
            .collect(Collectors.toList());
    }

    private String pickBrainrot() {
        return brainrots.get(MathUtil.pickRandom(brainrots));
    }

    private void loadBrainrots() {
        try {
            Resource resource = mc.getResourceManager().getResource(file).orElseThrow();
            brainrots.addAll(resource.getReader().lines().toList());
        } catch (Exception e) {
            Genyo.LOG.error("Brainrots not found: {}", e.getMessage());
            sendError("Couldn't find brainrot file. Send logs.");
            notFound();
        }
    }

    private void notFound() {
        toggle();
        sendDisableMsg("Brainrot not found.");
    }

    private void updateCooldown(Cooldown value) {
        switch (value) {
            case Five -> cooldown = 5;
            case Ten -> cooldown = 10;
            case Fifteen -> cooldown = 15;
            case Twenty -> cooldown = 20;
        }
    }

    private enum Range {
        Server, Radius
    }

    private enum Cooldown {
        Five, Ten, Fifteen, Twenty
    }

    private enum ChatMode {
        Default, Blue, Green
    }

}
