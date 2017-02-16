package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.util.BitmapUtil;

/**
 * Created by xiangpi on 16/9/7.
 */
public class NativeBlurGenerator extends BlurGenerator {


    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap) {
        if (scaledInBitmap == null) {
            return null;
        }

        try {

            final int w = scaledInBitmap.getWidth();
            final int h = scaledInBitmap.getHeight();
            final int[] pixels = new int[w * h];
            scaledInBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            switch (mMode) {
                case Blur.MODE_BOX:
                    nativeBoxBlur(pixels, w, h, mRadius);
                    break;
                case Blur.MODE_STACK:
                    nativeStackBlur(pixels, w, h, mRadius);
                    break;
                case Blur.MODE_GAUSSIAN:
                    nativeGaussianBlur(pixels, w, h, mRadius);
                    break;
            }

            if (scaledInBitmap.isMutable()) {
                scaledInBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            } else {
                BitmapUtil.replaceBitmap(scaledInBitmap, pixels);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scaledInBitmap;
    }


    public native void nativeBoxBlur(int[] pixels, int width, int height, int radius);
    public native void nativeStackBlur(int[] pixels, int width, int height, int radius);
    public native void nativeGaussianBlur(int[] pixels, int width, int height, int radius);

    static {
        System.loadLibrary("ImageBlur");
    }

}
