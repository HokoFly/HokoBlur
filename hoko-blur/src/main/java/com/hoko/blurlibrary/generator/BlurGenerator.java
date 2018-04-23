package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;
import android.view.View;

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

    private int mTranslateX;
    private int mTranslateY;

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
    public void translateX(int translateX) {
        mTranslateX = translateX;
    }

    @Override
    public int translateX() {
        return mTranslateX;
    }

    @Override
    public void translateY(int translateY) {
        mTranslateY = translateY;
    }

    @Override
    public int translateY() {
        return mTranslateY;
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
            mRadius = 1;
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

        Bitmap transInBitmap = BitmapUtil.transformBitmap(inBitmap, translateX(), translateY());

        Bitmap scaledInBitmap = BitmapUtil.getScaledBitmap(transInBitmap, sampleFactor());

        Bitmap scaledOutBitmap = doInnerBlur(scaledInBitmap, concurrent);

        Bitmap outBitmap = mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / sampleFactor()) : scaledOutBitmap;
        return outBitmap;
    }


    protected abstract Bitmap doInnerBlur(Bitmap scaledBitmap, boolean concurrent);

    @Override
    public Bitmap blur(View view) {
        if (view == null) {
            throw new IllegalArgumentException("You must input a view !");
        }

        Bitmap viewBitmap = BitmapUtil.getViewBitmap(view, translateX(), translateY(), sampleFactor());

        Bitmap scaledOutBitmap = doInnerBlur(viewBitmap, true);

        Bitmap outBitmap = mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / sampleFactor()) : scaledOutBitmap;

        return outBitmap;
    }

    @Override
    public void asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback) {
        BlurTaskManager.getInstance().submit(new AsyncBlurTask(this, bitmap, callback));
    }

    @Override
    public void asyncBlur(View view, AsyncBlurTask.Callback callback) {
        BlurTaskManager.getInstance().submit(new AsyncBlurTask(this, view, callback));
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