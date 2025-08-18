package com.genyo.events.render;

import meteordevelopment.meteorclient.events.Cancellable;

public class TickCounterEvent extends Cancellable {

    private static final TickCounterEvent INSTANCE = new TickCounterEvent();
    public float ticks;

    public static TickCounterEvent get(float ticks) {
        INSTANCE.ticks = ticks;

        return INSTANCE;
    }

}
