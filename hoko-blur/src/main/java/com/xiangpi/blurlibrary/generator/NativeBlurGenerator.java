package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.util.BitmapUtil;

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
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap) {
        if (scaledInBitmap == null) {
            return null;
        }

        final int w = scaledInBitmap.getWidth();
        final int h = scaledInBitmap.getHeight();
        final int[] pixels = new int[w * h];
        scaledInBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        switch (mBlurMode) {
            case BOX:
                nativeBoxBlur(pixels, w, h, mRadius);
                break;
            case STACK:
                nativeStackBlur(pixels, w, h, mRadius);
                break;
            case GAUSSIAN:
                nativeGaussianBlur(pixels, w, h, mRadius);
                break;
        }

        return Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
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
