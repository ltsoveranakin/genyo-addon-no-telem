package com.genyo.addon.utils;

import com.genyo.addon.GenyoAddon;

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
        if (list.size() == 1) return 0;

        int num = r.nextInt(0, list.size());
        return num-1;
    }

}
