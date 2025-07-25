package com.genyo.addon;

import com.genyo.addon.modules.combat.GenyoAutoCrystal;
import com.genyo.addon.modules.combat.GenyoAutoMine;
import com.genyo.addon.modules.combat.GenyoSurround;
import com.genyo.addon.modules.combat.GenyoSurroundV2;
import com.genyo.addon.modules.misc.GenyoAutoEZ;
import com.genyo.addon.modules.misc.GenyoGoodbye;
import com.genyo.addon.modules.misc.GenyoWelcome;
import com.genyo.addon.modules.visual.AngelSexHulkenberg;
import com.genyo.addon.modules.misc.GenyoSkinBlink;
import com.genyo.addon.systems.enemies.EnemiesTab;
import com.genyo.addon.hud.InCombatHud;
import com.genyo.addon.hud.PvPNeccessaryHud;
import com.genyo.addon.managers.Managers;
import com.genyo.addon.systems.enemies.Enemies;
import com.genyo.addon.systems.incombat.InCombatSystem;
import com.genyo.addon.systems.incombat.InCombatTab;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class GenyoAddon extends MeteorAddon {

    public static final Logger LOG = LogUtils.getLogger();
    public static final Category GENYO = new Category("Genyo", Items.MILK_BUCKET.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Genyo");

    @Override
    public void onInitialize() {
        LOG.info("Genyo fasz indul genyo");

        // Tabs
        initTabs();

        // Systems
        initSystems();

        initModules(Modules.get());

        // Managers mert ez menőn néz ki
        Managers.subscribe();

        // HUD
        initHUD(Hud.get());
    }

    private void initTabs() {
        Tabs.add(new EnemiesTab());
        Tabs.add(new InCombatTab());
    }

    private void initSystems() {
        Systems.add(new Enemies());
        Systems.add(new InCombatSystem());
    }

    private void initModules(Modules modules) {
        modules.add(new GenyoAutoEZ());
        modules.add(new AngelSexHulkenberg());
        modules.add(new GenyoSurround());
        modules.add(new GenyoWelcome());
        modules.add(new GenyoSkinBlink());
        modules.add(new GenyoGoodbye());
        modules.add(new GenyoAutoMine());
        modules.add(new GenyoSurroundV2());
        modules.add(new GenyoAutoCrystal());
    }

    private void initHUD(Hud hud) {
        hud.register(PvPNeccessaryHud.INFO);
        hud.register(InCombatHud.INFO);
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
