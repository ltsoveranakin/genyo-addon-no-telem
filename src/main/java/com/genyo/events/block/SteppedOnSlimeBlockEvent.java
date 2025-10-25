package com.genyo.events.block;

import meteordevelopment.meteorclient.events.Cancellable;

public class SteppedOnSlimeBlockEvent extends Cancellable {

    private static final SteppedOnSlimeBlockEvent INSTANCE = new SteppedOnSlimeBlockEvent();

    public static SteppedOnSlimeBlockEvent get() {
        return INSTANCE;
    }

}
