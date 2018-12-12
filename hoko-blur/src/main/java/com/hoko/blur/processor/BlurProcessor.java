package com.hoko.blur.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Mode;
import com.hoko.blur.anno.Scheme;
import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.task.AsyncBlurTask;
import com.hoko.blur.task.BlurTaskManager;
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

    public BlurProcessor(Builder builder) {
        mMode = builder.mMode;
        mScheme = builder.mScheme;
        mRadius = builder.mRadius;
        mSampleFactor = builder.mSampleFactor;
        mIsForceCopy = builder.mIsForceCopy;
        mNeedUpscale = builder.mNeedUpscale;
        mTranslateX = builder.mTranslateX;
        mTranslateY = builder.mTranslateY;
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
        return BlurTaskManager.getInstance().submit(new AsyncBlurTask(this, bitmap, callback));
    }

    @Override
    public Future asyncBlur(View view, AsyncBlurTask.Callback callback) {
        return BlurTaskManager.getInstance().submit(new AsyncBlurTask(this, view, callback));
    }

    protected void free() {

    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        @Mode
        private int mMode = HokoBlur.MODE_STACK;
        @Scheme
        private int mScheme = HokoBlur.SCHEME_NATIVE;
        private int mRadius = 5;
        private float mSampleFactor = 5.0f;
        private boolean mIsForceCopy = false;
        private boolean mNeedUpscale = true;

        private int mTranslateX = 0;
        private int mTranslateY = 0;

        Context mCtx;

        public Builder(Context context) {
            Preconditions.checkNotNull(context, "context == null");
            mCtx = context.getApplicationContext();
        }

        public Builder(BlurProcessor blurProcessor) {
            mMode = blurProcessor.mode();
            mScheme = blurProcessor.scheme();
            mRadius = blurProcessor.radius();
            mSampleFactor = blurProcessor.sampleFactor();
            mIsForceCopy = blurProcessor.forceCopy();
            mNeedUpscale = blurProcessor.needUpscale();
            mTranslateX = blurProcessor.translateX();
            mTranslateY = blurProcessor.translateY();
        }

        public Builder context(Context ctx) {
            mCtx = ctx;
            return this;
        }

        public Builder mode(@Mode int mode) {
            mMode = mode;
            return this;
        }

        public Builder scheme(@Scheme int scheme) {
            mScheme = scheme;
            return this;
        }

        public Builder radius(int radius) {
            mRadius = radius;
            return this;
        }

        public Builder sampleFactor(float factor) {
            mSampleFactor = factor;
            return this;
        }

        public Builder forceCopy(boolean isForceCopy) {
            mIsForceCopy = isForceCopy;
            return this;
        }

        public Builder needUpscale(boolean needUpscale) {
            mNeedUpscale = needUpscale;
            return this;
        }

        public Builder translateX(int translateX) {
            mTranslateX = translateX;
            return this;
        }

        public Builder translateY(int translateY) {
            mTranslateY = translateY;
            return this;
        }

        /**
         * Get different types of Blur Processors
         */
        public BlurProcessor processor() {
            return BlurProcessorFactory.getBlurProcessor(mScheme, this);
        }

    }
}