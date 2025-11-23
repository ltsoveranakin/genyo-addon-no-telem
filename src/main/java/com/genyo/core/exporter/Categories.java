package com.genyo.core.exporter;

public enum Categories {
    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    VISUAL("Visual"),
    WORLD("World");

    public final String label;

    private Categories(String label) {
        this.label = label;
    }
}
