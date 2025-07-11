package com.hoko.blur.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

/**
 * Created by yuxfzju on 16/9/12.
 */
public final class BitmapUtil {
    private static final Paint SCALE_PAINT = new Paint();
    public static Bitmap getScaledBitmap(Bitmap bitmap, float factor) {
        if (bitmap == null) {
            return null;
        }
        if (factor == 1.0f) {
            if (!bitmap.isMutable()) {
                return bitmap.copy(bitmap.getConfig(), true);
            } else {
                return bitmap;
            }
        }
        final float scale = 1.0f / factor;
        int newWidth = (int)(bitmap.getWidth() * scale);
        int newHeight = (int)(bitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
        Canvas canvas = new Canvas(scaledBitmap);
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scale, scale);
        canvas.drawBitmap(bitmap, scaleMatrix, SCALE_PAINT);
        return scaledBitmap;
    }

    /**
     * get bitmap from a view
     */
    public static Bitmap getViewBitmap(View view, int translateX, int translateY, float sampleFactor) {
        final float scale = 1.0f / sampleFactor;
        final int width = view.getWidth();
        final int height = view.getHeight();
        final int downScaledWidth = (int) ((width - translateX) * scale);
        final int downScaledHeight = (int) ((height - translateY) * scale);
        Bitmap bitmap = Bitmap.createBitmap(downScaledWidth, downScaledHeight, Bitmap.Config.ARGB_8888);
        if (view.getBackground() != null && view.getBackground() instanceof ColorDrawable) {
            bitmap.eraseColor(((ColorDrawable) view.getBackground()).getColor());
        } else {
            bitmap.eraseColor(Color.TRANSPARENT);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(
                -(int) (translateX * scale), -(int) (translateY * scale)
        );
        if (sampleFactor > 1.0f) {
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
