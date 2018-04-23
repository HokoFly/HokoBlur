package com.hoko.blurlibrary.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

/**
 * Created by yuxfzju on 16/9/12.
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

    /**
     * 从view绘制bitmap
     *
     * @param width 输出bitmap的宽度
     * @param height 输出bitmap的高度
     * @param translateX 离view原点的X方向偏移
     * @param translateY 离view原点的Y方向偏移
     * @return
     */
    public static Bitmap getViewBitmap(View view, int width, int height, int translateX, int translateY, int downScale) {
        final float scale = 1.0f / downScale;

        final int downScaledWidth = (int) ((width - translateX) * scale);
        final int downScaledHeight = (int) ((height - translateY) * scale);

        Bitmap bitmap = Bitmap.createBitmap(downScaledWidth, downScaledHeight, Bitmap.Config.ARGB_8888);

        if (view.getBackground() != null && view.getBackground() instanceof ColorDrawable) {
            bitmap.eraseColor(((ColorDrawable) view.getBackground()).getColor());
        } else {
            bitmap.eraseColor(Color.parseColor("#f6f6f6"));
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.translate(
                -(int) (translateX * scale), -(int) (translateY * scale)
        );
        if (downScale > 1) {
            canvas.scale(scale, scale);
        }

        view.draw(canvas);

        return bitmap;
    }

    public static Bitmap transformBitmap(Bitmap bitmap, int translateX, int translateY) {

        if (translateX == 0 && translateY == 0) {
            return bitmap;
        }

        return Bitmap.createBitmap(bitmap, translateX, translateY, bitmap.getWidth() - translateX, bitmap.getHeight() - translateY);
    }


    public static synchronized native void replaceBitmap(Bitmap bitmap, int[] pixels, int x, int y, int deltaX, int deltaY);

    static {
        System.loadLibrary("hoko_blur");
    }
}
