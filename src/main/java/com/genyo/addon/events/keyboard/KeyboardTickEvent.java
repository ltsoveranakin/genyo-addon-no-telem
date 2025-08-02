package com.genyo.addon.events.keyboard;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.client.input.Input;

public class KeyboardTickEvent extends Cancellable  {

    public static class Pre extends KeyboardTickEvent {
        private static final Pre INSTANCE = new Pre();

        public Input input;

        public static Pre get(Input input) {
            INSTANCE.input = input;

            return INSTANCE;
        }
    }

    public static class Post extends KeyboardTickEvent {
        private static final Post INSTANCE = new Post();

        public Input input;

        public static Post get(Input input) {
            INSTANCE.input = input;

            return INSTANCE;
        }
    }


}
