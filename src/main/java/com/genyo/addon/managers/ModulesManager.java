package com.genyo.addon.managers;

import com.genyo.addon.modules.AngelSexHulkenberg;
import com.genyo.addon.modules.GenyoAutoEZ;
import com.genyo.addon.modules.TescoCrystal;
import com.genyo.addon.modules.TescoTrajectories;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class ModulesManager {

    private static Modules modules = Modules.get();

    public static void initModules() {
        modules.add(new GenyoAutoEZ());
        modules.add(new TescoCrystal());
        modules.add(new AngelSexHulkenberg());
        modules.add(new TescoTrajectories());
    }

}
