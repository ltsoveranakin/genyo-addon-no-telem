package com.genyo.addon.systems.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.mixin.accessor.AccessorGameOptions;
import com.genyo.addon.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Set;

public class GenyoSkinBlink extends GenyoModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay (mp)")
        .description("hulkenberg????")
        .defaultValue(2)
        .sliderRange(1, 15)
        .build()
    );

    private final Setting<Boolean> exclueCapes = sgGeneral.add(new BoolSetting.Builder()
        .name("Exclude Capes")
        .description("Don't blink capes.")
        .defaultValue(true)
        .build()
    );

    public GenyoSkinBlink() {
        super(GenyoAddon.MISC, "genyo-skin-blink", "i love kiwi. i love kiwi. i love kiwi. i love kiwi. ");
    }

    private int timer = 0;
    private Set<PlayerModelPart> enabledParts = new HashSet<>();

    @Override
    public void onActivate() {
        if (mc.options == null) return;

        timer = 0;
        enabledParts = ((AccessorGameOptions) mc.options).getPlayerModelParts();
    }

    @Override
    public void onDeactivate() {
        if (enabledParts == null || mc.options == null) return;

        timer = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (exclueCapes.get() && part.equals(PlayerModelPart.CAPE)) continue;

            mc.options.setPlayerModelPart(part, enabledParts.contains(part));
        }
        mc.options.sendClientSettings();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        timer++;
        if (mc.player == null && mc.world == null) return;

        if (!(timer >= (delay.get() * 20))) return;

        Set<PlayerModelPart> currentParts = ((AccessorGameOptions) mc.options).getPlayerModelParts();
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (exclueCapes.get() && part.equals(PlayerModelPart.CAPE)) continue;

            mc.options.setPlayerModelPart(part, !currentParts.contains(part));
        }

        mc.options.sendClientSettings();

        timer = 0;
    }

}
