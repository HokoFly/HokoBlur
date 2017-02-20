package com.hoko.blurlibrary.util;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Direction;
import com.hoko.blurlibrary.anno.Mode;

/**
 * Created by xiangpi on 2017/2/18.
 */

public class NativeBlurHelper {

    public static void doBlur(@Mode int mode, Bitmap bitmap, int radius, int cores, int index, @Direction int direction) {
        switch (mode) {
            case Blur.MODE_BOX:
                nativeBoxBlur(bitmap, radius, cores, index, direction);
                break;
//
            case Blur.MODE_STACK:
                nativeStackBlur(bitmap, radius, cores, index, direction);
                break;

            case Blur.MODE_GAUSSIAN:
                nativeGaussianBlur(bitmap, radius, cores, index, direction);
                break;

        }
    }

    public static void doFullBlur(@Mode int mode, Bitmap bitmap, int radius) {
        doBlur(mode, bitmap, radius, 1, 0, Blur.HORIZONTAL);
        doBlur(mode, bitmap, radius, 1, 0, Blur.VERTICAL);
    }

    public static native void nativeBoxBlur(Bitmap bitmap, int radius, int cores, int index, int direction);
    public static native void nativeStackBlur(Bitmap bitmap, int radius, int cores, int index, int direction);
    public static native void nativeGaussianBlur(Bitmap bitmap, int radius, int cores, int index, int direction);

    static {
        System.loadLibrary("ImageBlur");
    }

}
