package com.genyo.addon;

import com.genyo.addon.gui.EnemiesTab;
import com.genyo.addon.hud.PvPNeccessaryHud;
import com.genyo.addon.managers.Managers;
import com.genyo.addon.managers.ModulesManager;
import com.genyo.addon.modules.AngelSexHulkenberg;
import com.genyo.addon.modules.GenyoAutoEZ;
import com.genyo.addon.modules.TescoCrystal;
import com.genyo.addon.systems.enemies.Enemies;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class GenyoAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category GENYO = new Category("Genyo");
    public static final HudGroup HUD_GROUP = new HudGroup("Genyo");

    @Override
    public void onInitialize() {
        LOG.info("Genyo fasz indul genyo");

        Tabs.add(new EnemiesTab());
        Systems.add(new Enemies());

        // Managers mert ez menőn néz ki

        ModulesManager.initModules();
        Managers.subscribe();

        // HUD
        Hud.get().register(PvPNeccessaryHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(GENYO);
    }

    @Override
    public String getPackage() {
        return "com.genyo.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("wuritz", "genyo-addon");
    }
}
