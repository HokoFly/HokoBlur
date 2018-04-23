package com.hoko.blurlibrary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.hoko.blurlibrary.generator.BlurGenerator;
import com.hoko.blurlibrary.generator.NativeBlurGenerator;

import static com.hoko.blurlibrary.util.BitmapUtil.getViewBitmap;
import static com.hoko.blurlibrary.util.BitmapUtil.transformBitmap;

/**
 * Modified by yuxfzju on 17-2-4.
 */

public class EasyBlur {


    public static Bitmap blur(View view, int width, int height, int downScale, float radius) {
        return blur(view, width, height, 0, 0, downScale, radius);
    }

    public static Bitmap blur(View view, int width, int height, int translateX, int translateY, int downScale, float radius) {
        Bitmap bitmapOut = getViewBitmap(view, width, height, translateX, translateY, downScale);

        blur(bitmapOut, radius);

        return bitmapOut;
    }

    public static void blur(Bitmap bitmap, float radius) {
        BlurGenerator generator = new NativeBlurGenerator();
        generator.mode(Blur.MODE_STACK);
        generator.forceCopy(false);
        generator.sampleFactor(1.0f);
        generator.radius((int) radius);
        generator.needUpscale(false);
        generator.blur(bitmap);
    }
}

