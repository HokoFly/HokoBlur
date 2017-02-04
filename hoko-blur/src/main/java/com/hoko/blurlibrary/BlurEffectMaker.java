package com.hoko.blurlibrary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.hoko.blurlibrary.generator.BitmapBlurGenerator;
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

    private static final BitmapBlurGenerator NATIVE_BLUR_GENERATOR = new NativeBlurGenerator();

    static {
        NATIVE_BLUR_GENERATOR.setBlurMode(Blur.MODE_STACK);
        //旧代码包含scale操作，为兼容旧代码这里设置factor为1.0，不做scale
        NATIVE_BLUR_GENERATOR.setSampleFactor(1.0f);
        NATIVE_BLUR_GENERATOR.forceCopy(false);
    }

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

        NATIVE_BLUR_GENERATOR.setBlurRadius((int) radius);

        ArrayList<NativeTask> horizontal = new ArrayList<NativeTask>(cores);
        ArrayList<NativeTask> vertical = new ArrayList<NativeTask>(cores);
        for (int i = 0; i < cores; i++) {
            horizontal.add(new NativeTask(src, NATIVE_BLUR_GENERATOR, cores, i, 1));
            vertical.add(new NativeTask(src, NATIVE_BLUR_GENERATOR, cores, i, 2));
        }

        try {
            BLUR_EXECUTOR.invokeAll(horizontal);
        } catch (InterruptedException e) {
        }

        try {
            BLUR_EXECUTOR.invokeAll(vertical);
        } catch (InterruptedException e) {
        }
    }

    private static class NativeTask implements Callable<Void> {
        private Bitmap _bitmapOut;
        private final BitmapBlurGenerator _generatior;
        private final int _totalCores;
        private final int _coreIndex;
        private final int _round;

        public NativeTask(Bitmap bitmapOut, BitmapBlurGenerator generatior, int totalCores, int coreIndex, int round) {
            _bitmapOut = bitmapOut;
            _generatior = generatior;
            _totalCores = totalCores;
            _coreIndex = coreIndex;
            _round = round;
        }

        @Override
        public Void call() throws Exception {
            _generatior.doBlur(_bitmapOut);
            return null;
        }
    }
}
