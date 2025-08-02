package com.genyo.addon.events.sync;

import meteordevelopment.meteorclient.events.Cancellable;

public class SyncEvent extends Cancellable {

    public static class Pre extends SyncEvent {
        private static final Pre INSTANCE = new Pre();

        public float yaw;
        public float pitch;
        public Runnable postAction;

        public static Pre get(float yaw, float pitch) {
            INSTANCE.yaw = yaw;
            INSTANCE.pitch = pitch;

            return INSTANCE;
        }
    }

    public static class Post extends SyncEvent {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }

}
