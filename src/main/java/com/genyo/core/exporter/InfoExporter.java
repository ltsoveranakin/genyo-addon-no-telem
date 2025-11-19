package com.genyo.core.exporter;

import com.genyo.Genyo;
import com.genyo.core.exporter.records.ExHUD;
import com.genyo.core.exporter.records.ExModule;
import com.genyo.core.exporter.records.ExSystem;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class InfoExporter {

    public static List<String> categories = new ArrayList<>();
    public static List<ExModule> modules = new ArrayList<>();
    public static List<ExSystem> systems = new ArrayList<>();
    public static List<ExHUD> huds = new ArrayList<>();

    public InfoExporter() {
        acquireInfo();
    }

    public static void acquireInfo() {
        // Categories
        categories.addAll(initCategories());
        if (categories.isEmpty()) {
            return;
        }

        // Modules
        for (String category : categories) {
            List<ExModule> currentModules = acquireModules();
            if (!currentModules.isEmpty()) modules.addAll(currentModules);
            else Genyo.LOG.error("Failed to add {} modules.", category);
        }

        // Systems
        systems.addAll(addSystems());

        // Huds
        huds.addAll(addHuds());
    }

    public void export() {
        Genyo.LOG.info("Initializing exporter.");

        // Empty checks
        if (emptyCheck()) {
            Genyo.LOG.error("Exporting infos failed, a list is empty.");
            return;
        }

        Map<String, Object> output = new LinkedHashMap<>();

        output.put("categories", categories);
        output.put("modules", exportModules());
        output.put("systems", exportSystems());
        output.put("huds", exportHuds());

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        String outputString = yaml.dump(output);

        try (FileWriter writer = new FileWriter("genyo_info.yaml")) {
            writer.write(outputString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Genyo.LOG.info("Successfully exported Genyo's infos!");
    }

    private boolean emptyCheck() {
        if (categories.isEmpty()) return true;
        if (modules.isEmpty()) return true;
        if (systems.isEmpty()) return true;
        if (huds.isEmpty()) return true;

        return false;
    }

    private static List<Map<String, Object>> exportModules() {
        List<Map<String, Object>> export = new ArrayList<>();

        for (ExModule module : modules) {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", module.name());
            map.put("description", module.description());
            map.put("category", module.category().label);

            export.add(map);
        }

        return export;
    }

    private static List<Map<String, Object>> exportSystems() {
        List<Map<String, Object>> export = new ArrayList<>();

        for (ExSystem system : systems) {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", system.name());
            map.put("description", system.description());

            export.add(map);
        }

        return export;
    }

    private static List<Map<String, Object>> exportHuds() {
        List<Map<String, Object>> export = new ArrayList<>();

        for (ExHUD hud : huds) {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", hud.name());
            map.put("description", hud.description());

            export.add(map);
        }

        return export;
    }

    private static List<ExModule> acquireModules() {
        List<ExModule> acquired = new ArrayList<>();

        for (Category category : Genyo.CATEGORIES) {
            for (Module module : Modules.get().getGroup(category)) {
                String moduleName = module.name;
                if (moduleName.contains("-")) { // if the module name is not already formatted, due to genyo efficient development
                    moduleName = Utils.nameToTitle(moduleName);
                }

                ExModule exModule = new ExModule(moduleName, module.description, handleCategory(category));
                acquired.add(exModule);
            }
        }

        return acquired;
    }

    private static List<ExSystem> addSystems() {
        List<ExSystem> init = new ArrayList<>();

        ExSystem enemies = new ExSystem("Enemies", "Store a list of enemies that Genyo will use. Add and remove players, use Meteor's BetterTab to see them in Tab and other features.");
        ExSystem sound = new ExSystem("Sound", "Custom sounds that make your gaming experience even better. 100% real information.");

        init.add(enemies);
        init.add(sound);

        return init;
    }

    private static List<ExHUD> addHuds() {
        List<ExHUD> init = new ArrayList<>();

        for (HudElementInfo<?> info : Genyo.HUD_ELEMENTS) {
            ExHUD current = new ExHUD(info.title, info.description);
            init.add(current);
        }

        return init;
    }

    private static List<String> initCategories() {
        List<String> init = new ArrayList<>();
        init.add(Categories.COMBAT.label);
        init.add(Categories.MISC.label);
        init.add(Categories.MOVEMENT.label);
        init.add(Categories.VISUAL.label);
        init.add(Categories.WORLD.label);

        return init;
    }

    private static Categories handleCategory(Category category) {
        if (category == Genyo.COMBAT) return Categories.COMBAT;
        else if (category == Genyo.MISC) return Categories.MISC;
        else if (category == Genyo.MOVEMENT) return Categories.MOVEMENT;
        else if (category == Genyo.VISUAL) return Categories.VISUAL;
        else if (category == Genyo.WORLD) return Categories.WORLD;
        else return null;
    }

}
