package com.genyo.utils.collection;

import com.genyo.utils.math.timer.TickTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.LinkedList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MessageTickQueue {

    private final List<Message> queue = new LinkedList<>();
    private int delay;

    private final Timer timer = new TickTimer();

    public MessageTickQueue(int delay) {
        this.delay = delay;

        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null && mc.world == null) return;

        if (queue.isEmpty()) {
            timer.reset();
            return;
        }

        if (timer.passed(delay)) {
            Message msg = queue.getFirst();
            ChatUtils.sendPlayerMsg(msg.message());
            timer.reset();

            if (msg.kill()) queue.clear();
            else queue.removeFirst();
        }
    }

    public void addMessage(Message message) {
        queue.add(message);
    }

    public void setDelay(int newDelay) {
        this.delay = newDelay;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

}
