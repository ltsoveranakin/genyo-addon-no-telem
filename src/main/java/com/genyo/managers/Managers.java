package com.genyo.managers;

import com.genyo.managers.anticheat.AntiCheatManager;
import com.genyo.managers.combat.CombatManager;
import com.genyo.managers.combat.PearlManager;
import com.genyo.managers.combat.TotemManager;
import com.genyo.managers.network.GDTogglerManager;
import com.genyo.managers.network.NetworkManager;
import com.genyo.managers.player.InteractionManager;
import com.genyo.managers.player.InventoryManager;
import com.genyo.managers.player.MovementManager;
import com.genyo.managers.player.PositionManager;
import com.genyo.managers.player.rotation.RotationManager;
import com.genyo.managers.world.BlockManager;
import com.genyo.managers.world.SocialManager;
import com.genyo.managers.world.sound.SoundManager;
import com.genyo.managers.world.tick.TickManager;
import com.genyo.render.Render3DEngine;
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
    public static final PositionManager POSITION = new PositionManager();
    public static final AntiCheatManager ANTICHEAT = new AntiCheatManager();
    public static final TickManager TICK = new TickManager();
    public static final SocialManager SOCIAL = new SocialManager();
    public static final PearlManager PEARL = new PearlManager();
    public static final SoundManager SOUND = new SoundManager();

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
        MeteorClient.EVENT_BUS.subscribe(POSITION);
        MeteorClient.EVENT_BUS.subscribe(ANTICHEAT);
        MeteorClient.EVENT_BUS.subscribe(TICK);
        MeteorClient.EVENT_BUS.subscribe(PEARL);
        MeteorClient.EVENT_BUS.subscribe(SOCIAL);
        MeteorClient.EVENT_BUS.subscribe(SOUND);
    }

    public static void init() {
        subscribe();
    }

}
