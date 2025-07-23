package com.genyo.addon.utils.render;

import meteordevelopment.meteorclient.utils.render.color.Color;

public class ColorUtil {

    public static Color interpolateColor(float value, Color start, Color end)
    {
        float sr = start.r / 255.0f;
        float sg = start.g / 255.0f;
        float sb = start.b / 255.0f;
        float sa = start.a / 255.0f;
        float er = end.r / 255.0f;
        float eg = end.g / 255.0f;
        float eb = end.b / 255.0f;
        float ea = end.a / 255.0f;
        return new Color(sr * value + er * (1.0f - value),
            sg * value + eg * (1.0f - value),
            sb * value + eb * (1.0f - value),
            sa * value + ea * (1.0f - value));
    }

}
