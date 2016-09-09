package com.example.xiangpi.dynamicblurdemo.helper;

import android.graphics.Bitmap;

import com.example.xiangpi.dynamicblurdemo.originblur.BoxBlur;
import com.example.xiangpi.dynamicblurdemo.originblur.StackBlur;

/**
 * Created by xiangpi on 16/9/7.
 */
public class OriginBlurGenerator extends BlurGenerator{

    private static volatile OriginBlurGenerator sGenerator;

    public static OriginBlurGenerator getInstance() {
        if (sGenerator == null) {
            synchronized (OriginBlurGenerator.class) {
                if (sGenerator == null) {
                    sGenerator = new OriginBlurGenerator();
                }
            }
        }

        return sGenerator;
    }

    @Override
    public Bitmap doBlur(Bitmap input) {
        if (input == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        Bitmap output = null;
        try {
            final int w = input.getWidth();
            final int h = input.getHeight();
            final int[] pixels = new int[w * h];
            input.getPixels(pixels, 0, w, 0, 0, w, h);

            if (mBlurMode == Blur.BlurMode.BOX) {
                BoxBlur.doBlur(pixels, w, h, mRadius);
            } else if (mBlurMode == Blur.BlurMode.STACK) {
                StackBlur.doBlur(pixels, w, h, mRadius);
            } else if (mBlurMode == Blur.BlurMode.GAUSSIAN) {
                StackBlur.doBlur(pixels, w, h, mRadius);
            }

            output = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
            output = input;
        }

        return output;
    }

    public static void release() {
        sGenerator = null;
    }


    @Override
    public void setBlurMode(Blur.BlurMode mode) {
        mBlurMode = mode;
    }

    @Override
    public void setBlurRadius(int radius) {
        mRadius = radius;
    }

}
