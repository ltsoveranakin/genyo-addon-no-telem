package com.genyo.addon.managers;

import com.genyo.addon.managers.combat.CombatManager;
import com.genyo.addon.managers.combat.TotemManager;
import com.genyo.addon.managers.network.GDTogglerManager;
import com.genyo.addon.managers.network.NetworkManager;
import com.genyo.addon.managers.player.InteractionManager;
import com.genyo.addon.managers.player.InventoryManager;
import com.genyo.addon.managers.player.MovementManager;
import com.genyo.addon.managers.player.rotation.RotationManager;
import com.genyo.addon.managers.world.BlockManager;
import com.genyo.addon.render.Render3DEngine;
import meteordevelopment.meteorclient.MeteorClient;

public class Managers {

    public static final CombatManager COMBAT = new CombatManager();
    public static final Render3DEngine ENGINE3D = new Render3DEngine();
    public static final BlockManager BLOCK = new BlockManager();
    public static final InventoryManager INVENTORY = new InventoryManager();
    public static final NetworkManager NETWORK = new NetworkManager();
    public static final TotemManager TOTEM = new TotemManager();
    public static final InteractionManager INTERACT = new InteractionManager();
    public static final MovementManager MOVEMENT = new MovementManager();
    public static final RotationManager ROTATION = new RotationManager();

    public static void subscribe() {
        MeteorClient.EVENT_BUS.subscribe(COMBAT);
        MeteorClient.EVENT_BUS.subscribe(ENGINE3D);
        MeteorClient.EVENT_BUS.subscribe(BLOCK);
        MeteorClient.EVENT_BUS.subscribe(INVENTORY);
        MeteorClient.EVENT_BUS.subscribe(NETWORK);
        MeteorClient.EVENT_BUS.subscribe(TOTEM);
        MeteorClient.EVENT_BUS.subscribe(MOVEMENT);
        MeteorClient.EVENT_BUS.subscribe(INTERACT);
        MeteorClient.EVENT_BUS.subscribe(new GDTogglerManager());
        MeteorClient.EVENT_BUS.subscribe(ROTATION);
    }

}
