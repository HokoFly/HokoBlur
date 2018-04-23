package com.hoko.blurlibrary.generator;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.anno.Scheme;
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
    int mMode;

    @Scheme
    private int mScheme;

    private float mSampleFactor;

    private boolean mIsForceCopy;

    private boolean mNeedUpscale;

    private int mTranslateX;
    private int mTranslateY;

    public BlurGenerator(Builder builder) {
        mMode = builder.mMode;
        mScheme = builder.mScheme;
        mRadius = builder.mRadius;
        mSampleFactor = builder.mSampleFactor;
        mIsForceCopy = builder.mIsForceCopy;
        mNeedUpscale = builder.mNeedUpscale;
        mTranslateX = builder.mTranslateX;
        mTranslateY = builder.mTranslateY;
    }

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
    public int scheme() {
        return mScheme;
    }

    @Override
    public boolean forceCopy() {
        return mIsForceCopy;
    }

    @Override
    public boolean needUpscale() {
        return mNeedUpscale;
    }


    @Override
    public int translateX() {
        return mTranslateX;
    }

    @Override
    public int translateY() {
        return mTranslateY;
    }

    @Override
    public Bitmap blur(Bitmap bitmap) {
        return doBlur(bitmap, true);
    }

    private Bitmap doBlur(Bitmap bitmap, boolean concurrent) {
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

    protected void free() {

    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        @Mode
        private static final int DEFAULT_MODE = HokoBlur.MODE_STACK;
        @Scheme
        private static final int DEFAULT_SCHEME = HokoBlur.SCHEME_NATIVE;
        private static final int DEFAULT_BLUR_RADIUS = 5;
        private static final float DEFAULT_SAMPLE_FACTOR = 5.0f;
        private static final boolean DEFAULT_FORCE_COPY = false;
        private static final boolean DEFAULT_UP_SCALE = true;
        private static final int DEFAULT_TRANSLATE_X = 0;
        private static final int DEFAULT_TRANSLATE_Y = 0;
        @Mode
        private int mMode = DEFAULT_MODE;
        @Scheme
        private int mScheme = DEFAULT_SCHEME;
        private int mRadius = DEFAULT_BLUR_RADIUS;
        private float mSampleFactor = DEFAULT_SAMPLE_FACTOR;
        private boolean mIsForceCopy = DEFAULT_FORCE_COPY;
        private boolean mNeedUpscale = DEFAULT_UP_SCALE;

        private int mTranslateX = DEFAULT_TRANSLATE_X;
        private int mTranslateY = DEFAULT_TRANSLATE_Y;

        Context mCtx;

        public Builder(Context context) {
            mCtx = context.getApplicationContext();
        }

        public Builder(IBlurGenerator blurGenerator) {
            mMode = blurGenerator.mode();
            mScheme = blurGenerator.scheme();
            mRadius = blurGenerator.radius();
            mSampleFactor = blurGenerator.sampleFactor();
            mIsForceCopy = blurGenerator.forceCopy();
            mNeedUpscale = blurGenerator.needUpscale();
            mTranslateX = blurGenerator.translateX();
            mTranslateY = blurGenerator.translateY();
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
         * 创建不同的模糊发生器
         * @return
         */
        public BlurGenerator blurGenerator() {
            BlurGenerator generator = null;

            switch (mScheme) {
                case HokoBlur.SCHEME_RENDER_SCRIPT:
                    generator = new RenderScriptBlurGenerator(this);
                    break;
                case HokoBlur.SCHEME_OPENGL:
                    generator = new OpenGLBlurGenerator(this);
                    break;
                case HokoBlur.SCHEME_NATIVE:
                    generator = new NativeBlurGenerator(this);
                    break;
                case HokoBlur.SCHEME_JAVA:
                    generator = new OriginBlurGenerator(this);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported blur scheme!");

            }

            return generator;
        }

        private void reset() {
            mMode = DEFAULT_MODE;
            mScheme = DEFAULT_SCHEME;
            mRadius = DEFAULT_BLUR_RADIUS;
            mSampleFactor = DEFAULT_SAMPLE_FACTOR;
            mIsForceCopy = DEFAULT_FORCE_COPY;
            mNeedUpscale = DEFAULT_UP_SCALE;
            mTranslateX = DEFAULT_TRANSLATE_X;
            mTranslateY = DEFAULT_TRANSLATE_Y;
        }
    }
}