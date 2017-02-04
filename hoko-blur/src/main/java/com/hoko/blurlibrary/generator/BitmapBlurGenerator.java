package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.api.IBitmapBlur;
import com.hoko.blurlibrary.util.BitmapUtil;

/**
 * Created by xiangpi on 16/9/8.
 */
public abstract class BitmapBlurGenerator implements IBitmapBlur {

    int mRadius;

    @Blur.BlurMode int mMode;

    private float mSampleFactor;

    private boolean mIsForceCopy;

    @Override
    public void setBlurMode(@Blur.BlurMode int mode) {
        mMode = mode;
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
    @Blur.BlurMode
    public int getBlurMode() {
        return mMode;
    }

    @Override
    public int getBlurRadius() {
        return mRadius;
    }

    @Override
    public float getSampleFactor() {
        return mSampleFactor;
    }

    @Override
    public Bitmap doBlur(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException("You must input an unrecycled bitmap !");
        }

        if (mRadius <= 0) {
            return bitmap;
        }

        if (mSampleFactor < 1.0f) {
            mSampleFactor = 1.0f;
        }

        Bitmap inBitmap = null;

        //factor为1.0必须进行scale，因此bitmap不会是immutable
        if (mIsForceCopy || (!bitmap.isMutable() && mSampleFactor == 1.0f)) {
            inBitmap = bitmap.copy(bitmap.getConfig(), true);
        } else {
            inBitmap = bitmap;
        }

        Bitmap scaledInBitmap = BitmapUtil.getScaledBitmap(inBitmap, mSampleFactor);
        Bitmap scaledOutBitmap = doInnerBlur(scaledInBitmap);
        Bitmap outBitmap = BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / mSampleFactor);

        return outBitmap;
    }

    protected abstract Bitmap doInnerBlur(Bitmap scaledBitmap);

    @Override
    public void forceCopy(boolean isForceCopy) {
        mIsForceCopy = isForceCopy;
    }

    protected void free() {

    }
}
