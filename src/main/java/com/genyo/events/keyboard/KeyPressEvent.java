package com.genyo.events.keyboard;

import meteordevelopment.meteorclient.events.Cancellable;

public class KeyPressEvent extends Cancellable {

    private static final KeyPressEvent INSTANCE = new KeyPressEvent();

    public int key;
    public int scanCode;

    public static KeyPressEvent get(int key, int scanCode) {
        INSTANCE.key = key;
        INSTANCE.scanCode = scanCode;

        return INSTANCE;
    }

}
