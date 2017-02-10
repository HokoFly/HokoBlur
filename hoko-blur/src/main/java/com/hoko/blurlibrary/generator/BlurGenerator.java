package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.api.IBlurGenerator;
import com.hoko.blurlibrary.task.BlurTask;
import com.hoko.blurlibrary.task.BlurTaskManager;
import com.hoko.blurlibrary.util.BitmapUtil;

/**
 * Created by xiangpi on 16/9/8.
 */
public abstract class BlurGenerator implements IBlurGenerator {

    int mRadius;

    @Mode
    int mMode = Blur.MODE_STACK;

    private float mSampleFactor;

    private boolean mIsForceCopy;
    
    private boolean mNeedUpscale = true;

    @Override
    public void setBlurMode(@Mode int mode) {
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
    @Mode
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

        //factor不为1.0必须进行scale，因此bitmap不会是immutable
        if (mIsForceCopy || (!bitmap.isMutable() && mSampleFactor == 1.0f)) {
            inBitmap = bitmap.copy(bitmap.getConfig(), true);
        } else {
            inBitmap = bitmap;
        }

        Bitmap scaledInBitmap = BitmapUtil.getScaledBitmap(inBitmap, mSampleFactor);
        Bitmap scaledOutBitmap = doInnerBlur(scaledInBitmap);
        
        Bitmap outBitmap = mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / mSampleFactor) : scaledOutBitmap;
        return outBitmap;
    }

    protected abstract Bitmap doInnerBlur(Bitmap scaledBitmap);

    public void doAsyncBlur(Bitmap bitmap, BlurTask.CallBack callBack) {
        BlurTaskManager.getInstance().submit(new BlurTask(this, bitmap, callBack));
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
