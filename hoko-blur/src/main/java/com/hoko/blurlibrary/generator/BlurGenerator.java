package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.api.IBlurGenerator;
import com.hoko.blurlibrary.task.AsyncBlurTask;
import com.hoko.blurlibrary.task.BlurTaskManager;
import com.hoko.blurlibrary.util.BitmapUtil;

/**
 * Created by yuxfzju on 16/9/8.
 */
public abstract class BlurGenerator implements IBlurGenerator {

    int mRadius;

    @Mode
    int mMode = Blur.MODE_STACK;

    private float mSampleFactor;

    private boolean mIsForceCopy;

    private boolean mNeedUpscale = true;

    @Override
    public void mode(@Mode int mode) {
        mMode = mode;
    }

    @Override
    public void radius(int radius) {
        mRadius = radius;
    }

    @Override
    public void sampleFactor(float factor) {
        mSampleFactor = factor;
    }

    @Override
    @Mode
    public int mode() {
        return mMode;
    }

    @Override
    public int radius() {
        return mRadius;
    }

    @Override
    public float sampleFactor() {
        return mSampleFactor;
    }

    @Override
    public Bitmap blur(Bitmap bitmap) {
        return doBlur(bitmap, true);
    }

    public Bitmap doBlur(Bitmap bitmap, boolean concurrent) {
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

        if (mIsForceCopy) {
            inBitmap = bitmap.copy(bitmap.getConfig(), true);
        } else {
            inBitmap = bitmap;
        }

        Bitmap scaledInBitmap = BitmapUtil.getScaledBitmap(inBitmap, mSampleFactor);

        Bitmap scaledOutBitmap = doInnerBlur(scaledInBitmap, concurrent);

        Bitmap outBitmap = mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / mSampleFactor) : scaledOutBitmap;
        return outBitmap;
    }


    protected abstract Bitmap doInnerBlur(Bitmap scaledBitmap, boolean concurrent);

    public void asyncBlur(Bitmap bitmap, AsyncBlurTask.CallBack callBack) {
        BlurTaskManager.getInstance().submit(new AsyncBlurTask(this, bitmap, callBack));
    }

    @Override
    public void forceCopy(boolean isForceCopy) {
        mIsForceCopy = isForceCopy;
    }

    @Override
    public void needUpscale(boolean needUpscale) {
        mNeedUpscale = needUpscale;
    }

    protected void free() {

    }
}