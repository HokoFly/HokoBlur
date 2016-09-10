package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.Blur;

/**
 * Created by xiangpi on 16/9/7.
 */
public class NativeBlurGenerator extends BlurGenerator{

//    private static volatile NativeBlurGenerator sGenerator;
//
//    public static NativeBlurGenerator getInstance() {
//        if (sGenerator == null) {
//            synchronized (NativeBlurGenerator.class) {
//                if (sGenerator == null) {
//                    sGenerator = new NativeBlurGenerator();
//                }
//            }
//        }
//
//        return sGenerator;
//    }

    @Override
    public Bitmap doBlur(Bitmap input) {
        if (input == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        final int w = input.getWidth();
        final int h = input.getHeight();
        final int[] pixels = new int[w * h];
        input.getPixels(pixels, 0, w, 0, 0, w, h);

        if (mBlurMode == Blur.BlurMode.BOX) {
            nativeBoxBlur(pixels, w, h, mRadius);
        } else if (mBlurMode == Blur.BlurMode.STACK) {
            nativeStackBlur(pixels, w, h, mRadius);
        } else if (mBlurMode == Blur.BlurMode.GAUSSIAN) {
            nativeStackBlur(pixels, w, h, mRadius);
        }
        final Bitmap blurred = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);

        return blurred;
    }

//    public static void release() {
//        sGenerator = null;
//    }


    public native void nativeBoxBlur(int[] pixels, int width, int height, int radius);
    public native void nativeStackBlur(int[] pixels, int width, int height, int radius);
    public native void nativeGaussianBlur(int[] pixels, int width, int height, int radius);

    static {
        System.loadLibrary("ImageBlur");
    }

}
