package com.hoko.blurlibrary.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by xiangpi on 16/9/12.
 */
public class BitmapUtil {
    public static Bitmap getScaledBitmap(Bitmap bitmap, float factor) {
        if (bitmap == null) {
            return null;
        }

        if (factor == 1.0f) {
            return bitmap;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final float ratio = 1f / factor;

        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static synchronized native void replaceBitmap(Bitmap bitmap, int[] pixels, int x, int y, int deltaX, int deltaY);

    static {
        System.loadLibrary("hoko_blur");
    }
}
