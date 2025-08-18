package com.genyo.addon.systems.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.systems.modules.GenyoModule;
import com.genyo.addon.utils.math.MathUtil;
import com.genyo.addon.utils.math.timer.CacheTimer;
import com.genyo.addon.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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

        if (!timer.passed(300000)) return;
        //if (!timer.passed(5000)) return;

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
        } catch (Exception ignored) { }

        if (sigma.isEmpty()) return null;
        else return sigma;
    }

    private enum NBColor {
        None,
        Green,
        Blue
    }

}
