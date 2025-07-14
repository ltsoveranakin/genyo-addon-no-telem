package com.genyo.addon.systems.incombat;

import com.genyo.addon.events.UnderCombatEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InCombatSystem extends System<InCombatSystem> implements Iterable<CombatPerson> {

    private static final InCombatSystem INSTANCE = new InCombatSystem();
    private final List<CombatPerson> inCombat = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Object lock = new Object();

    public final Settings settings = new Settings();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("Enabled")
        .description("tyu")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> combatCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("Cooldown")
        .description("mikor resetelje")
        .defaultValue(30)
        .sliderRange(15, 60)
        .visible(enabled::get)
        .build()
    );

    private final Setting<Boolean> autoUnfriend = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Unfriend")
        .description("utána visszaaddolja")
        .defaultValue(true)
        .visible(enabled::get)
        .build()
    );

    private int cooldown = 0;

    private void countdown() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (lock) {
                if (cooldown > 0) {
                    cooldown--;
                } else {
                    clear();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public InCombatSystem() {
        super("incombat");
    }

    @Override
    public void init() {
        clear();
    }

    @Override
    public @NotNull Iterator<CombatPerson> iterator() {
        return inCombat.iterator();
    }

    public boolean add(CombatPerson person) {
        if (!inCombat.contains(person)) {
            inCombat.add(person);

            if (autoUnfriend.get()) Friends.get().remove(Friends.get().get(person.getPlayer()));

            resetCooldown();
            save();
            return true;
        }

        return false;
    }

    public int getRemainingCooldown() {
        return cooldown;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public boolean contains(CombatPerson person) {
        return get(person.getName()) != null;
    }

    public boolean contains(PlayerEntity entity) {
        return get(entity.getName().getString()) != null;
    }

    public CombatPerson get(String name) {
        for (CombatPerson person : inCombat) {
            if (person.getName().equals(name)) {
                return person;
            }
        }

        return null;
    }

    public void resetCooldown() {
        synchronized (lock) {
            if (cooldown == 0) {
                cooldown = combatCooldown.get();
                countdown();
            } else {
                cooldown = combatCooldown.get();
            }
        }
    }

    public boolean empty() {
        return inCombat.isEmpty();
    }

    public List<CombatPerson> getInCombat() {
        return inCombat;
    }

    public int size() {
        return inCombat.size();
    }

    public void clear() {
        MeteorExecutor.execute(() -> inCombat.forEach(person ->  {
            if (autoUnfriend.get()) {
                if (person.wasFriendB()) Friends.get().add(person.getFriend());
            }
        }));

        get().getInCombat().clear();
        save();
    }

    public static InCombatSystem get() {
        return INSTANCE;
    }

    @EventHandler
    private void onUnderAttack(UnderCombatEvent event) {
        if (mc.world == null) return;

        CombatPerson person = new CombatPerson(event.entity);
        if (!InCombatSystem.get().contains(person)) {
            InCombatSystem.get().add(person);
        }

        if (!empty()) resetCooldown();
    }

}
