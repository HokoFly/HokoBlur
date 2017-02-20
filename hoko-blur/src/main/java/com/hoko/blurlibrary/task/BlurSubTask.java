package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Direction;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.anno.Scheme;
import com.hoko.blurlibrary.origin.OriginBlurHelper;
import com.hoko.blurlibrary.util.NativeBlurHelper;

import java.util.concurrent.Callable;

/**
 * 对bitmap的模糊任务，任务主要模糊bitmap的一部分，通过线程池管理，实现对bitmap的并发模糊处理
 * Created by xiangpi on 2017/2/17.
 */

public class BlurSubTask implements Callable<Void> {

    @Scheme
    protected final int mScheme;
    @Mode
    protected final int mMode;
    protected final Bitmap mBitmapOut;
    protected final int mRadius;
    protected final int mIndex;
    protected final int mCores;

    @Direction
    protected final int mDirection;

    public BlurSubTask(@Scheme int scheme, @Mode int mode, Bitmap bitmapOut, int radius, int cores, int index, @Direction int direction) {
        mScheme = scheme;
        mMode = mode;
        mBitmapOut = bitmapOut;
        mRadius = radius;
        mIndex = index;
        mCores = cores;
        mDirection = direction;
    }

    @Override
    public Void call() throws Exception {
        if (mBitmapOut == null || mBitmapOut.isRecycled() || mCores <= 0) {
            return null;
        }

        applyPixelsBlur();

        return null;
    }

    private void applyPixelsBlur() {
        switch (mScheme) {
            case Blur.SCHEME_NATIVE:
                NativeBlurHelper.doBlur(mMode, mBitmapOut, mRadius, mCores, mIndex, mDirection);
                break;

            case Blur.SCHEME_JAVA:
                OriginBlurHelper.doBlur(mMode, mBitmapOut, mRadius, mCores, mIndex, mDirection);
                break;

            default:
                break;

        }

    }
}
