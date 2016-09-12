package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.origin.BoxBlurFilter;
import com.xiangpi.blurlibrary.origin.GaussianBlurFilter;
import com.xiangpi.blurlibrary.origin.StackBlurFilter;
import com.xiangpi.blurlibrary.util.BitmapUtil;

/**
 * Created by xiangpi on 16/9/7.
 */
public class OriginBlurGenerator extends BlurGenerator{

//    private static volatile OriginBlurGenerator sGenerator;
//
//    public static OriginBlurGenerator getInstance() {
//        if (sGenerator == null) {
//            synchronized (OriginBlurGenerator.class) {
//                if (sGenerator == null) {
//                    sGenerator = new OriginBlurGenerator();
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

        Bitmap scaledOutBitmap = null;

        try {
            final int w = scaledInBitmap.getWidth();
            final int h = scaledInBitmap.getHeight();
            final int[] pixels = new int[w * h];
            scaledInBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            if (mBlurMode == Blur.BlurMode.BOX) {
                BoxBlurFilter.doBlur(pixels, w, h, mRadius);
            } else if (mBlurMode == Blur.BlurMode.STACK) {
                StackBlurFilter.doBlur(pixels, w, h, mRadius);
            } else if (mBlurMode == Blur.BlurMode.GAUSSIAN) {
                GaussianBlurFilter.doBlur(pixels, w, h, mRadius);
            }

            scaledOutBitmap = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
            scaledOutBitmap = scaledInBitmap;
        }

        return scaledOutBitmap;
    }

//    public static void release() {
//        sGenerator = null;
//    }


    @Override
    public void setBlurRadius(int radius) {
        super.setBlurRadius(radius);

    }
}
