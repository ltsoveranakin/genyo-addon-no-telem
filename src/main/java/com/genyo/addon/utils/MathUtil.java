package com.genyo.addon.utils;

import java.util.List;
import java.util.Random;

public class MathUtil {

    private static final Random r = new Random();

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

    public static int pickRandom(List list) {
        int num = r.nextInt(1, list.size());
        return num-1;
    }

}
