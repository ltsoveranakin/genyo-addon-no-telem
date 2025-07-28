package com.genyo.addon;

import com.genyo.addon.modules.combat.*;
import com.genyo.addon.modules.misc.*;
import com.genyo.addon.modules.visual.AngelSexHulkenberg;
import com.genyo.addon.modules.visual.GenyoPenisESP;
import com.genyo.addon.modules.world.GenyoAutoMine;
import com.genyo.addon.modules.world.GenyoAutoTool;
import com.genyo.addon.modules.world.GenyoScaffold;
import com.genyo.addon.modules.world.GenyoSpeedmine;
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
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.misc.Version;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class GenyoAddon extends MeteorAddon {

    public static final Logger LOG = LogUtils.getLogger();
    public static final Category GENYO = new Category("Genyo", Items.MILK_BUCKET.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Genyo");

    public static final String MOD_ID = "genyo";
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

        NAME = MOD_META.getName();

        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        // When building and running through IntelliJ and not Gradle it doesn't replace the version so just use a dummy
        if (versionString.equals("${version}")) versionString = "0.0.0";

        VERSION = new Version(versionString);
    }

    @Override
    public void onInitialize() {
        LOG.info("Genyo fasz indul genyo");

        if (Modules.get().isActive(DiscordPresence.class)) {
            Modules.get().get(DiscordPresence.class).toggle();
            LOG.info("oh no la policia");
        }

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
        modules.add(new GenyoDiscord());
        modules.add(new GenyoSpeedmine());
        modules.add(new GenyoAutoTool());
        modules.add(new GenyoReplenish());
        modules.add(new GenyoSelfTrap());
        modules.add(new GenyoScaffold());
        modules.add(new GenyoPenisESP());
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
