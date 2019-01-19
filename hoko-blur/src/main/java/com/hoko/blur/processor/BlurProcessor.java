package com.hoko.blur.processor;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.anno.Scheme;
import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.task.AsyncBlurTask;
import com.hoko.blur.task.BitmapAsyncBlurTask;
import com.hoko.blur.task.BlurTaskManager;
import com.hoko.blur.task.ViewAsyncBlurTask;
import com.hoko.blur.util.BitmapUtil;
import com.hoko.blur.util.Preconditions;

import java.util.concurrent.Future;

/**
 * Created by yuxfzju on 16/9/8.
 */
public abstract class BlurProcessor implements IBlurProcessor {

    int mRadius;

    @Mode
    int mMode;

    @Scheme
    private int mScheme;

    private float mSampleFactor;

    private boolean mIsForceCopy;

    private boolean mNeedUpscale;

    private int mTranslateX;
    private int mTranslateY;

    private IBlurResultDispatcher mDispatcher;

    public BlurProcessor(HokoBlurBuild builder) {
        mMode = builder.mMode;
        mScheme = builder.mScheme;
        mRadius = builder.mRadius;
        mSampleFactor = builder.mSampleFactor;
        mIsForceCopy = builder.mIsForceCopy;
        mNeedUpscale = builder.mNeedUpscale;
        mTranslateX = builder.mTranslateX;
        mTranslateY = builder.mTranslateY;
        mDispatcher = builder.mDispatcher;
    }

    public void mode(@Mode int mode) {
        mMode = mode;
    }

    public void radius(int radius) {
        mRadius = radius;
    }

    public void sampleFactor(float factor) {
        mSampleFactor = factor;
    }

    @Mode
    public int mode() {
        return mMode;
    }

    public int radius() {
        return mRadius;
    }

    public float sampleFactor() {
        return mSampleFactor;
    }

    @Scheme
    public int scheme() {
        return mScheme;
    }

    public boolean forceCopy() {
        return mIsForceCopy;
    }

    public boolean needUpscale() {
        return mNeedUpscale;
    }


    public int translateX() {
        return mTranslateX;
    }

    public int translateY() {
        return mTranslateY;
    }

    @Override
    public Bitmap blur(Bitmap bitmap) {
        return doBlur(bitmap, true);
    }

    private Bitmap doBlur(Bitmap bitmap, boolean concurrent) {
        Preconditions.checkNotNull(bitmap, "bitmap == null");
        Preconditions.checkArgument(!bitmap.isRecycled(), "You must input an unrecycled bitmap !");

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

        return mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / sampleFactor()) : scaledOutBitmap;
    }


    protected abstract Bitmap doInnerBlur(Bitmap scaledBitmap, boolean concurrent);

    @Override
    public Bitmap blur(View view) {
        Preconditions.checkNotNull(view, "You must input a view !");

        Bitmap viewBitmap = BitmapUtil.getViewBitmap(view, translateX(), translateY(), sampleFactor());

        Bitmap scaledOutBitmap = doInnerBlur(viewBitmap, true);

        return mNeedUpscale ? BitmapUtil.getScaledBitmap(scaledOutBitmap, 1f / sampleFactor()) : scaledOutBitmap;
    }

    @Override
    public Future asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback) {
        return BlurTaskManager.getInstance().submit(new BitmapAsyncBlurTask(this, bitmap, callback, mDispatcher));
    }

    @Override
    public Future asyncBlur(View view, AsyncBlurTask.Callback callback) {
        return BlurTaskManager.getInstance().submit(new ViewAsyncBlurTask(this, view, callback, mDispatcher));
    }

    protected void free() {

    }

}