package com.genyo.events;

public class StageEvent {

    private EventStage stage;

    public void setStage(EventStage stage) {
        this.stage = stage;
    }

    public EventStage getStage() {
        return stage;
    }

    public enum EventStage {
        PRE,
        POST
    }
}
