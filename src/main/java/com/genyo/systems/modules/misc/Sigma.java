package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.math.MathUtil;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Sigma extends GenyoModule {

    public Sigma() {
        super(Genyo.MISC, "sigma", "and i heard em say, nothing's ever promised tomorrow today");

        loadSigmas();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
        .name("Time Interval")
        .description("The time between the comings of Christ (in minutes)")
        .min(1)
        .defaultValue(5)
        .max(20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> the = sgGeneral.add(new BoolSetting.Builder()
        .name("This doesn't do anything")
        .description("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh")
        .defaultValue(false)
        .build()
    );

    private final Setting<NBColor> color = sgGeneral.add(new EnumSetting.Builder<NBColor>()
        .name("9b9t Text Color")
        .description("only works on 9b9t")
        .defaultValue(NBColor.None)
        .build()
    );

    private final Timer timer = new CacheTimer();
    private final Identifier sigmaFile = Identifier.of(Genyo.MOD_ID, "sigma.txt");
    private final List<String> sigmas = new ArrayList<>();

    @Override
    public void onDeactivate() {
        timer.reset();
    }

    @Override
    public void onActivate() {
        timer.reset();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null && mc.world == null) return;

        if (!timer.passed(interval.get() * 60000)) return;
        //if (!timer.passed(5000)) return; // for testing

        String output = "";
        String sigma = getSigmaText();

        if (sigma == null || sigma.isEmpty()) {
            sigmaNotFound();
            return;
        }

        if (color.get().equals(NBColor.Blue)) output += "`";
        else if (color.get().equals(NBColor.Green)) output += ">";

        output += sigma;

        ChatUtils.sendPlayerMsg(output);
        timer.reset();
    }

    private String getSigmaText() {
        return sigmas.get(MathUtil.pickRandom(sigmas));
    }

    private void sigmaNotFound() {
        toggle();
        sendDisableMsg("Sigma text not found.");
    }

    private void loadSigmas() {
        try {
            Resource resource = mc.getResourceManager().getResource(sigmaFile).orElseThrow();
            sigmas.addAll(resource.getReader().lines().toList());
        } catch (Exception e) {
            Genyo.LOG.info(e.getMessage());
            sendError("Couldn't read file. Send logs to wuritz pls.");
            sigmaNotFound();
        }
    }

    private enum NBColor {
        None,
        Green,
        Blue
    }

}
