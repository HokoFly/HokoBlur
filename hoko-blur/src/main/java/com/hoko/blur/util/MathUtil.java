package com.hoko.blur.util;

/**
 * Created by yuxfzju on 2017/2/3.
 */

public class MathUtil {
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
