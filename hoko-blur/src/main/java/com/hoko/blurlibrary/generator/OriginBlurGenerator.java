package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.origin.BoxBlurFilter;
import com.hoko.blurlibrary.origin.GaussianBlurFilter;
import com.hoko.blurlibrary.origin.StackBlurFilter;

/**
 * Created by xiangpi on 16/9/7.
 */
public class OriginBlurGenerator extends BitmapBlurGenerator {

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

            switch (mMode) {
                case Blur.MODE_BOX:
                    BoxBlurFilter.doBlur(pixels, w, h, mRadius);
                    break;
                case Blur.MODE_STACK:
                    StackBlurFilter.doBlur(pixels, w, h, mRadius);
                    break;
                case Blur.MODE_GAUSSIAN:
                    GaussianBlurFilter.doBlur(pixels, w, h, mRadius);
                    break;
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
