package com.genyo.addon.utils;

public class MathUtil {

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

}
