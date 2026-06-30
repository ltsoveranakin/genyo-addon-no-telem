package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.events.render.TickCounterEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.settings.FloatSetting;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class GenyoTimer extends GenyoModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Float> ticksConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Ticks")
        .description("The of the if the of if the.")
        .min(0.1f)
        .defaultValue(4.4f)
        .max(50.0f)
        .sliderRange(0.1f, 50.0f)
        .build()
    );
    private final Setting<Boolean> tpsSyncConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("TPS Sync")
        .description("Syncs game tick speed to server tick speed")
        .defaultValue(false)
        .build()
    );
    //
    private float prevTimer = -1.0f;

    //TODO: speed module
    private float timer = 1.0f;
    public GenyoTimer() {
        super(Genyo.MISC, "genyo-timer", "Changes the change to the change and change.");
    }

    @Override
    public String getInfoString() {
        return String.format("%s", timer);
    }

    @Override
    public void onDeactivate() {
        Managers.TICK.setClientTick(1.0f);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (tpsSyncConfig.get()) {
            timer = Math.max(Managers.TICK.getTpsCurrent() / 20.0f, 0.1f);
            return;
        }
        timer = ticksConfig.get();
    }

    @EventHandler
    public void onTickCounter(TickCounterEvent event) {
        if (timer != 1.0f) {
            event.cancel();
            event.ticks = timer;
        }
    }

    /**
     * @return
     */
    public float getTimer() {
        return timer;
    }

    /**
     * @param timer
     */
    public void setTimer(float timer) {
        prevTimer = this.timer;
        this.timer = timer;
    }

    public void resetTimer() {
        if (prevTimer > 0.0f) {
            this.timer = prevTimer;
            prevTimer = -1.0f;
        }
    }

}
