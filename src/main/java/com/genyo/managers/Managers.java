package com.genyo.managers;

import com.genyo.core.sound.SoundManager;
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
import com.genyo.managers.world.tick.TickManager;
import com.genyo.render.Render3DEngine;

import static meteordevelopment.meteorclient.MeteorClient.*;

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
        EVENT_BUS.subscribe(COMBAT);
        EVENT_BUS.subscribe(ENGINE3D);
        EVENT_BUS.subscribe(BLOCK);
        EVENT_BUS.subscribe(INVENTORY);
        EVENT_BUS.subscribe(NETWORK);
        EVENT_BUS.subscribe(TOTEM);
        EVENT_BUS.subscribe(MOVEMENT);
        EVENT_BUS.subscribe(INTERACT);
        EVENT_BUS.subscribe(new GDTogglerManager());
        EVENT_BUS.subscribe(ROTATION);
        EVENT_BUS.subscribe(POSITION);
        EVENT_BUS.subscribe(ANTICHEAT);
        EVENT_BUS.subscribe(TICK);
        EVENT_BUS.subscribe(PEARL);
        EVENT_BUS.subscribe(SOCIAL);
        EVENT_BUS.subscribe(SOUND);
    }

    public static void init() {
        subscribe();
    }

}
