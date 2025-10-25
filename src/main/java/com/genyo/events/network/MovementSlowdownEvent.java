package com.genyo.events.network;

import net.minecraft.client.input.Input;

public class MovementSlowdownEvent {

    private static final MovementSlowdownEvent INSTANCE = new MovementSlowdownEvent();

    public Input input;

    public static MovementSlowdownEvent get(Input input) {
        INSTANCE.input = input;

        return INSTANCE;
    }

}
