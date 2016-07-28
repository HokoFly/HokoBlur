package com.example.xiangpi.dynamicblurdemo.util;

/**
 * Created by xiangpi on 16/7/25.
 */
public class ImageMath {
    public static int clamp(int i, int minValue, int maxValue) {
        if (i < minValue) {
            return minValue;
        } else if (i > maxValue) {
            return maxValue;
        } else {
            return i;
        }
    }
}
