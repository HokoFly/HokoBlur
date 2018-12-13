package com.hoko.blur.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Mode;
import com.hoko.blur.anno.Scheme;
import com.hoko.blur.api.IBlurBuild;
import com.hoko.blur.task.AsyncBlurTask;

import java.util.concurrent.Future;

public class HokoBlurBuild implements IBlurBuild {

    @Mode
    int mMode = HokoBlur.MODE_STACK;
    @Scheme
    int mScheme = HokoBlur.SCHEME_NATIVE;
    int mRadius = 5;
    float mSampleFactor = 5.0f;
    boolean mIsForceCopy = false;
    boolean mNeedUpscale = true;

    int mTranslateX = 0;
    int mTranslateY = 0;

    Context mCtx;

    public HokoBlurBuild(Context context) {
        this.mCtx = context;
    }


    @Override
    public IBlurBuild context(Context context) {
        this.mCtx = context;
        return this;
    }

    @Override
    public IBlurBuild mode(int mode) {
        this.mMode = mode;
        return this;
    }

    @Override
    public IBlurBuild scheme(int scheme) {
        this.mScheme = scheme;
        return this;
    }

    @Override
    public IBlurBuild radius(int radius) {
        this.mRadius = radius;
        return this;
    }

    @Override
    public IBlurBuild sampleFactor(float sampleFactor) {
        this.mSampleFactor = sampleFactor;
        return this;
    }

    @Override
    public IBlurBuild forceCopy(boolean isForceCopy) {
        this.mIsForceCopy = isForceCopy;
        return this;
    }

    @Override
    public IBlurBuild needUpscale(boolean needUpscale) {
        this.mNeedUpscale = needUpscale;
        return this;
    }

    @Override
    public IBlurBuild translateX(int translateX) {
        this.mTranslateX = translateX;
        return this;
    }

    @Override
    public IBlurBuild translateY(int translateY) {
        this.mTranslateY = translateY;
        return this;
    }


    @Override
    public BlurProcessor processor() {
        return BlurProcessorFactory.getBlurProcessor(mScheme, this);
    }

    @Override
    public Bitmap blur(Bitmap bitmap) {
        BlurProcessor processor = processor();
        return processor.blur(bitmap);
    }

    @Override
    public Bitmap blur(View view) {
        BlurProcessor processor = processor();
        return processor.blur(view);
    }

    @Override
    public Future asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback) {
        BlurProcessor processor = processor();
        return processor.asyncBlur(bitmap, callback);
    }

    @Override
    public Future asyncBlur(View view, AsyncBlurTask.Callback callback) {
        BlurProcessor processor = processor();
        return processor.asyncBlur(view, callback);
    }


}
