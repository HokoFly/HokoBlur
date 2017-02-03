package com.hoko.blurlibrary.util;

/**
 * Created by xiangpi on 2017/2/3.
 */

public class BlurUtil {
    public static final int MAX_RADIUS = 25;

    public static int clamp(int i, int minValue, int maxValue) {
        if (i < minValue) {
            return minValue;
        } else if (i > maxValue) {
            return maxValue;
        } else {
            return i;
        }
    }

    public static int checkRadius(int radius) {
        return clamp(radius, 0, 25);
    }

}
