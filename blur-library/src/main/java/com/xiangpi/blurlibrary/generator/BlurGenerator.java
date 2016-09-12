package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.util.BitmapUtil;

/**
 * Created by xiangpi on 16/9/8.
 */
public abstract class BlurGenerator implements IBlur {

//    private static final int DEFAULT_BLUR_KERNEL_RADIUS = 5;

//    private static final Blur.BlurMode DEFAULT_BLUR_MODE = Blur.BlurMode.GAUSSIAN;

//    private static final float DEFAULT_SAMPLE_FACTOR = 5.0f;

    protected int mRadius;

    protected Blur.BlurMode mBlurMode;

    protected float mSampleFactor;

    @Override
    public void setBlurMode(Blur.BlurMode mode) {
        mBlurMode = mode;
    }

    @Override
    public void setBlurRadius(int radius) {
        mRadius = radius;
    }

    @Override
    public void setSampleFactor(float factor) {
        mSampleFactor = factor;
    }

    @Override
    public Bitmap doBlur(Bitmap inBitmap) {
        if (inBitmap == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        if (mRadius <= 0) {
            return inBitmap;
        }

        if (mSampleFactor < 1.0f) {
            mSampleFactor = 1.0f;
        }

        Bitmap scaledInBitmap = BitmapUtil.getScaledBitmap(inBitmap, mSampleFactor);
        Bitmap scaledOutBitmap = doInnerBlur(scaledInBitmap);
        Bitmap outBitmap = BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / mSampleFactor);

        return outBitmap;
    }

    protected abstract Bitmap doInnerBlur(Bitmap bitmap);
}
