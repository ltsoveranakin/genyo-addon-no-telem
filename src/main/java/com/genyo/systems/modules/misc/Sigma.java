package com.genyo.systems.modules.misc;

import com.genyo.GenyoAddon;
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

import java.util.List;

public class Sigma extends GenyoModule {

    public Sigma() {
        super(GenyoAddon.MISC, "sigma", "and i heard em say, nothing's ever promised tomorrow today");
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
        String sigma = getSigma();

        if (sigma == null) {
            sigmaNotFound();
            return;
        }

        if (color.get().equals(NBColor.Blue)) output += "`";
        else if (color.get().equals(NBColor.Green)) output += ">";

        output += sigma;

        ChatUtils.sendPlayerMsg(output);
        timer.reset();
    }

    private void sigmaNotFound() {
        toggle();
        sendDisableMsg("Sigma text not found.");
    }

    private String getSigma() {
        String sigma = "";
        Identifier identifier = Identifier.of(GenyoAddon.MOD_ID, "sigma.txt");

        try {
            Resource resource = mc.getResourceManager().getResource(identifier).orElseThrow();
            List<String> messages = resource.getReader().lines().toList();
            sigma = messages.get(MathUtil.pickRandom(messages));
        } catch (Exception e) {
            GenyoAddon.LOG.info(e.getMessage());
            sendError("Couldn't read file. Send logs to wuritz pls.");
        }

        if (sigma.isEmpty()) return null;
        else return sigma;
    }

    private enum NBColor {
        None,
        Green,
        Blue
    }

}
