package com.hoko.blurlibrary.filter;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.anno.Direction;
import com.hoko.blurlibrary.anno.Mode;

import static com.hoko.blurlibrary.util.BitmapUtil.replaceBitmap;

/**
 * Created by yuxfzju on 2017/2/19.
 */

public final class OriginBlurFilter {

    public static void doBlur(@Mode int mode, Bitmap bitmap, int radius, int cores, int index, @Direction int direction) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int x = 0;
        int y = 0;
        int deltaX = 0;
        int deltaY = 0;

        if (direction == HokoBlur.HORIZONTAL) {
            deltaY = h / cores;
            y = index * deltaY;

            if (index == cores - 1) {
                deltaY = h - (cores - 1) * deltaY;
            }

            deltaX = w;
        } else if (direction == HokoBlur.VERTICAL){
            deltaX = w / cores;
            x = index * deltaX;

            if (index == cores - 1) {
                deltaX = w - (cores - 1) * deltaX;
            }

            deltaY = h;
        }


        final int[] pixels = new int[deltaX * deltaY];
        bitmap.getPixels(pixels, 0, deltaX, x, y, deltaX, deltaY);

        switch (mode) {
            case HokoBlur.MODE_BOX:
                BoxBlurFilter.doBlur(pixels, deltaX, deltaY, radius, direction);
                break;

            case HokoBlur.MODE_GAUSSIAN:
                GaussianBlurFilter.doBlur(pixels, deltaX, deltaY, radius, direction);
                break;

            case HokoBlur.MODE_STACK:
                StackBlurFilter.doBlur(pixels, deltaX, deltaY, radius, direction);
                break;
        }
        if (bitmap.isMutable()) {
            bitmap.setPixels(pixels, 0, deltaX, x, y, deltaX, deltaY);
        } else {
            replaceBitmap(bitmap, pixels, x, y, deltaX, deltaY);
        }

    }

    public static void doFullBlur(@Mode int mode, Bitmap bitmap, int radius) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        switch (mode) {
            case HokoBlur.MODE_BOX:
                BoxBlurFilter.doBlur(pixels, w, h, radius, HokoBlur.BOTH);
                break;

            case HokoBlur.MODE_GAUSSIAN:
                GaussianBlurFilter.doBlur(pixels, w, h, radius, HokoBlur.BOTH);
                break;

            case HokoBlur.MODE_STACK:
                StackBlurFilter.doBlur(pixels, w, h, radius, HokoBlur.BOTH);
                break;
        }
        if (bitmap.isMutable()) {
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        } else {
            replaceBitmap(bitmap, pixels, 0, 0, w, h);
        }
    }
}
