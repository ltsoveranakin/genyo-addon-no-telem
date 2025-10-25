package com.genyo.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.util.Hand;

public class SwingEvent extends Cancellable {

    private static final SwingEvent INSTANCE = new SwingEvent();

    public Hand hand;

    public static SwingEvent get(Hand hand) {
        INSTANCE.hand = hand;

        return INSTANCE;
    }

}
