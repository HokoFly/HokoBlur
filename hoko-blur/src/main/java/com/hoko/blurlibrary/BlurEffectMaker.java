package com.hoko.blurlibrary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.hoko.blurlibrary.generator.BlurGenerator;
import com.hoko.blurlibrary.generator.NativeBlurGenerator;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dolphinWang on 14-10-20.
 * Modified by xiangpi on 17-2-4.
 */

public class BlurEffectMaker {
    // 线程数到可用cpu核数的一半
    private static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() <= 3 ?
            1 : Runtime.getRuntime().availableProcessors() / 2;

    private static final ExecutorService BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    private static Bitmap drawBitmapOnView(View view, int width, int height, int translateX, int translateY, int downScale) {
        final float scale = 1.0f / downScale;

        final int downScaledWidth = (int) ((width - translateX) * scale);
        final int downScaledHeight = (int) ((height - translateY) * scale);

        Bitmap src = Bitmap.createBitmap(downScaledWidth, downScaledHeight, Bitmap.Config.ARGB_8888);

        if (view.getBackground() != null && view.getBackground() instanceof ColorDrawable) {
            src.eraseColor(((ColorDrawable) view.getBackground()).getColor());
        } else {
            src.eraseColor(Color.parseColor("#f6f6f6"));
        }

        Canvas canvas = new Canvas(src);
        canvas.translate(
                -(int) (translateX * scale), -(int) (translateY * scale)
        );
        if (downScale > 1) {
            canvas.scale(scale, scale);
        }

        view.draw(canvas);

        return src;
    }

    private static Bitmap buildBitmapFromSrc(Bitmap src, int width, int height, int translateX, int translateY, int downScale) {
        final float scale = 1.0f / downScale;

        Bitmap temp = Bitmap.createBitmap(src, translateX, translateY, width, height);

        if (!temp.isMutable()) {
            temp = temp.copy(temp.getConfig(), true);
        }

        Bitmap dest = Bitmap.createScaledBitmap(temp, (int) (width * scale), (int) (height * scale), true);

        return dest;
    }

    public static Bitmap makeBlur(View view, int width, int height, int downScale, float radius) {
        return makeBlur(view, width, height, 0, 0, downScale, radius);
    }

    public static Bitmap makeBlur(View view, int width, int height, int translateX, int translateY, int downScale, float radius) {
        Bitmap bitmapOut = drawBitmapOnView(view, width, height, translateX, translateY, downScale);

        makeBlur(bitmapOut, radius);

        return bitmapOut;
    }

    public static Bitmap makeBlur(Bitmap src, int width, int height, int translateX, int translateY, int downScale, float radius) {
        Bitmap bitmapOut = buildBitmapFromSrc(src, width, height, translateX, translateY, downScale);

        makeBlur(bitmapOut, radius);

        return bitmapOut;
    }

    public static Bitmap makeBlurWithForceCopy(Bitmap src, int width, int height, int translateX, int translateY, int downScale, float radius) {
        Bitmap bitmapOut = buildBitmapFromSrc(src, width, height, translateX, translateY, downScale);
        if (bitmapOut == src) {
            bitmapOut = src.copy(src.getConfig(), true);
        }

        makeBlur(bitmapOut, radius);

        return bitmapOut;
    }

    public static void makeBlur(Bitmap src, float radius) {
        int cores = EXECUTOR_THREADS;

        ArrayList<NativeTask> tasks = new ArrayList<NativeTask>(cores);
        tasks.add(new NativeTask(src, (int) radius));

        try {
            BLUR_EXECUTOR.invokeAll(tasks);
        } catch (InterruptedException e) {
        }
    }

    private static class NativeTask implements Callable<Void> {
        private Bitmap _bitmapOut;
        private int _radius;

        public NativeTask(Bitmap bitmapOut, int radius) {
            _bitmapOut = bitmapOut;
            _radius = radius;
        }

        @Override
        public Void call() throws Exception {
            BlurGenerator generator = new NativeBlurGenerator();
            generator.setBlurMode(Blur.MODE_STACK);
            generator.forceCopy(false);
            //旧代码包含scale操作，为兼容旧代码这里设置factor为1.0，不做scale
            generator.setSampleFactor(1.0f);
            generator.setBlurRadius(_radius);
            generator.needUpscale(false);
            generator.doBlur(_bitmapOut);
            return null;
        }
    }
}
