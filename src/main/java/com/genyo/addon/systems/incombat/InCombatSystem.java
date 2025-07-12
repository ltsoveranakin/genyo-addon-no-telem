package com.genyo.addon.systems.incombat;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.UnderAttackEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.orbit.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InCombatSystem extends System<InCombatSystem> implements Iterable<CombatPerson> {

    private static final InCombatSystem INSTANCE = new InCombatSystem();
    private final List<CombatPerson> inCombat = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Object lock = new Object();

    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> combatCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("Cooldown")
        .description("mikor resetelje")
        .defaultValue(30)
        .sliderRange(15, 60)
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
    public @NotNull Iterator<CombatPerson> iterator() {
        return inCombat.iterator();
    }

    public boolean add(CombatPerson person) {
        if (!inCombat.contains(person)) {
            inCombat.add(person);
            resetCooldown();
            save();
            return true;
        }

        return false;
    }

    public int getRemainingCooldown() {
        return cooldown;
    }

    public boolean contains(CombatPerson person) {
        return get(person.getName()) != null;
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

    public int getCombatCooldown() {
        return combatCooldown.get();
    }

    public void clear() {
        get().getInCombat().clear();
        save();
    }

    public static InCombatSystem get() {
        return INSTANCE;
    }

    @EventHandler
    private void onUnderAttack(UnderAttackEvent event) {
        CombatPerson person = new CombatPerson(event.entity);
        if (!InCombatSystem.get().contains(person)) {
            InCombatSystem.get().add(person);
        }
    }

}
