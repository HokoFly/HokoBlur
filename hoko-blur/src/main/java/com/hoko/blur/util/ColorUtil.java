package com.hoko.blur.util;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class ColorUtil {

    public static float[] toRgbaFloatComponents(@ColorInt int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);
        return new float[]{
                r / 255f,
                g / 255f,
                b / 255f,
                a / 255f
        };


    }
}
